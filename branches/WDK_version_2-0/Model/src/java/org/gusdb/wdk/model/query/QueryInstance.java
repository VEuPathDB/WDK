/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.QueryInfo;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jerric Gao
 * 
 */
public abstract class QueryInstance {

    public abstract void createCache(Connection connection, String tableName,
            int instanceId, String[] indexColumns)
            throws NoSuchAlgorithmException, WdkModelException, SQLException,
            JSONException, WdkUserException;

    public abstract void insertToCache(Connection connection, String tableName,
            int instanceId) throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException;

    public abstract String getSql() throws WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException, WdkUserException;

    protected abstract void appendSJONContent(JSONObject jsInstance)
            throws JSONException;

    protected abstract ResultList getUncachedResults()
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException;

    private static final Logger logger = Logger.getLogger(QueryInstance.class);

    protected User user;
    private Integer instanceId;
    protected Query query;
    protected WdkModel wdkModel;
    protected Map<String, String> values;
    protected String resultMessage;
    protected boolean cached;

    private String checksum;
    protected int assignedWeight;

    protected Map<String, String> context;

    protected QueryInstance(User user, Query query, Map<String, String> values,
            boolean validate, int assignedWeight, Map<String, String> context)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        this.user = user;
        this.query = query;
        this.wdkModel = query.getWdkModel();
        this.cached = query.isCached();
        this.assignedWeight = assignedWeight;
        this.context = context;

        this.context.put(Utilities.QUERY_CTX_QUERY, query.getFullName());
        this.context.put(Utilities.QUERY_CTX_USER, user.getSignature());

