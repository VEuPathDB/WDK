/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The query instance holds the values for the parameters, and the sub classes
 * of it are responsible for running the actual query, and retrieving the
 * results from the resource.
 * 
 * If the query is set to be cached, the query instance will call the
 * CacheFactory to cache the results, and then represent the results as an SQL
 * on the cached result. If a query of the same param values is called later,
 * the cache will be used instead to improve the performance of query.
 * 
 * @author Jerric Gao
 * 
 */
public abstract class QueryInstance {

  public abstract void createCache(String tableName, int instanceId)
      throws WdkModelException;

  public abstract void insertToCache(String tableName, int instanceId)
      throws WdkModelException;

  public abstract String getSql() throws WdkModelException;

  protected abstract void appendSJONContent(JSONObject jsInstance)
      throws JSONException;

  protected abstract ResultList getUncachedResults() throws WdkModelException;

  private static final Logger logger = Logger.getLogger(QueryInstance.class);

  protected User user;
  private int instanceId;
  protected Query query;
  protected WdkModel wdkModel;
  protected Map<String, String> stableValues;
  protected String resultMessage;

  private String checksum;
  protected int assignedWeight;

  protected Map<String, String> context;

  protected QueryInstance(User user, Query query,
      Map<String, String> stableValues, boolean validate, int assignedWeight,
      Map<String, String> context) throws WdkModelException {
    this.user = user;
    this.query = query;
    this.wdkModel = query.getWdkModel();
    this.assignedWeight = assignedWeight;
    this.context = context;

    this.context.put(Utilities.QUERY_CTX_QUERY, query.getFullName());
    this.context.put(Utilities.QUERY_CTX_USER, user.getSignature());

    setValues(stableValues, validate);
  }

  public Query getQuery() {
    return query;
  }

  /**
   * @return the instanceId
   */
  public int getInstanceId() {
    return instanceId;
  }

  /**
   * @param instanceId
   *          the instanceId to set
   */
  public void setInstanceId(int instanceId) {
    this.instanceId = instanceId;
  }

  private void setValues(Map<String, String> stableValues, boolean validate)
      throws WdkModelException {
    logger.trace("----- input value for [" + query.getFullName() + "] -----");
    for (String paramName : stableValues.keySet()) {
      logger.trace(paramName + "='" + stableValues.get(paramName) + "'");
    }

    // add user_id into the param values
    Map<String, Param> params = query.getParamMap();
    String userKey = Utilities.PARAM_USER_ID;
    if (params.containsKey(userKey) && !stableValues.containsKey(userKey)) {
      stableValues.put(userKey, Integer.toString(user.getUserId()));
    }

    if (validate) validateValues(user, stableValues);

    // passed, assign the value
    this.stableValues = stableValues;
    checksum = null;
  }

  public String getResultMessage() {
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
    return query.isCached();
  }

  public String getChecksum() throws WdkModelException {
    if (checksum == null) {
      JSONObject jsQuery = getJSONContent();
      checksum = Utilities.encrypt(jsQuery.toString());
    }
    return checksum;
  }

  public JSONObject getJSONContent() throws WdkModelException {
    try {
      JSONObject jsInstance = new JSONObject();
      jsInstance.put("project", wdkModel.getProjectId());
      jsInstance.put("query", query.getFullName());

      jsInstance.put("params", getParamSignatures());
      jsInstance.put("assignedWeight", assignedWeight);

      // include extra info from child
      appendSJONContent(jsInstance);

      return jsInstance;
    } catch (JSONException e) {
      throw new WdkModelException("Unable to build JSON object.", e);
    }
  }

  public JSONObject getParamSignatures() throws WdkModelException {
    // the values are dependent values. need to convert it into independent
    // values
    Map<String, String> signatures = query.getSignatures(user, stableValues);

    // construct param-value map; param is sorted by name
    String[] paramNames = new String[signatures.size()];
    signatures.keySet().toArray(paramNames);
    Arrays.sort(paramNames);

    try {
      JSONObject jsParams = new JSONObject();
      for (String paramName : paramNames) {
        String value = signatures.get(paramName);
        if (value != null && value.length() > 0)
          jsParams.put(paramName, value);
      }
      return jsParams;
    } catch (JSONException e) {
      throw new WdkModelException(
          "Error while converting param values to JSON." + e);
    }
  }

  public ResultList getResults() throws WdkModelException {
    logger.debug("retrieving results of query [" + query.getFullName() + "]");

    ResultList resultList = (isCached()) ? getCachedResults()
        : getUncachedResults();

    logger.debug("results of query [" + query.getFullName() + "] retrieved.");

    return resultList;
  }

