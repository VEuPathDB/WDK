/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.axis.i18n.ProjectResourceBundle;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.ColumnType;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class BasketFactory {

    static final String BASKET_QUESTION_SUFFIX = "_by_basket";
    static final String BASKET_ID_QUERY_SUFFIX = "_by_basket";
    static final String BASKET_ATTRIBUTE_QUERY_SUFFIX = "_basket_attrs";
    static final String BASKET_ATTRIBUTE = "in_basket";

    static final String PARAM_USER_SIGNATURE = "user_signature";

    private static final String TABLE_BASKET = "user_basket";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_RECORD_CLASS = "record_class";
    private static final String COLUMN_PK_PREFIX = "pk_column_";

    private WdkModel wdkModel;
    private String schema;

    /**
     * 
     */
    public BasketFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    }

    /**
     * @param user
     * @param recordClass
     * @param pkValuesList
     *            a list of primary key values. the inner map is a primary-key
     *            column-value map.
     * @throws SQLException
     */
    public void addToBasket(User user, RecordClass recordClass,
            List<Map<String, String>> pkValuesList) throws SQLException {
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
            sqlInsert += ", " + COLUMN_PK_PREFIX + i;
            sqlValues += ", ?";
            sqlCount += " AND " + COLUMN_PK_PREFIX + i + " = ?";
        }
        sqlInsert += ") VALUES (?, ?, ?" + sqlValues + ")";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psInsert = null, psCount = null;
        try {
            psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
            psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
            int count = 0;
            for (Map<String, String> pkValues : pkValuesList) {
                // check if the record already exists.
                setParams(psCount, userId, projectId, rcName, pkColumns,
                        pkValues);
                boolean hasRecord = false;
                ResultSet resultSet = null;
                try {
                    resultSet = psCount.executeQuery();
                    if (resultSet.next()) {
                        int rsCount = resultSet.getInt(1);
                        hasRecord = (rsCount > 0);
                    }
                } finally {
                    resultSet.close();
                }
                if (hasRecord) continue;

                // insert new record
                setParams(psInsert, userId, projectId, rcName, pkColumns,
                        pkValues);
                psInsert.addBatch();
                count++;
                if (count % 100 == 0) psInsert.executeBatch();
            }
            if (count % 100 != 0) psInsert.executeBatch();
            psInsert.setInt(1, user.getUserId());
        } finally {
            SqlUtils.closeStatement(psInsert);
            SqlUtils.closeStatement(psCount);
        }
    }

    public void RemoveFromBasket(User user, RecordClass recordClass,
            List<Map<String, String>> pkValuesList) throws SQLException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sqlDelete = "DELETE FROM " + schema + TABLE_BASKET + " WHERE "
                + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sqlDelete += " AND " + COLUMN_PK_PREFIX + i + " = ?";
        }

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psDelete = null;
        try {
            psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
            int count = 0;
            for (Map<String, String> pkValues : pkValuesList) {
                setParams(psDelete, userId, projectId, rcName, pkColumns,
                        pkValues);
                psDelete.addBatch();
                count++;
                if (count % 100 == 0) psDelete.executeBatch();
            }
            if (count % 100 != 0) psDelete.executeBatch();
            psDelete.setInt(1, user.getUserId());
        } finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    Question getBasketQuestion(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // check if the basket question already exists
        String qname = recordClass.getFullName().replace('.', '_')
                + BASKET_QUESTION_SUFFIX;
        QuestionSet internalSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET);

        Question question = internalSet.getQuestion(qname);
        if (question == null) {
            String rcName = recordClass.getDisplayName();
            question = new Question();
            question.setName(qname);
            question.setDisplayName("Get a Snapshot of " + rcName
                    + "(s) From Basket");
            question.setShortDisplayName(rcName + " Basket");
            question.setRecordClassRef(recordClass.getFullName());
            Query query = getBasketIdQuery(recordClass);
            question.setQueryRef(query.getFullName());
            question.excludeResources(wdkModel.getProjectId());
            question.resolveReferences(wdkModel);

            internalSet.addQuestion(question);
        }
        return question;
    }

    private Query getBasketIdQuery(RecordClass recordClass)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        String dbLink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();

        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();

        // check if the boolean query already exists
        String queryName = rcName.replace('.', '_') + BASKET_ID_QUERY_SUFFIX;
        QuerySet internalQuerySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);

        SqlQuery query = (SqlQuery) internalQuerySet.getQuery(queryName);
        if (query == null) {
            query = new SqlQuery();
            query.setName(queryName);
            // create columns
            for (String columnName : pkColumns) {
                Column column = new Column();
                column.setName(columnName);
                query.addColumn(column);
            }
            // create params
            query.addParam(getParam(PARAM_USER_SIGNATURE));

            // make sure we create index on primary keys
            query.setIndexColumns(pkColumns);
            query.excludeResources(wdkModel.getProjectId());
            query.resolveReferences(wdkModel);
            query.setDoNotTest(true);
            query.setIsCacheable(true); // cache the boolean query

            // construct the sql
            User systemUser = wdkModel.getSystemUser();
            Query allRecordsQuery = recordClass.getAllRecordsQuery();
            Map<String, String> params = new LinkedHashMap<String, String>();
            QueryInstance instance = allRecordsQuery.makeInstance(systemUser,
                    params, true);
            String allRecordsSql = instance.getSql();
            String sql = "";
            for (int i = 1; i <= pkColumns.length; i++) {
                if (sql.length() == 0) sql = "SELECT ";
                else sql += ", ";
                sql += "b." + pkColumns[i - 1] + " AS " + COLUMN_PK_PREFIX + i;
            }
            sql += " FROM " + schema + TABLE_BASKET + dbLink + " b, " + schema
                    + UserFactory.TABLE_USER + dbLink + " u, (" + allRecordsSql
                    + ") i WHERE b." + COLUMN_USER_ID + " = u."
                    + UserFactory.COLUMN_USER_ID + " AND u."
                    + UserFactory.COLUMN_SIGNATURE + " = $$"
                    + PARAM_USER_SIGNATURE + "$$ AND b." + COLUMN_PROJECT_ID
                    + " = '" + projectId + "' AND b." + COLUMN_RECORD_CLASS
                    + " = '" + recordClass + "'";
        }
        return query;
    }

    private Param getParam(String paramName) {
        return null;
    }

    Query createBasketAttributeQuery(String recordClassName) {
        return null;
    }

    private void setParams(PreparedStatement ps, int userId, String projectId,
            String rcName, String[] pkColumns, Map<String, String> pkValues)
            throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, projectId);
        ps.setString(3, rcName);
        for (int i = 0; i < pkColumns.length; i++) {
            ps.setString(i + 4, pkValues.get(pkColumns[i]));
        }
    }
}
