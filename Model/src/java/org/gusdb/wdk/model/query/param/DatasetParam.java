/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         The input to a datasetParam is a dataset_checksum;
 * 
 *         The output is a SQL that represents the dataset list in application
 *         DB, and dblink might be used.
 * 
 */
public class DatasetParam extends Param {

    private static final String USER_DEPENDENT_PATTERN = "\\w+:\\d+";
    private static final String USER_INDEPENDENT_PATTERN = "\\w+";

    private String columnName = DatasetFactory.COLUMN_DATASET_VALUE;

    public DatasetParam() {}

    public DatasetParam(DatasetParam param) {
        super(param);
        this.columnName = param.columnName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#validateValue(java.lang.Object)
     */
    @Override
    public void validateValue(String datasetChecksum) throws WdkModelException,
            WdkUserException, SQLException {
        // try getting the dataset id
        getDatasetId(datasetChecksum);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel model) throws WdkModelException {
        this.wdkModel = model;
    }

    /**
     * @param combinedKey
     *            a combined key of dataset: <user_key>:<dataset_key>
     * @return a SQL that represents the data set value list. The return field
     *         name is defined by columnName, by default it is
     *         DatasetFactory.COLMN_DATASET_VALUE.
     * @throws SQLException
     */
    @Override
    public String getInternalValue(String datasetChecksum)
            throws WdkModelException, WdkUserException, SQLException {
        int datasetId = getDatasetId(datasetChecksum);
        return Integer.toString(datasetId);

        // to be compatible with previous model, it returns dataset_id;
        // in the future, should return a nested SQL that represents the result

        // ModelConfig config = wdkModel.getModelConfig();
        // String dbLink = config.getApplicationDB().getUserDbLink();
        // String wdkSchema = config.getUserDB().getWdkEngineSchema();
        //
        // StringBuffer sql = new StringBuffer("SELECT ");
        // sql.append(DatasetFactory.COLUMN_DATASET_VALUE).append(" AS ");
        // sql.append(columnName);
        // sql.append(" FROM ").append(wdkSchema);
        // sql.append(DatasetFactory.TABLE_DATASET_VALUE).append(dbLink);
        // sql.append(" WHERE ").append(DatasetFactory.COLUMN_DATASET_ID);
        // sql.append(" = ").append(datasetId);
        // return sql.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#clone()
     */
    @Override
    public Param clone() {
        return new DatasetParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) throws JSONException {
        jsParam.put("column", columnName);
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName
     *            the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * the input is <user_key:userDatasetId>;
     * 
     * the output is <dataset_checksum>
     * 
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     */
    @Override
    protected String getUserIndependentValue(String combinedKey)
            throws WdkModelException, WdkUserException, SQLException {
        Dataset dataset = getDataset(combinedKey);
        return dataset.getChecksum();
    }

    public Dataset getDataset(String combinedKey) throws WdkUserException,
            WdkModelException, SQLException {
        if (!combinedKey.matches(USER_DEPENDENT_PATTERN))
            throw new WdkModelException("The user-dependent input to the "
                    + "datasetParam [" + getFullName() + "] should "
                    + "be in form such as 'user_key:step_display_id'; "
                    + "instead, it is '" + combinedKey + "'");

        String[] parts = combinedKey.split("\\:");
        String userSignature = parts[0];
        int userDatasetId = Integer.parseInt(parts[1]);

        // get step
        UserFactory userFactory = wdkModel.getUserFactory();
        User user = userFactory.getUser(userSignature);
        DatasetFactory datasetFactory = wdkModel.getDatasetFactory();
        return datasetFactory.getDataset(user, userDatasetId);
    }

    /**
     * Make sure the dataset exists through dblink
     * 
     * @param datasetChecksum
     * @throws SQLException
     * @throws WdkModelException
     */
    private int getDatasetId(String datasetChecksum) throws SQLException,
            WdkModelException {
        if (!datasetChecksum.matches(USER_INDEPENDENT_PATTERN))
            throw new WdkModelException("The user-dependent input to the "
                    + "datasetParam [" + getFullName() + "] should "
                    + "be in form such as 'user_key:step_display_id'; "
                    + "instead, it is '" + datasetChecksum + "'");

        String wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        String dbLink = wdkModel.getModelConfig().getApplicationDB().getUserDbLink();
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(DatasetFactory.COLUMN_DATASET_ID).append("FROM ");
        sql.append(wdkSchema).append(DatasetFactory.TABLE_DATASET_INDEX);
        sql.append(dbLink).append(" WHERE ");
        sql.append(DatasetFactory.COLUMN_DATASET_CHECKSUM).append(" = ?");

        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    sql.toString());
            ps.setString(1, datasetChecksum);
            resultSet = ps.executeQuery();

            if (!resultSet.next())
                throw new WdkModelException("The dataset with checksum '"
                        + datasetChecksum + "' doesn't exist");
            return resultSet.getInt(DatasetFactory.COLUMN_DATASET_ID);
        } finally {
            if (resultSet != null) SqlUtils.closeResultSet(resultSet);
        }
    }
}