        setValues(values, validate);
    }

    public Query getQuery() {
        return query;
    }

    /**
     * @return the instanceId
     * @throws JSONException
     * @throws WdkModelException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    public Integer getInstanceId() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        if (instanceId == null) {
            ResultFactory resultFactory = wdkModel.getResultFactory();
            String[] indexColumns = query.getIndexColumns();
            instanceId = resultFactory.getInstanceId(this, indexColumns);
        }
        return instanceId;
    }

    /**
     * @param instanceId
     *            the instanceId to set
     */
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    private void setValues(Map<String, String> values, boolean validate)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        logger.trace("----- input value for [" + query.getFullName()
                + "] -----");
        for (String paramName : values.keySet()) {
            logger.trace(paramName + "='" + values.get(paramName) + "'");
        }

        // add user_id into the param values
        Map<String, Param> params = query.getParamMap();
        String userKey = Utilities.PARAM_USER_ID;
        if (params.containsKey(userKey) && !values.containsKey(userKey)) {
            values.put(userKey, Integer.toString(user.getUserId()));
        }

        // convert the values into dependent values
        for (Param param : params.values()) {
            if (values.containsKey(param.getName())) {
                String value = values.get(param.getName());
                value = param.rawOrDependentValueToDependentValue(user, value);
                values.put(param.getName(), value);
            }
        }

        if (validate)
            validateValues(user, values);
        // passed, assign the value
        this.values = new LinkedHashMap<String, String>(values);
        checksum = null;
    }

    public String getResultMessage() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        // make sure the result message is loaded by getting instance id
        getInstanceId();
        return resultMessage;
    }

    public void setResultMessage(String message) {
        this.resultMessage = message;
    }

    /**
     * @return the cached
     */
    public boolean isCached() {
        return this.cached;
    }

    /**
     * @param cached
     *            the cached to set
     */
    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public String getChecksum() throws NoSuchAlgorithmException,
            WdkModelException, JSONException, WdkUserException, SQLException {
        if (checksum == null) {
            JSONObject jsQuery = getJSONContent();
            checksum = Utilities.encrypt(jsQuery.toString());
        }
        return checksum;
    }

    public JSONObject getJSONContent() throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        JSONObject jsInstance = new JSONObject();
        jsInstance.put("project", wdkModel.getProjectId());
        jsInstance.put("query", query.getFullName());

        jsInstance.put("params", getIndependentParamValuesJSONObject());
        jsInstance.put("assignedWeight", assignedWeight);

        // include extra info from child
        appendSJONContent(jsInstance);

        return jsInstance;
    }

    public JSONObject getIndependentParamValuesJSONObject()
            throws JSONException, NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        // the values are dependent values. need to convert it into independent
        // values
        Map<String, String> independentValues = query
                .dependentValuesToIndependentValues(user, values);

        // construct param-value map; param is sorted by name
        String[] paramNames = new String[independentValues.size()];
        independentValues.keySet().toArray(paramNames);
        Arrays.sort(paramNames);

        JSONObject jsParams = new JSONObject();
        for (String paramName : paramNames) {
            String value = independentValues.get(paramName);
            if (value != null && value.length() > 0)
                jsParams.put(paramName, value);
        }
        return jsParams;
    }

    public ResultList getResults() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        logger.debug("retrieving results of query [" + query.getFullName()
                + "]");

        ResultList resultList = (cached) ? getCachedResults()
                : getUncachedResults();

        logger.debug("results of query [" + query.getFullName()
                + "] retrieved.");

        return resultList;
    }

    public int getResultSize() throws NoSuchAlgorithmException, SQLException,
            WdkModelException, JSONException, WdkUserException {
        logger.debug("start getting query size");
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM (");
        sql.append(getSql()).append(") f");
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        Object objSize = SqlUtils.executeScalar(wdkModel, dataSource,
                sql.toString(), query.getFullName() + "-count");
        int resultSize = Integer.parseInt(objSize.toString());
        logger.debug("end getting query size");
        return resultSize;
    }

    public Map<String, String> getValues() {
        return values;
    }

    private ResultList getCachedResults() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        ResultFactory factory = wdkModel.getResultFactory();
        return factory.getCachedResults(this);
    }

    protected String getCachedSql() throws NoSuchAlgorithmException,
            SQLException, WdkModelException, JSONException, WdkUserException {
        CacheFactory cacheFactory = wdkModel.getResultFactory()
                .getCacheFactory();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(getQuery());

        String cacheTable = queryInfo.getCacheTable();
        int instanceId = getInstanceId();

        StringBuffer sql = new StringBuffer("SELECT * FROM ");
        sql.append(cacheTable).append(" WHERE ");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID);
        sql.append(" = ").append(instanceId);
        return sql.toString();
    }

    private void validateValues(User user, Map<String, String> values)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        Map<String, Param> params = query.getParamMap();
        Map<String, String> errors = null;

        values = fillEmptyValues(values);
        // then check that all params have supplied values
        for (String paramName : values.keySet()) {
            String errMsg = null;
            String dependentValue = values.get(paramName);
            String prompt = paramName;
            try {
                if (!params.containsKey(paramName))
                    throw new WdkModelException("The parameter '" + paramName
                            + "' doesn't exist");

                Param param = params.get(paramName);
                prompt = param.getPrompt();

                // check for dependent param
                if (param instanceof AbstractEnumParam
                        && ((AbstractEnumParam) param).getDependedParam() != null) {
                    String dependedParam = ((AbstractEnumParam) param)
                            .getDependedParam().getName();
                    String dependedValue = values.get(dependedParam);
                    ((AbstractEnumParam) param).setDependedValue(dependedValue);
                }

                // validate param
                param.validate(user, dependentValue);
            } catch (Exception ex) {
                ex.printStackTrace();
                errMsg = ex.getMessage();
                if (errMsg == null)
                    errMsg = ex.getClass().getName();
            }
            if (errMsg != null) {
                if (errors == null)
                    errors = new LinkedHashMap<String, String>();
                errors.put(prompt, errMsg);
            }
        }
        if (errors != null) {
            WdkModelException ex = new WdkModelException(
                    "Some of the input parameters are invalid.", errors);
            logger.debug(ex.formatErrors());
            throw ex;
        }
    }

    private Map<String, String> fillEmptyValues(Map<String, String> values)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        Map<String, String> newValues = new LinkedHashMap<String, String>();
        Map<String, Param> paramMap = query.getParamMap();
        for (String paramName : paramMap.keySet()) {
            Param param = paramMap.get(paramName);

            String value;
            if (!values.containsKey(paramName)) {
                // param not provided, use default value
                value = param.getDefault();
            } else { // param provided, but it can be empty
                value = values.get(paramName);
                // logger.debug("param " + paramName + " provided value: '"
                // + value + "'");
                if (value == null || value.length() == 0) {
                    value = param.isAllowEmpty() ? param.getEmptyValue() : null;
                }
            }
            newValues.put(paramName, value);
        }
        return newValues;
    }

    protected Map<String, String> getInternalParamValues()
            throws WdkModelException, SQLException, NoSuchAlgorithmException,
            JSONException, WdkUserException {
        // the empty & default values are filled
        Map<String, String> values = fillEmptyValues(this.values);
        Map<String, String> internalValues = new LinkedHashMap<String, String>();
        Map<String, Param> params = query.getParamMap();
        for (String paramName : params.keySet()) {
            Param param = params.get(paramName);

            String dependentValue = values.get(paramName);
            String internalValue = param.dependentValueToInternalValue(user,
                    dependentValue);
            internalValues.put(paramName, internalValue);
        }
        return internalValues;
    }

    public int getAssignedWeight() {
        return assignedWeight;
    }
}
