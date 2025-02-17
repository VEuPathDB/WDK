package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.db.stream.ResultSets;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.stream.PrimaryKeyRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStreamFactory;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.RecordNotFoundException;
import org.gusdb.wdk.model.record.StaticRecordInstance;

public class BasketFactory {

  public static final String REALTIME_BASKET_QUESTION_SUFFIX = "ByRealtimeBasket";
  public static final String SNAPSHOT_BASKET_QUESTION_SUFFIX = "BySnapshotBasket";
  private static final String REALTIME_BASKET_ID_QUERY_SUFFIX = "ByRealtimeBasket";
  static final String SNAPSHOT_BASKET_ID_QUERY_SUFFIX = "BySnapshotBasket";

  public static final String PARAM_USER_SIGNATURE = "user_signature";
  public static final String PARAM_DATASET_SUFFIX = "Dataset";

  public static final String TABLE_BASKET = "user_baskets";
  public static final String COLUMN_BASKET_ID = "basket_id";
  public static final String COLUMN_USER_ID = "user_id";
  public static final String COLUMN_PROJECT_ID = "project_id";
  public static final String COLUMN_RECORD_CLASS = "record_class";
  public static final String COLUMN_UNIQUE_ID = "pk_column_1";

  private static final Logger LOG = Logger.getLogger(BasketFactory.class);

  private final WdkModel _wdkModel;
  private final String _userSchema;

  public BasketFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  public void addEntireResultToBasket(User user, RunnableObj<AnswerSpec> spec) throws WdkModelException {
    AnswerValue answer = AnswerValueFactory.makeAnswer(spec);
    int recordCount = answer.getResultSizeFactory().getResultSize();
    try (RecordStream records = RecordStreamFactory.getRecordStream(answer, Collections.EMPTY_LIST, Collections.EMPTY_LIST)) {
      addToBasket(user, spec.get().getQuestion().get().getRecordClass(), recordCount, records);
    }
  }

  public void addPksToBasket(User user, RecordClass recordClass, Collection<PrimaryKeyValue> recordsToAdd) throws WdkModelException {
    addToBasket(user, recordClass, recordsToAdd.size(), new PrimaryKeyRecordStream(user, recordClass, recordsToAdd));
  }

  public void removePksFromBasket(User user, RecordClass recordClass, Collection<PrimaryKeyValue> recordsToDelete) throws WdkModelException {
    removeFromBasket(user, recordClass, PrimaryKeyValue.toStringArrays(recordsToDelete));
  }

  /**
   * @param user
   * @param recordClass
   * @param recordCount
   * @param records a stream of records containing primary key values. the inner map is a primary-key column-value map
   * @throws WdkModelException
   */
  public void addToBasket(User user, RecordClass recordClass, int recordCount, RecordStream records)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlInsert = "INSERT INTO " + _userSchema + TABLE_BASKET + " (" + COLUMN_BASKET_ID + ", " +
        COLUMN_USER_ID + ", " + COLUMN_PROJECT_ID + ", " + COLUMN_RECORD_CLASS;
    String sqlValues = "";
    String sqlCount = "SELECT count(*) FROM " + _userSchema + TABLE_BASKET + " WHERE " + COLUMN_USER_ID +
        "= ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlInsert += ", " + Utilities.COLUMN_PK_PREFIX + i;
      sqlValues += ", ?";
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    sqlInsert += ") VALUES (?, ?, ?, ?" + sqlValues + ")";
    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    DBPlatform platform = _wdkModel.getUserDb().getPlatform();
    PreparedStatement psInsert = null, psCount = null;
    try {
      List<Long> basketRecordIds = platform.getNextNIds(dataSource, _userSchema, TABLE_BASKET, recordCount);
      psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      int count = 0;
      for (RecordInstance record : records) {
        // get pkValues out of record
        String[] pkValue = record.getPrimaryKey().getValues().values().toArray(new String[pkColumns.length]);

        // check if the record already exists.
        setParams(psCount, userId, projectId, rcName, pkValue);
        boolean hasRecord = false;
        ResultSet resultSet = null;
        try {
          long start = System.currentTimeMillis();
          resultSet = psCount.executeQuery();
          QueryLogger.logEndStatementExecution(sqlCount, "wdk-basket-factory-count", start);
          if (resultSet.next()) {
            int rsCount = resultSet.getInt(1);
            hasRecord = (rsCount > 0);
          }
        }
        finally {
          if (resultSet != null)
            SqlUtils.closeResultSetOnly(resultSet);
        }
        if (hasRecord)
          continue;

        // insert new record
        long basketId = basketRecordIds.get(count);
        psInsert.setLong(1, basketId);
        psInsert.setLong(2, userId);
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
          QueryLogger.logEndStatementExecution(sqlInsert, "wdk-basket-factory-insert", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psInsert.executeBatch();
        QueryLogger.logEndStatementExecution(sqlInsert, "wdk-basket-factory-insert", start);
      }
      // check the remote table to solve out-dated db-link issue with Oracle
      checkRemoteTable();
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeStatement(psInsert);
      SqlUtils.closeStatement(psCount);
    }
  }

