/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.ModelConfig;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         raw data: a comma separated list of entities;
 * 
 *         user-dependent data: user dataset id;
 * 
 *         user-independent data: dataset checksum;
 * 
 *         internal data: dataset id; in the future the return will be a SQL
 *         that represents the
 * 
 */
public class DatasetParam extends Param {

    public static final String TYPE_DATA = "data";
    public static final String TYPE_FILE = "file";
    public static final String TYPE_BASKET = "basket";


    private String recordClassRef;
    private RecordClass recordClass;

    /**
     * Only used by datasetParam, determines what input type to be selected as
     * default.
     */
    private String defaultType;

    public DatasetParam() {}

    public DatasetParam(DatasetParam param) {
        super(param);
        this.recordClass = param.recordClass;
        this.recordClassRef = param.recordClassRef;
        this.defaultType = param.defaultType;
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
        recordClass = (RecordClass) wdkModel.resolveReference(recordClassRef);
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
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
        if (extra) {
            jsParam.put("recordClass", recordClass.getFullName());
        }
    }

    /**
     * convert from user dataset id to dataset checksum
     * 
     * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    @Override
    public String dependentValueToIndependentValue(User user,
            String dependentValue) throws NoSuchAlgorithmException,
            WdkUserException, WdkModelException, SQLException, JSONException {
        logger.debug("dependent to independent: " + dependentValue);
        int userDatasetId = Integer.parseInt(dependentValue);
        Dataset dataset = user.getDataset(userDatasetId);
        dataset.setRecordClass(recordClass);
        return dataset.getChecksum();
    }

    /**
     * the internal value is an sql that represents the query from the dataset
     * tables, and returns the primary key columns.
     * 
     * @see org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
     *      (org.gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // the input has to be a user-dataset-id
        int userDatasetId = Integer.parseInt(dependentValue);
        
        if (isNoTranslation()) return Integer.toString(userDatasetId);

        ModelConfig config = wdkModel.getModelConfig();
        String dbLink = config.getAppDB().getUserDbLink();
        String wdkSchema = config.getUserDB().getWdkEngineSchema();
        String userSchema = config.getUserDB().getUserSchema();
        String dvTable = wdkSchema + DatasetFactory.TABLE_DATASET_VALUE + dbLink;
        String udTable = userSchema + DatasetFactory.TABLE_USER_DATASET + dbLink;
        String colDatasetId = DatasetFactory.COLUMN_DATASET_ID;
        String colUserDatasetId = DatasetFactory.COLUMN_USER_DATASET_ID;
        StringBuffer sql = new StringBuffer("SELECT ");
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        for (int i = 1; i <= pkColumns.length; i++) {
            if (i > 1) sql.append(", ");
            sql.append("dv." + Utilities.COLUMN_PK_PREFIX + i);
            sql.append(" AS " + pkColumns[i - 1]);
        }
        sql.append(" FROM ");
        sql.append(udTable + " ud, " + dvTable + " dv ");
        sql.append(" WHERE dv." + colDatasetId + " = ud." + colDatasetId);
        sql.append(" AND ud." + colUserDatasetId + " = " + userDatasetId);
        return sql.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(org.
     * gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String dependentValueToRawValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        logger.debug("dependent to raw: " + dependentValue);
        int userDatasetId = Integer.parseInt(dependentValue);
        Dataset dataset = user.getDataset(userDatasetId);
        dataset.setRecordClass(recordClass);
        return dataset.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(org.
     * gusdb.wdk.model.user.User, java.lang.String)
     */
    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        // first assume the input is dependent value, that is, user dataset id
        if (rawValue == null || rawValue.length() == 0) return null;
        if (rawValue.matches("\\d+")) {
            int userDatasetId = Integer.parseInt(rawValue);
            try {
                user.getDataset(userDatasetId);
                return rawValue;
            } catch (Exception ex) {
                // dataset doesn't exist, create one
                logger.info("user dataset id doesn't exist: " + userDatasetId);
            }
        }
        return rawValueToDependentValue(user, "", rawValue);
    }

    /**
     * @param user
     * @param uploadFile
     * @param rawValue
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     */
    public String rawValueToDependentValue(User user, String uploadFile,
            String rawValue) throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        logger.debug("raw to dependent: " + rawValue);
        Dataset dataset = user.createDataset(recordClass, uploadFile, rawValue);
        return Integer.toString(dataset.getUserDatasetId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#validateValue(org.gusdb.wdk.model
     * .user.User, java.lang.String)
     */
    @Override
    protected void validateValue(User user, String dependentValue)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // try to get the dataset
        int userDatasetId = Integer.parseInt(dependentValue);
        user.getDataset(userDatasetId);
    }

    /**
     * @return the recordClass
     */
    public RecordClass getRecordClass() {
        return recordClass;
    }

    /**
     * @param recordClassRef
     *            the recordClassRef to set
     */
    public void setRecordClassRef(String recordClassRef) {
        this.recordClassRef = recordClassRef;
    }
    
    public void setRecordClass(RecordClass recordClass) {
        this.recordClass = recordClass;
    }

    public String getDefaultType() {
        return (defaultType != null) ? defaultType : TYPE_DATA;
    }

    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    protected void applySuggection(ParamSuggestion suggest) {
        defaultType = suggest.getDefaultType();
    }
}
