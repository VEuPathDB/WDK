/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.AttributeQueryReference;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;

/**
 * @author xingao
 * 
 */
public class BasketFactory {

  public static final String REALTIME_BASKET_QUESTION_SUFFIX = "ByRealtimeBasket";
  public static final String SNAPSHOT_BASKET_QUESTION_SUFFIX = "BySnapshotBasket";
  private static final String REALTIME_BASKET_ID_QUERY_SUFFIX = "ByRealtimeBasket";
  private static final String SNAPSHOT_BASKET_ID_QUERY_SUFFIX = "BySnapshotBasket";
  static final String BASKET_ATTRIBUTE_QUERY_SUFFIX = "_basket_attrs";
  public static final String BASKET_ATTRIBUTE = "in_basket";

  public static final String PARAM_USER_SIGNATURE = "user_signature";
  public static final String PARAM_DATASET_SUFFIX = "Dataset";

  public static final String TABLE_BASKET = "user_baskets";
  public static final String COLUMN_BASKET_ID = "basket_id";
  public static final String COLUMN_USER_ID = "user_id";
  public static final String COLUMN_PROJECT_ID = "project_id";
  public static final String COLUMN_RECORD_CLASS = "record_class";

  private static final Logger logger = Logger.getLogger(BasketFactory.class);

  private WdkModel wdkModel;
  private String schema;