  public void removeFromBasket(User user, RecordClass recordClass, List<String[]> pkValues)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlDelete = "DELETE FROM " + _userSchema + TABLE_BASKET + " WHERE " + COLUMN_USER_ID + "= ? AND " +
        COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlDelete += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }

    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
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
          QueryLogger.logEndStatementExecution(sqlDelete, "wdk-basket-factory-delete", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psDelete.executeBatch();
        QueryLogger.logEndStatementExecution(sqlDelete, "wdk-basket-factory-delete", start);
      }
      // check the remote table to solve out-dated db-link issue with
      // Oracle.
      checkRemoteTable();
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  public void clearBasket(User user, RecordClass recordClass) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String sqlDelete = "DELETE FROM " + _userSchema + TABLE_BASKET + " WHERE " + COLUMN_USER_ID + "= ? AND " +
        COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";

    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psDelete = null;
    try {
      long start = System.currentTimeMillis();
      psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
      psDelete.setLong(1, userId);
      psDelete.setString(2, projectId);
      psDelete.setString(3, rcName);
      psDelete.executeUpdate();
      QueryLogger.logEndStatementExecution(sqlDelete, "wdk-basket-factory-delete-all", start);

      // check the remote table to solve out-dated db-link issue with Oracle
      checkRemoteTable();
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to clear basket " + recordClass.getFullName() + " for user " + user.getUserId());
    }
    finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  public Map<RecordClass, Integer> getBasketCounts(User user) throws WdkModelException {
    Map<RecordClass, Integer> counts = new LinkedHashMap<RecordClass, Integer>();
    Map<String, RecordClass> recordClasses = new LinkedHashMap<String, RecordClass>();
    for (RecordClassSet rcSet : _wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : rcSet.getRecordClasses()) {
        if (recordClass.isUseBasket()) {
          counts.put(recordClass, 0);
          recordClasses.put(recordClass.getFullName(), recordClass);
        }
      }
    }
    // load the unique counts
    String sql = "SELECT " + COLUMN_RECORD_CLASS + ", count(*) AS record_size " + " FROM (SELECT " +
        COLUMN_UNIQUE_ID + "," + COLUMN_RECORD_CLASS + " FROM " + _userSchema + TABLE_BASKET + " WHERE " +
        COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? GROUP BY " + COLUMN_UNIQUE_ID + "," +
        COLUMN_RECORD_CLASS + " ) t " + " GROUP BY " + COLUMN_RECORD_CLASS;
    DataSource ds = _wdkModel.getUserDb().getDataSource();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setLong(1, user.getUserId());
      ps.setString(2, _wdkModel.getProjectId());
      rs = ps.executeQuery();
      while (rs.next()) {
        String rcName = rs.getString(COLUMN_RECORD_CLASS);
        int size = rs.getInt("record_size");

        RecordClass recordClass = recordClasses.get(rcName);
        if (recordClass != null && size > 0) {
          counts.put(recordClass, size);
        }
        else {
          LOG.info("Basket is disabled on record class [" + rcName + "], but user #" + user.getUserId() +
              " has basket entries on it.");
        }
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Cannot retrieve basket counts for user " + user.getEmail(), e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs, ps);
    }
    return counts;
  }

  public int getBasketCounts(User user, List<String[]> records, RecordClass recordClass) throws WdkModelException {
    return reduce(queryBasketStatus(user, records, recordClass), (count, next) -> count + (next ? 1 : 0), 0);
  }

  public List<Boolean> queryBasketStatus(User user, RecordClass recordClass, List<PrimaryKeyValue> pksToQuery) throws WdkModelException {
    return queryBasketStatus(user, PrimaryKeyValue.toStringArrays(pksToQuery), recordClass);
  }

  public List<Boolean> queryBasketStatus(User user, List<String[]> records, RecordClass recordClass) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlCount = "SELECT count(*) FROM " + _userSchema + TABLE_BASKET + " WHERE " + COLUMN_USER_ID +
        "= ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psCount = null;
    List<Boolean> basketStatuses = new ArrayList<>();
    try {
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      for (String[] row : records) {
        // fill or truncate the pk columns
        String[] pkValue = new String[pkColumns.length];
        int length = Math.min(row.length, pkValue.length);
        System.arraycopy(row, 0, pkValue, 0, length);

        // check if the record exists in this basket
        setParams(psCount, userId, projectId, rcName, pkValue);
        ResultSet resultSet = null;
        try {
          long start = System.currentTimeMillis();
          resultSet = psCount.executeQuery();
          QueryLogger.logEndStatementExecution(sqlCount, "wdk-basket-factory-count", start);
          if (!resultSet.next()) {
            throw new WdkModelException("Basket count query for a single record did not return result.");
          }
          int rsCount = resultSet.getInt(1);
          if (rsCount > 1) {
            throw new WdkModelException(">1 record in " + rcName + " basket with IDs " + FormatUtil.join(pkValue, ", "));
          }
          basketStatuses.add(rsCount == 1);
        }
        finally {
          if (resultSet != null) {
            SqlUtils.closeResultSetOnly(resultSet);
          }
        }
      }
      return basketStatuses;
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not get basket counts for user " + user.getUserId(), e);
    }
    finally {
      SqlUtils.closeStatement(psCount);
    }
  }

  public Stream<RecordInstance> getBasket(User user, RecordClass recordClass) {

    final var sql =
        "SELECT * FROM " + _userSchema + TABLE_BASKET +
        " WHERE " + COLUMN_PROJECT_ID + " = ?" +
        " AND " + COLUMN_USER_ID + " = ?" +
        " AND " + COLUMN_RECORD_CLASS + " =?";

    final var columns = recordClass.getPrimaryKeyDefinition().getColumnRefs();

    return ResultSets.openStream(_wdkModel.getUserDb().getDataSource(), sql,
      // SQL argument values
      new Object[] { _wdkModel.getProjectId(), user.getUserId(), recordClass.getFullName() },
      // SQL argument types
      new Integer[] { Types.VARCHAR, Types.BIGINT, Types.VARCHAR }, rs -> {

        var pkValues = new LinkedHashMap<String, Object>();

        for (int i = 1; i <= columns.length; i++) {
          var columnValue = rs.getObject(Utilities.COLUMN_PK_PREFIX + i);
          pkValues.put(columns[i - 1], columnValue);
        }

        try {
          return Optional.of(new StaticRecordInstance(user, recordClass, recordClass, pkValues, true));
        }
        catch (WdkUserException | RecordNotFoundException e) {
          // FIXME: thrown because pkValues either:
          //    WdkUserException: mapped to more than one record
          //    RecordNotFoundException: did not map to any records
          // Skip both for now but probably want to convert the multiple case
          // IDs to records and add all those records to the result.
          return Optional.empty();
        }
        catch (WdkModelException e) {
          throw new WdkRuntimeException(e);
        }
      }
    );
  }

  public static String getSnapshotBasketQuestionName(RecordClass recordClass) {
    return Utilities.INTERNAL_QUESTION_SET + "." +
        recordClass.getFullName().replace('.', '_') + SNAPSHOT_BASKET_QUESTION_SUFFIX;
  }

  /**
   * the method has to be called before the recordClasses are resolved.
   */
  public void createSnapshotBasketQuestion(RecordClass recordClass) throws WdkModelException {
    // check if the basket question already exists
    String qname = recordClass.getFullName().replace('.', '_') + SNAPSHOT_BASKET_QUESTION_SUFFIX;
    QuestionSet questionSet = _wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET).get();
    if (questionSet.contains(qname))
      return;

    String rcName = recordClass.getDisplayName();
    Question question = new Question();
    question.setName(qname);
    question.setDisplayName("Copy of " + rcName + " Basket");
    question.setShortDisplayName("Copy of Basket");
    question.setRecordClass(recordClass);
    Query query = recordClass.getBasketSnapshotQueryPlugin().getBasketSnapshotIdQuery(_wdkModel);
    question.setQuery(query);
    questionSet.addQuestion(question);
    question.excludeResources(_wdkModel.getProjectId());
  }

  public static String getDatasetParamName(RecordClass recordClass) {
    return recordClass.getFullName().replace('.', '_') + PARAM_DATASET_SUFFIX;
  }

  static DatasetParam getDatasetParam(RecordClass recordClass, WdkModel wdkModel) throws WdkModelException {
    String paramName = getDatasetParamName(recordClass);
    ParamSet paramSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    if (paramSet.contains(paramName))
      return (DatasetParam) paramSet.getParam(paramName);

    DatasetParam param = new DatasetParam();
    param.setName(paramName);
    param.setId(paramName);
    param.setAllowEmpty(false);
    param.setPrompt(recordClass.getDisplayNamePlural() + " from");
    param.setAllowEmpty(false);
    param.setRecordClassRef(recordClass.getFullName());
    paramSet.addParam(param);
    param.excludeResources(wdkModel.getProjectId());
    return param;
  }

  /**
   * the method has to be called before the recordClasses are resolved.
   */
  public void createRealtimeBasketQuestion(RecordClass recordClass) throws WdkModelException {
    // check if the basket question already exists
    String qname = recordClass.getFullName().replace('.', '_') + REALTIME_BASKET_QUESTION_SUFFIX;
    QuestionSet questionSet = _wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET).get();
    if (questionSet.contains(qname))
      return;

    String rcName = recordClass.getDisplayName();
    Question question = new Question();
    question.setName(qname);
    question.setDisplayName("Current " + rcName + " Basket");
    question.setShortDisplayName(rcName + " Basket");
    question.setRecordClass(recordClass);
    Query query = getBasketRealtimeIdQuery(recordClass);
    question.setQuery(query);
    questionSet.addQuestion(question);
    question.excludeResources(_wdkModel.getProjectId());
  }

  private Query getBasketRealtimeIdQuery(RecordClass recordClass) throws WdkModelException {

    // check if the boolean query already exists
    String queryName = recordClass.getFullName().replace('.', '_') + REALTIME_BASKET_ID_QUERY_SUFFIX;
    QuerySet querySet = _wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    if (querySet.contains(queryName))
      return querySet.getQuery(queryName);

    SqlQuery query = new SqlQuery();
    query.setName(queryName);
    // create columns
    for (String columnName : recordClass.getPrimaryKeyDefinition().getColumnRefs()) {
      Column column = new Column();
      column.setName(columnName);
      query.addColumn(column);
    }
    // create params
    query.addParam(Query.getUserParam(_wdkModel));

    // make sure we create index on primary keys
    query.setIndexColumns(recordClass.getIndexColumns());
    query.setDoNotTest(true);
    query.setIsCacheable(false);
    query.setSql(getBasketSelectSql(_wdkModel, recordClass));
    querySet.addQuery(query);
    query.excludeResources(_wdkModel.getProjectId());
    return query;
  }

  public static String getBasketSelectSql(WdkModel wdkModel, RecordClass recordClass) {

    String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();

    StringBuilder sql = new StringBuilder("SELECT DISTINCT ");
    for (int i = 0; i < pkColumns.length; i++) {
      if (i > 0)
        sql.append(", ");
      sql.append("b." + Utilities.COLUMN_PK_PREFIX + (i + 1));
      sql.append(" AS " + pkColumns[i]);
    }
    sql.append(" FROM " + userSchema + TABLE_BASKET + dbLink + " b ");
    sql.append(" WHERE b." + COLUMN_USER_ID + " = " + getUserParamMacro() + " ");
    sql.append("   AND b." + COLUMN_PROJECT_ID + " = '" + wdkModel.getProjectId() + "'");
    sql.append("   AND b." + COLUMN_RECORD_CLASS + " = '" + recordClass.getFullName() + "'");

    return sql.toString();
  }

  public static String getUserParamMacro() {
    return "$$" + Utilities.PARAM_USER_ID + "$$";
  }

  private void setParams(PreparedStatement ps, long userId, String projectId, String rcName, String[] pkValue)
      throws SQLException {
    ps.setLong(1, userId);
    ps.setString(2, projectId);
    ps.setString(3, rcName);
    for (int i = 0; i < pkValue.length; i++) {
      ps.setString(i + 4, pkValue[i]);
    }
  }

  /**
   * The method is used to address out-dated db-link issue with Oracle. The solution is suggested by Oracle
   * support, that: " Since clocks are synchronized at the end of a remote query, precede each remote query
   * with a dummy remote query to the same site (such as select * from dual@remote)."
   */
  private void checkRemoteTable() throws SQLException {
    String dblink = _wdkModel.getModelConfig().getAppDB().getUserDbLink();
    StringBuilder sql = new StringBuilder("SELECT count(*) FROM ");
    sql.append(_userSchema).append(TABLE_BASKET).append(dblink);

    // execute this dummy sql to make sure the remote table is sync-ed.
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    SqlUtils.executeScalar(dataSource, sql.toString(), "wdk-remote-basket-dummy");
  }

}
