/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeQueryReference;
import org.gusdb.wdk.model.ColumnAttributeField;
import org.gusdb.wdk.model.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParamSet;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;

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

    static final String TABLE_BASKET = "user_baskets";
    static final String COLUMN_USER_ID = "user_id";
    static final String COLUMN_PROJECT_ID = "project_id";
    static final String COLUMN_RECORD_CLASS = "record_class";

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

    public void addToBasket(User user, Step step)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
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
     *            a list of primary key values. the inner map is a primary-key
     *            column-value map.
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public void addToBasket(User user, RecordClass recordClass,
            List<String[]> pkValues) throws SQLException, WdkUserException,
            WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sqlInsert = "INSERT INTO " + schema + TABLE_BASKET + " ("
                + COLUMN_USER_ID + ", " + COLUMN_PROJECT_ID + ", "
                + COLUMN_RECORD_CLASS;
        String sqlValues = "";
        String sqlCount = "SELECT count(*) FROM " + schema + TABLE_BASKET
                + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
                + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sqlInsert += ", " + Utilities.COLUMN_PK_PREFIX + i;
            sqlValues += ", ?";
            sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }
        sqlInsert += ") VALUES (?, ?, ?" + sqlValues + ")";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
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
                    SqlUtils.verifyTime(wdkModel, sqlCount,
                            "wdk-basket-factory-count", start);
                    if (resultSet.next()) {
                        int rsCount = resultSet.getInt(1);
                        hasRecord = (rsCount > 0);
                    }
                } finally {
                    if (resultSet != null)
                        resultSet.close();
                }
                if (hasRecord)
                    continue;

                // insert new record
                setParams(psInsert, userId, projectId, rcName, pkValue);
                psInsert.addBatch();

                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psInsert.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sqlInsert,
                            "wdk-basket-factory-insert", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psInsert.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlInsert,
                        "wdk-basket-factory-insert", start);
            }
        } finally {
            SqlUtils.closeStatement(psInsert);
            SqlUtils.closeStatement(psCount);
        }
    }

    public void removeFromBasket(User user, Step step)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        AnswerValue answerValue = step.getAnswerValue();
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        List<String[]> pkValues = answerValue.getAllIds();
        removeFromBasket(user, recordClass, pkValues);
    }

    public void removeFromBasket(User user, RecordClass recordClass,
            List<String[]> pkValues) throws SQLException, WdkUserException,
            WdkModelException {
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

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
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
                    SqlUtils.verifyTime(wdkModel, sqlDelete,
                            "wdk-basket-factory-delete", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psDelete.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlDelete,
                        "wdk-basket-factory-delete", start);
            }
        } finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public void clearBasket(User user, RecordClass recordClass)
            throws SQLException, WdkUserException, WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String sqlDelete = "DELETE FROM " + schema + TABLE_BASKET + " WHERE "
                + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_RECORD_CLASS + " = ?";

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psDelete = null;
        try {
            long start = System.currentTimeMillis();
            psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
            psDelete.setInt(1, userId);
            psDelete.setString(2, projectId);
            psDelete.setString(3, rcName);
            psDelete.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sqlDelete,
                    "wdk-basket-factory-delete-all", start);
        } finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public Map<String, Integer> getBasketCounts(User user) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
            for (RecordClass recordClass : rcSet.getRecordClasses()) {
                if (recordClass.hasBasket())
                    counts.put(recordClass.getFullName(), 0);
            }
        }
        // load the unique counts
        String sql = "SELECT " + COLUMN_RECORD_CLASS
                + ", count(*) AS record_size "
                + " FROM (SELECT DISTINCT * FROM " + schema + TABLE_BASKET
                + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID
                + " = ?) t " + " GROUP BY " + COLUMN_RECORD_CLASS;
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setInt(1, user.getUserId());
            ps.setString(2, wdkModel.getProjectId());
            rs = ps.executeQuery();
            while (rs.next()) {
                String recordClass = rs.getString(COLUMN_RECORD_CLASS);
                int size = rs.getInt("record_size");
                counts.put(recordClass, size);
            }
        } finally {
            SqlUtils.closeResultSet(rs);
        }
        return counts;
    }

    public String getBasket(User user, RecordClass recordClass)
            throws WdkUserException, WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException {
        String sql = "SELECT * FROM " + schema + TABLE_BASKET + " WHERE "
                + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_USER_ID
                + " = ? AND " + COLUMN_RECORD_CLASS + " =?";
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setFetchSize(100);
            ps.setString(1, wdkModel.getProjectId());
            ps.setInt(2, user.getUserId());
            ps.setString(3, recordClass.getFullName());
            rs = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-basket-factory-select-all",
                    start);

            StringBuffer buffer = new StringBuffer();
            PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
            String[] columns = pkField.getColumnRefs();
            while (rs.next()) {
                if (buffer.length() > 0)
                    buffer.append(DatasetFactory.RECORD_DIVIDER);

                Map<String, Object> columnValues = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= columns.length; i++) {
                    Object columnValue = rs.getObject(Utilities.COLUMN_PK_PREFIX
                            + i);
                    columnValues.put(columns[i - 1], columnValue);

                    // cannot use primary key value to format the output,
                    // otherwise we might loose information
                    if (i > 1)
                        buffer.append(DatasetFactory.COLUMN_DIVIDER);
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
            SqlUtils.closeResultSet(rs);
        }
    }

    /**
     * get deprecated records by comparing stored ids with all id query
     * 
     * @param recordClass
     * @return
     * @throws WdkModelException
     * @throws SQLException
     */
    public List<PrimaryKeyAttributeValue> getDeprecatedRecords(User user,
            RecordClass recordClass) throws WdkModelException, SQLException {
        List<PrimaryKeyAttributeValue> invalidRecords = new ArrayList<PrimaryKeyAttributeValue>();

        // get all id query from record class
        String queryRef = recordClass.getAllRecordsQueryRef();

        // if no all records query is defined, then basket is disabled to that
        // record class type, and no invalid record either.
        if (queryRef == null)
            return invalidRecords;

        SqlQuery idQuery = (SqlQuery) wdkModel.resolveReference(queryRef);
        String allIdsSql = idQuery.getSql();
        String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
        PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
        String[] pkColumns = pkField.getColumnRefs();

        // use basket sql left join with all id query, and filter by the nulls
        // from id query
        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < pkColumns.length; i++) {
            if (i > 0)
                sql.append(", ");
            sql.append("bq." + Utilities.COLUMN_PK_PREFIX + i + " AS "
                    + pkColumns[i]);
        }
        sql.append(" FROM " + schema + TABLE_BASKET + dbLink + " bq ");
        sql.append(" LEFT JOIN  (" + allIdsSql + ") iq ON ");
        for (int i = 0; i < pkColumns.length; i++) {
            if (i > 0)
                sql.append(" AND ");
            sql.append("bq." + pkColumns[i] + " = iq." + pkColumns[i]);
        }
        sql.append(" WHERE iq." + pkColumns[0] + " IS NULL ");
        sql.append(" AND bq." + COLUMN_USER_ID + " = ? ");
        sql.append(" AND bq." + COLUMN_PROJECT_ID + " = ? ");
        sql.append(" AND bq." + COLUMN_RECORD_CLASS + " = ? ");

        ResultSet resultSet = null;
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        try {
            PreparedStatement psSelect = SqlUtils.getPreparedStatement(
                    dataSource, sql.toString());
            psSelect.setInt(1, user.getUserId());
            psSelect.setString(2, wdkModel.getProjectId());
            psSelect.setString(3, recordClass.getFullName());
            resultSet = psSelect.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
                for (String pkColumn : pkColumns) {
                    pkValues.put(pkColumn, resultSet.getObject(pkColumn));
                }
                PrimaryKeyAttributeValue pkValue = new PrimaryKeyAttributeValue(
                        pkField, pkValues);
                invalidRecords.add(pkValue);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        return invalidRecords;
    }

    /**
     * the method has to be called before the recordClasses are resolved.
     * 
     * @param recordClass
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    public void createSnapshotBasketQuestion(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // check if the basket question already exists
        String qname = recordClass.getFullName().replace('.', '_')
                + SNAPSHOT_BASKET_QUESTION_SUFFIX;
        QuestionSet questionSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET);
        if (questionSet.contains(qname))
            return;

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
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();

        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // check if the boolean query already exists
        String queryName = rcName.replace('.', '_')
                + SNAPSHOT_BASKET_ID_QUERY_SUFFIX;
        QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
        if (querySet.contains(queryName))
            return querySet.getQuery(queryName);

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
        String queryRef = recordClass.getAllRecordsQueryRef();
        SqlQuery idQuery = (SqlQuery) wdkModel.resolveReference(queryRef);
        String allRecordsSql = idQuery.getSql();
        String sql = "SELECT DISTINCT ";
        for (int i = 1; i <= pkColumns.length; i++) {
            if (i > 1)
                sql += ", ";
            sql += "d." + pkColumns[i - 1];
        }
        sql += " FROM ($$" + datasetParam.getName() + "$$) d, ("
                + allRecordsSql + ") i ";
        for (int i = 0; i < pkColumns.length; i++) {
            sql += (i == 0) ? " WHERE " : " AND ";
            sql += "d." + pkColumns[i] + " = i." + pkColumns[i];
        }
        query.setSql(sql);
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
        param.setPrompt(recordClass.getType() + "s from");
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
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkUserException
     */
    public void createRealtimeBasketQuestion(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // check if the basket question already exists
        String qname = recordClass.getFullName().replace('.', '_')
                + REALTIME_BASKET_QUESTION_SUFFIX;
        QuestionSet questionSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET);
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
        question.excludeResources(wdkModel.getProjectId());
    }

    private Query getBasketRealtimeIdQuery(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();

        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // check if the boolean query already exists
        String queryName = rcName.replace('.', '_')
                + REALTIME_BASKET_ID_QUERY_SUFFIX;
        QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
        if (querySet.contains(queryName))
            return querySet.getQuery(queryName);

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
        String queryRef = recordClass.getAllRecordsQueryRef();
        SqlQuery idQuery = (SqlQuery) wdkModel.resolveReference(queryRef);
        String allRecordsSql = idQuery.getSql();
        String sql = "";
        for (int i = 1; i <= pkColumns.length; i++) {
            if (sql.length() == 0)
                sql = "SELECT DISTINCT ";
            else
                sql += ", ";
            sql += "b." + Utilities.COLUMN_PK_PREFIX + i + " AS "
                    + pkColumns[i - 1];
        }
        sql += " FROM " + schema + TABLE_BASKET + dbLink + " b, " + schema
                + UserFactory.TABLE_USER + dbLink + " u, (" + allRecordsSql
                + ") i WHERE b." + COLUMN_USER_ID + " = u."
                + Utilities.COLUMN_USER_ID + " AND u."
                + UserFactory.COLUMN_SIGNATURE + " = $$" + PARAM_USER_SIGNATURE
                + "$$ AND b." + COLUMN_PROJECT_ID + " = '" + projectId
                + "' AND b." + COLUMN_RECORD_CLASS + " = '" + rcName + "'";
        // force the primary key column to be string, so that it can join with
        // string columns in basket.
        for (int i = 1; i <= pkColumns.length; i++) {
            sql += " AND b." + Utilities.COLUMN_PK_PREFIX + i;
            sql += "     = i." + pkColumns[i - 1];
        }
        query.setSql(sql);
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
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public void createBasketAttributeQuery(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();

        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // check if the boolean query already exists
        String queryName = rcName.replace('.', '_')
                + BASKET_ATTRIBUTE_QUERY_SUFFIX;
        QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
        if (querySet.contains(queryName))
            return;

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

        // construct the sql
        String queryRef = recordClass.getAllRecordsQueryRef();
        SqlQuery idQuery = (SqlQuery) wdkModel.resolveReference(queryRef);
        String allRecordsSql = idQuery.getSql();
        String sql = "SELECT ";
        for (int i = 1; i <= pkColumns.length; i++) {
            sql += "i." + pkColumns[i - 1] + ", ";
        }
        // case clause works for both Oracle & PostreSQL
        sql += "(CASE WHEN b." + Utilities.COLUMN_PK_PREFIX
                + "1 IS NULL THEN 0 ELSE 1 END) " + " AS " + BASKET_ATTRIBUTE;
        sql += " FROM (" + allRecordsSql + ") i LEFT OUTER JOIN " + schema
                + TABLE_BASKET + dbLink + " b ON ";
        for (int i = 1; i <= pkColumns.length; i++) {
            sql += " i." + pkColumns[i - 1] + " = b."
                    + Utilities.COLUMN_PK_PREFIX + i + " AND ";
        }
        sql += " b." + COLUMN_USER_ID + " = $$" + Utilities.PARAM_USER_ID
                + "$$ AND b." + COLUMN_PROJECT_ID + " = '" + projectId
                + "' AND b." + COLUMN_RECORD_CLASS + " = '" + rcName + "'";

        query.setSql(sql);
        querySet.addQuery(query);
        query.excludeResources(projectId);
    }

    /**
     * this method has to be called before resolving the mdoel.
     * 
     * @param recordClass
     * @throws WdkModelException
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

    public int exportBasket(User user, String targetProject, String rcName)
            throws SQLException, WdkUserException, WdkModelException {
        String table = schema + TABLE_BASKET;
        String prefix = Utilities.COLUMN_PK_PREFIX;
        String pkColumns = prefix + "1, " + prefix + "2, " + prefix + "3 ";
        String projectId = wdkModel.getProjectId();
        int userId = user.getUserId();

        String selectClause = "SELECT " + COLUMN_USER_ID + ", "
                + COLUMN_RECORD_CLASS + ", " + pkColumns + " FROM " + table
                + " WHERE " + COLUMN_USER_ID + " = ? AND "
                + COLUMN_RECORD_CLASS + " = ? AND " + COLUMN_PROJECT_ID
                + " = ? ";

        StringBuilder sql = new StringBuilder("INSERT INTO " + table + " (");
        sql.append(COLUMN_USER_ID + ", " + COLUMN_RECORD_CLASS + ", ");
        sql.append(COLUMN_PROJECT_ID + ", " + pkColumns + ") ");
        sql.append(" SELECT " + COLUMN_USER_ID + ", " + COLUMN_RECORD_CLASS);
        sql.append(", ? AS " + COLUMN_PROJECT_ID + ", " + pkColumns);
        sql.append(" FROM (" + selectClause + " MINUS " + selectClause + ")");
        
        logger.debug(sql);

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psInsert = null;
        int count = 0;
        try {
            psInsert = SqlUtils.getPreparedStatement(dataSource, sql.toString());
            psInsert.setString(1, targetProject);
            psInsert.setInt(2, userId);
            psInsert.setString(3, rcName);
            psInsert.setString(4, projectId);
            psInsert.setInt(5, userId);
            psInsert.setString(6, rcName);
            psInsert.setString(7, targetProject);

            long start = System.currentTimeMillis();
            count = psInsert.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sql.toString(), "wdk-export-basket",
                    start);
        } finally {
            SqlUtils.closeStatement(psInsert);
        }
        return count;
    }
}