  public int getResultSize() throws WdkModelException {
    try {
      logger.debug("start getting query size");
      StringBuffer sql = new StringBuffer("SELECT count(*) FROM (");
      sql.append(getSql()).append(") f");
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      Object objSize = SqlUtils.executeScalar(dataSource, sql.toString(),
          query.getFullName() + "__count");
      int resultSize = Integer.parseInt(objSize.toString());
      logger.debug("end getting query size");
      return resultSize;
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  public Map<String, String> getParamStableValues() {
    return stableValues;
  }

  private ResultList getCachedResults() throws WdkModelException {
    ResultFactory factory = wdkModel.getResultFactory();
    return factory.getCachedResults(this);
  }

  protected String getCachedSql() throws WdkModelException {
    ResultFactory resultFactory = wdkModel.getResultFactory();
    return resultFactory.getCachedSql(this);
  }

  private void validateValues(User user, Map<String, String> values)
      throws WdkModelException {
    Map<String, Param> params = query.getParamMap();
    Map<String, String> errors = null;

    values = fillEmptyValues(values);
    // then check that all params have supplied values
    for (String paramName : values.keySet()) {
      String errMsg = null;
      String dependentValue = values.get(paramName);
      String prompt = paramName;
      try {
        if (!params.containsKey(paramName)) {
          logger.warn("The parameter '" + paramName
              + "' doesn't exist in query " + query.getFullName());
          continue;
        }

        Param param = params.get(paramName);
        prompt = param.getPrompt();

        // validate param
        param.validate(user, dependentValue, values);
      } catch (Exception ex) {
        ex.printStackTrace();
        errMsg = ex.getMessage();
        if (errMsg == null) errMsg = ex.getClass().getName();
      }
      if (errMsg != null) {
        if (errors == null) errors = new LinkedHashMap<String, String>();
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

  private Map<String, String> fillEmptyValues(Map<String, String> stableValues)
      throws WdkModelException {
    Map<String, String> newValues = new LinkedHashMap<String, String>(
        stableValues);
    Map<String, Param> paramMap = query.getParamMap();

    // iterate through this query's params, filling values
    for (String paramName : paramMap.keySet()) {
      resolveParamValue(paramMap.get(paramName), newValues);
    }
    return newValues;
  }

  private void resolveParamValue(Param param, Map<String, String> stableValues)
      throws WdkModelException {
    String value;
    if (!stableValues.containsKey(param.getName())) {
      // param not provided, determine value
      if (param instanceof AbstractEnumParam
          && ((AbstractEnumParam) param).isDependentParam()) {
        // special case; must get value of depended param first
        AbstractEnumParam aeParam = (AbstractEnumParam) param;
        Map<String, String> dependedValues = new LinkedHashMap<>();
        for (Param dependedParam : aeParam.getDependedParams()) {
          resolveParamValue(dependedParam, stableValues);
          String dependedName = dependedParam.getName();
          dependedValues.put(dependedName, stableValues.get(dependedName));
        }
        value = aeParam.getDefault(dependedValues);
      } else {
        value = param.getDefault();
      }
    } else { // param provided, but it can be empty
      value = stableValues.get(param.getName());
      if (value == null || value.length() == 0) {
        value = param.isAllowEmpty() ? param.getEmptyValue() : null;
      }
    }
    stableValues.put(param.getName(), value);
  }

  protected Map<String, String> getParamInternalValues()
      throws WdkModelException {
    // the empty & default values are filled
    Map<String, String> stableValues = fillEmptyValues(this.stableValues);
    Map<String, String> internalValues = new LinkedHashMap<String, String>();
    Map<String, Param> params = query.getParamMap();
    for (String paramName : params.keySet()) {
      Param param = params.get(paramName);
      String internalValue, stableValue = stableValues.get(paramName);

      // TODO: refactor so this fork isn't necessary
      if (param instanceof AbstractEnumParam
          && ((AbstractEnumParam) param).isDependentParam()) {
        AbstractEnumParam aeParam = (AbstractEnumParam) param;
        Map<String, String> dependedParamValues = new LinkedHashMap<>();
        for (Param dependedParam : aeParam.getDependedParams()) {
          String value = stableValues.get(dependedParam.getName());
          dependedParamValues.put(dependedParam.getName(), value);
        }
        internalValue = aeParam.getInternalValue(user, stableValue,
            dependedParamValues);
      } else {
        internalValue = param.getInternalValue(user, stableValue, stableValues);
      }

      internalValues.put(paramName, internalValue);
    }
    return internalValues;
  }

  public int getAssignedWeight() {
    return assignedWeight;
  }
}