  /**
     * 
     */
  public BasketFactory(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    this.schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  public String getSchema() {
    return schema;
  }

  public void addToBasket(User user, Step step) throws WdkModelException,
      SQLException {
    logger.debug("adding to basket from step...");

    AnswerValue answerValue = step.getAnswerValue();
    RecordClass recordClass = answerValue.getQuestion().getRecordClass();
    List<String[]> pkValues = answerValue.getAllIds();
    addToBasket(user, recordClass, pkValues);
  }

  /**
   * @param user
   * @param recordClass
   * @param pkValues
   *          a list of primary key values. the inner map is a primary-key
   *          column-value map.
   */
  public void addToBasket(User user, RecordClass recordClass,
      List<String[]> pkValues) throws SQLException {
    int userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    String sqlInsert = "INSERT INTO " + schema + TABLE_BASKET + " ("
        + COLUMN_BASKET_ID + ", " + COLUMN_USER_ID + ", " + COLUMN_PROJECT_ID
        + ", " + COLUMN_RECORD_CLASS;
    String sqlValues = "";
    String sqlCount = "SELECT count(*) FROM " + schema + TABLE_BASKET
        + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
        + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlInsert += ", " + Utilities.COLUMN_PK_PREFIX + i;
      sqlValues += ", ?";
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    sqlInsert += ") VALUES (?, ?, ?, ?" + sqlValues + ")";
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    PreparedStatement psInsert = null, psCount = null;
    try {
      psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      int count = 0;
      for (String[] row : pkValues) {
        // fill or truncate the pk columns
        String[] pkValue = new String[pkColumns.length];
        int length = Math.min(row.length, pkValue.length);
        System.arraycopy(row, 0, pkValue, 0, length);

        // check if the record already exists.
        setParams(psCount, userId, projectId, rcName, pkValue);
        boolean hasRecord = false;
        ResultSet resultSet = null;
        try {
          long start = System.currentTimeMillis();
          resultSet = psCount.executeQuery();
          QueryLogger.logEndStatementExecution(sqlCount,
              "wdk-basket-factory-count", start);
          if (resultSet.next()) {
            int rsCount = resultSet.getInt(1);
            hasRecord = (rsCount > 0);
          }
        } finally {
          if (resultSet != null) SqlUtils.closeResultSetOnly(resultSet);
        }
        if (hasRecord) continue;

        // insert new record
        int basketId = platform.getNextId(dataSource, schema, TABLE_BASKET);
        psInsert.setInt(1, basketId);
        psInsert.setInt(2, userId);
        psInsert.setString(3, projectId);
        psInsert.setString(4, rcName);
        for (int i = 0; i < pkValue.length; i++) {
          psInsert.setString(i + 5, pkValue[i]);
        }
        psInsert.addBatch();

        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psInsert.executeBatch();
          QueryLogger.logEndStatementExecution(sqlInsert,
              "wdk-basket-factory-insert", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psInsert.executeBatch();
        QueryLogger.logEndStatementExecution(sqlInsert,
            "wdk-basket-factory-insert", start);
      }
      // check the remote table to solve out-dated db-link issue with
      // Oracle.
      checkRemoteTable();
    } finally {
      SqlUtils.closeStatement(psInsert);
      SqlUtils.closeStatement(psCount);
    }
  }

  public void removeFromBasket(User user, Step step) throws WdkModelException,
      SQLException {
    AnswerValue answerValue = step.getAnswerValue();
    RecordClass recordClass = answerValue.getQuestion().getRecordClass();
    List<String[]> pkValues = answerValue.getAllIds();
    removeFromBasket(user, recordClass, pkValues);
  }

  public void removeFromBasket(User user, RecordClass recordClass,
      List<String[]> pkValues) throws SQLException {
    int userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    String sqlDelete = "DELETE FROM " + schema + TABLE_BASKET + " WHERE "
        + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
        + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlDelete += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psDelete = null;
    try {
      psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
      int count = 0;
      for (String[] row : pkValues) {
        // fill or truncate the pk columns
        String[] pkValue = new String[pkColumns.length];
        int length = Math.min(row.length, pkValue.length);
        System.arraycopy(row, 0, pkValue, 0, length);

        setParams(psDelete, userId, projectId, rcName, pkValue);
        psDelete.addBatch();
        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psDelete.executeBatch();
          QueryLogger.logEndStatementExecution(sqlDelete,
              "wdk-basket-factory-delete", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psDelete.executeBatch();
        QueryLogger.logEndStatementExecution(sqlDelete,
            "wdk-basket-factory-delete", start);
      }
      // check the remote table to solve out-dated db-link issue with
      // Oracle.
      checkRemoteTable();
    } finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  public void clearBasket(User user, RecordClass recordClass)
      throws SQLException {
    int userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String sqlDelete = "DELETE FROM " + schema + TABLE_BASKET + " WHERE "
        + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
        + COLUMN_RECORD_CLASS + " = ?";

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psDelete = null;
    try {
      long start = System.currentTimeMillis();
      psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
      psDelete.setInt(1, userId);
      psDelete.setString(2, projectId);
      psDelete.setString(3, rcName);
      psDelete.executeUpdate();
      QueryLogger.logEndStatementExecution(sqlDelete,
          "wdk-basket-factory-delete-all", start);

      // check the remote table to solve out-dated db-link issue with
      // Oracle.
      checkRemoteTable();
    } finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  public Map<RecordClass, Integer> getBasketCounts(User user)
      throws WdkModelException {
    Map<RecordClass, Integer> counts = new LinkedHashMap<RecordClass, Integer>();
    Map<String, RecordClass> recordClasses = new LinkedHashMap<String, RecordClass>();
    for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : rcSet.getRecordClasses()) {
        if (recordClass.isUseBasket()) {
          counts.put(recordClass, 0);
          recordClasses.put(recordClass.getFullName(), recordClass);
        }
      }
    }
    // load the unique counts
    String sql = "SELECT " + COLUMN_RECORD_CLASS + ", count(*) AS record_size "
        + " FROM (SELECT DISTINCT * FROM " + schema + TABLE_BASKET + " WHERE "
        + COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ?) t "
        + " GROUP BY " + COLUMN_RECORD_CLASS;
    DataSource ds = wdkModel.getUserDb().getDataSource();
    ResultSet rs = null;
    try {
      PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setInt(1, user.getUserId());
      ps.setString(2, wdkModel.getProjectId());
      rs = ps.executeQuery();
      while (rs.next()) {
        String rcName = rs.getString(COLUMN_RECORD_CLASS);
        int size = rs.getInt("record_size");

        RecordClass recordClass = recordClasses.get(rcName);
        if (recordClass != null) {
          counts.put(recordClass, size);
        } else {
          logger.info("Basket is disabled on record class [" + rcName
              + "], but user #" + user.getUserId()
              + " has basket entries on it.");
        }
      }
    } catch (SQLException e) {
      throw new WdkModelException("Cannot retrieve basket counts for user "
          + user.getEmail(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }
    return counts;
  }

  public int getBasketCounts(User user, List<String[]> records,
      RecordClass recordClass) throws WdkModelException {
    int userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    String sqlCount = "SELECT count(*) FROM " + schema + TABLE_BASKET
        + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
        + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psCount = null;
    try {
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      int basketCount = 0;
      for (String[] row : records) {
        // fill or truncate the pk columns
        String[] pkValue = new String[pkColumns.length];
        int length = Math.min(row.length, pkValue.length);
        System.arraycopy(row, 0, pkValue, 0, length);

        // check if the record already exists.
        setParams(psCount, userId, projectId, rcName, pkValue);
        boolean hasRecord = false;
        ResultSet resultSet = null;
        try {
          long start = System.currentTimeMillis();
          resultSet = psCount.executeQuery();
          QueryLogger.logEndStatementExecution(sqlCount,
              "wdk-basket-factory-count", start);
          if (resultSet.next()) {
            int rsCount = resultSet.getInt(1);
            hasRecord = (rsCount > 0);
          }
        } finally {
          if (resultSet != null) SqlUtils.closeResultSetOnly(resultSet);
        }
        if (hasRecord) {
          basketCount++;
        }
      }
      return basketCount;
    } catch (SQLException e) {
      throw new WdkModelException("Could not get basket counts for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeStatement(psCount);
    }
  }

  public String getBasket(User user, RecordClass recordClass)
      throws SQLException {
    String sql = "SELECT * FROM " + schema + TABLE_BASKET + " WHERE "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_USER_ID + " = ? AND "
        + COLUMN_RECORD_CLASS + " =?";
    DataSource ds = wdkModel.getUserDb().getDataSource();
    ResultSet rs = null;
    try {
      long start = System.currentTimeMillis();
      PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setFetchSize(100);
      ps.setString(1, wdkModel.getProjectId());
      ps.setInt(2, user.getUserId());
      ps.setString(3, recordClass.getFullName());
      rs = ps.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-basket-factory-select-all", start);

      StringBuffer buffer = new StringBuffer();
      PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
      String[] columns = pkField.getColumnRefs();
      while (rs.next()) {
        if (buffer.length() > 0) buffer.append(DatasetFactory.RECORD_DIVIDER);

        Map<String, Object> columnValues = new LinkedHashMap<String, Object>();
        for (int i = 1; i <= columns.length; i++) {
          Object columnValue = rs.getObject(Utilities.COLUMN_PK_PREFIX + i);
          columnValues.put(columns[i - 1], columnValue);

          // cannot use primary key value to format the output,
          // otherwise we might loose information
          if (i > 1) buffer.append(DatasetFactory.COLUMN_DIVIDER);
          buffer.append(columnValue);
        }

        // format the basket with a primary key value stub

        // PrimaryKeyAttributeValue pkValue = new
        // PrimaryKeyAttributeValue(
        // pkField, columnValues);
        // buffer.append(pkValue.getValue());
      }
      return buffer.toString();
    } finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }
  }

  /**
   * the method has to be called before the recordClasses are resolved.
   * 
   * @param recordClass
   */
  public void createSnapshotBasketQuestion(RecordClass recordClass)
      throws WdkModelException {
    // check if the basket question already exists
    String qname = recordClass.getFullName().replace('.', '_')
        + SNAPSHOT_BASKET_QUESTION_SUFFIX;
    QuestionSet questionSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET);
    if (questionSet.contains(qname)) return;

    String rcName = recordClass.getDisplayName();
    Question question = new Question();
    question.setName(qname);
    question.setDisplayName("Copy of " + rcName + " Basket");
    question.setShortDisplayName("Copy of Basket");
    question.setRecordClass(recordClass);
    Query query = getBasketSnapshotIdQuery(recordClass);
    question.setQuery(query);
    questionSet.addQuestion(question);
    question.excludeResources(wdkModel.getProjectId());
  }

  private Query getBasketSnapshotIdQuery(RecordClass recordClass)
      throws WdkModelException {
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();

    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

    // check if the boolean query already exists
    String queryName = rcName.replace('.', '_')
        + SNAPSHOT_BASKET_ID_QUERY_SUFFIX;
    QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    if (querySet.contains(queryName)) return querySet.getQuery(queryName);

    SqlQuery query = new SqlQuery();
    query.setName(queryName);
    // create columns
    for (String columnName : pkColumns) {
      Column column = new Column();
      column.setName(columnName);
      query.addColumn(column);
    }
    // create params
    DatasetParam datasetParam = getDatasetParam(recordClass);
    query.addParam(datasetParam);

    // make sure we create index on primary keys
    query.setIndexColumns(pkColumns);
    query.setDoNotTest(true);
    query.setIsCacheable(true);

    // construct the sql
    StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
    for (int i = 0; i < pkColumns.length; i++) {
      if (i > 0) sql.append(", ");
      sql.append(pkColumns[i]);
    }
    sql.append(" FROM ($$" + datasetParam.getName() + "$$)");
    query.setSql(sql.toString());
    querySet.addQuery(query);
    query.excludeResources(projectId);
    return query;
  }

  private DatasetParam getDatasetParam(RecordClass recordClass)
      throws WdkModelException {
    String rcName = recordClass.getFullName();
    String paramName = rcName.replace('.', '_') + PARAM_DATASET_SUFFIX;
    ParamSet paramSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    if (paramSet.contains(paramName))
      return (DatasetParam) paramSet.getParam(paramName);

    DatasetParam param = new DatasetParam();
    param.setName(paramName);
    param.setId(paramName);
    param.setAllowEmpty(false);
    param.setRecordClassRef(rcName);
    param.setRecordClass(recordClass);
    param.setPrompt(recordClass.getDisplayNamePlural() + " from");
    param.setDefaultType(DatasetParam.TYPE_BASKET);
    param.setAllowEmpty(false);
    paramSet.addParam(param);
    param.excludeResources(wdkModel.getProjectId());
    return param;
  }

  /**
   * the method has to be called before the recordClasses are resolved.
   * 
   * @param recordClass
   */
  public void createRealtimeBasketQuestion(RecordClass recordClass)
      throws WdkModelException {
    // check if the basket question already exists
    String qname = recordClass.getFullName().replace('.', '_')
        + REALTIME_BASKET_QUESTION_SUFFIX;
    QuestionSet questionSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET);
    if (questionSet.contains(qname)) return;

    String rcName = recordClass.getDisplayName();
    Question question = new Question();
    question.setName(qname);
    question.setDisplayName("Current " + rcName + " Basket");
    question.setShortDisplayName(rcName + " Basket");
    question.setRecordClass(recordClass);
    Query query = getBasketRealtimeIdQuery(recordClass);
    question.setQuery(query);
    questionSet.addQuestion(question);
    question.excludeResources(wdkModel.getProjectId());
  }

  private Query getBasketRealtimeIdQuery(RecordClass recordClass)
      throws WdkModelException {
    String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();

    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

    // check if the boolean query already exists
    String queryName = rcName.replace('.', '_')
        + REALTIME_BASKET_ID_QUERY_SUFFIX;
    QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    if (querySet.contains(queryName)) return querySet.getQuery(queryName);

    SqlQuery query = new SqlQuery();
    query.setName(queryName);
    // create columns
    for (String columnName : pkColumns) {
      Column column = new Column();
      column.setName(columnName);
      query.addColumn(column);
    }
    // create params
    query.addParam(getSignatureParam());

    // make sure we create index on primary keys
    query.setIndexColumns(pkColumns);
    query.setDoNotTest(true);
    query.setIsCacheable(false);

    // construct the sql
    StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
    for (int i = 0; i < pkColumns.length; i++) {
      if (i > 0) sql.append(", ");
      sql.append("b." + Utilities.COLUMN_PK_PREFIX + (i + 1));
      sql.append(" AS " + pkColumns[i]);
    }
    sql.append(" FROM " + schema + TABLE_BASKET + dbLink + " b, ");
    sql.append(schema + UserFactory.TABLE_USER + dbLink + " u ");
    sql.append(" WHERE b." + COLUMN_USER_ID + " = u." + COLUMN_USER_ID);
    sql.append(" AND u." + UserFactory.COLUMN_SIGNATURE + " = $$"
        + PARAM_USER_SIGNATURE + "$$ ");
    sql.append(" AND b." + COLUMN_PROJECT_ID + " = '" + projectId + "'");
    sql.append(" AND b." + COLUMN_RECORD_CLASS + " = '" + rcName + "'");
    query.setSql(sql.toString());
    querySet.addQuery(query);
    query.excludeResources(projectId);
    return query;
  }

  private Param getSignatureParam() throws WdkModelException {
    ParamSet paramSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    if (paramSet.contains(PARAM_USER_SIGNATURE))
      return paramSet.getParam(PARAM_USER_SIGNATURE);

    StringParam param = new StringParam();
    param.setName(PARAM_USER_SIGNATURE);
    param.setAllowEmpty(false);
    param.setId(PARAM_USER_SIGNATURE);
    param.setNumber(false);
    param.setVisible(false);
    paramSet.addParam(param);
    param.excludeResources(wdkModel.getProjectId());
    return param;
  }

  /**
   * the method has to be called before the recordClasses are resolved.
   * 
   * @param recordClass
   * @return
   */
  public void createBasketAttributeQuery(RecordClass recordClass)
      throws WdkModelException {
    String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();

    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

    // check if the boolean query already exists
    String queryName = rcName.replace('.', '_') + BASKET_ATTRIBUTE_QUERY_SUFFIX;
    QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    if (querySet.contains(queryName)) return;

    SqlQuery query = new SqlQuery();
    query.setName(queryName);

    // create columns
    for (String columnName : pkColumns) {
      Column column = new Column();
      column.setName(columnName);
      query.addColumn(column);
    }
    Column column = new Column();
    column.setName(BASKET_ATTRIBUTE);
    query.addColumn(column);

    // make sure we create index on primary keys
    query.setIndexColumns(pkColumns);
    query.setDoNotTest(true);
    query.setIsCacheable(false); // cache the boolean query

    String prefix = Utilities.COLUMN_PK_PREFIX;

    // construct the sql
    StringBuilder sql = new StringBuilder("SELECT ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append("i." + pkColumns[i] + ", ");
    }
    // case clause works for both Oracle & PostreSQL
    sql.append("(CASE WHEN b." + prefix + "1 IS NULL THEN 0 ELSE 1 END) ");
    sql.append(" AS " + BASKET_ATTRIBUTE);
    sql.append(" FROM (##WDK_ID_SQL##) i ");
    sql.append(" LEFT JOIN " + schema + TABLE_BASKET + dbLink + " b ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append((i == 0) ? " ON " : " AND ");
      sql.append(" i." + pkColumns[i] + " = b." + prefix + (i + 1));
    }
    sql.append(" AND b." + COLUMN_USER_ID + " = $$" + Utilities.PARAM_USER_ID
        + "$$ ");
    sql.append(" AND b." + COLUMN_PROJECT_ID + " = '" + projectId + "'");
    sql.append(" AND b." + COLUMN_RECORD_CLASS + " = '" + rcName + "'");

    query.setSql(sql.toString());
    querySet.addQuery(query);
    query.excludeResources(projectId);
  }

  /**
   * this method has to be called before resolving the mdoel.
   * 
   * @param recordClass
   */
  public void createAttributeQueryRef(RecordClass recordClass)
      throws WdkModelException {
    String rcName = recordClass.getFullName();
    String queryName = Utilities.INTERNAL_QUERY_SET + "."
        + rcName.replace('.', '_') + BASKET_ATTRIBUTE_QUERY_SUFFIX;

    ColumnAttributeField attribute = new ColumnAttributeField();
    attribute.setName(BASKET_ATTRIBUTE);
    attribute.setDisplayName("In Basket");
    attribute.setInternal(true);
    attribute.setInReportMaker(false);
    attribute.setSortable(true);

    AttributeQueryReference reference = new AttributeQueryReference();
    reference.setRef(queryName);
    reference.addAttributeField(attribute);
    recordClass.addAttributesQueryRef(reference);
    reference.excludeResources(wdkModel.getProjectId());
  }

  private void setParams(PreparedStatement ps, int userId, String projectId,
      String rcName, String[] pkValue) throws SQLException {
    ps.setInt(1, userId);
    ps.setString(2, projectId);
    ps.setString(3, rcName);
    for (int i = 0; i < pkValue.length; i++) {
      ps.setString(i + 4, pkValue[i]);
    }
  }

  /**
   * The method is used to address out-dated db-link issue with Oracle. The
   * solution is suggested by Oracle support, that: " Since clocks are
   * synchronized at the end of a remote query, precede each remote query with a
   * dummy remote query to the same site (such as select * from dual@remote)."
   */
  private void checkRemoteTable() throws SQLException {
    String dblink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
    StringBuilder sql = new StringBuilder("SELECT count(*) FROM ");
    sql.append(schema).append(TABLE_BASKET).append(dblink);

    // execute this dummy sql to make sure the remote table is sync-ed.
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    SqlUtils.executeScalar(dataSource, sql.toString(),
        "wdk-remote-basket-dummy");
  }
}
