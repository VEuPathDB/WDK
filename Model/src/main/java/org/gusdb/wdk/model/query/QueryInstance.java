package org.gusdb.wdk.model.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ValidatedParamStableValues;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The query instance holds the values for the parameters, and the sub classes of it are responsible for
 * running the actual query, and retrieving the results from the resource.
 * 
 * If the query is set to be cached, the query instance will call the CacheFactory to cache the results, and
 * then represent the results as an SQL on the cached result. If a query of the same param values is called
 * later, the cache will be used instead to improve the performance of query.
 * 
 * @author Jerric Gao
 */
public abstract class QueryInstance<T extends Query> {

  private static final Logger LOG = Logger.getLogger(QueryInstance.class);

  /**
   * @param tableName name of table
   * @param instanceId id of query cache table
   */
  public abstract void createCache(String tableName, long instanceId) throws WdkModelException, WdkUserException;

  /**
   * @param tableName name of table
   * @param instanceId id of query cache table
   */
  public abstract void insertToCache(String tableName, long instanceId) throws WdkModelException, WdkUserException;

  public abstract String getSql() throws WdkModelException, WdkUserException;

  public abstract String getSqlUnsorted() throws WdkModelException, WdkUserException;

  protected abstract void appendJSONContent(JSONObject jsInstance) throws JSONException;

  protected abstract ResultList getUncachedResults() throws WdkModelException, WdkUserException;

  protected User _user;
  private long _instanceId;
  protected T _query;
  protected WdkModel _wdkModel;
  protected ValidatedParamStableValues _contextParamStableValues;
  protected String _resultMessage;

  private String _checksum;
  protected int _assignedWeight;

  protected Map<String, String> _context;
  private Map<String, String> _paramInternalValues = null;

  /**
   * @param user user to execute query as
   * @param query query to create instance for
   * @param contextParamStableValues stable values of all params in the query's context
   * @param validate whether to validate param values.  If set to true, then, for any missing param values in the context, use default.  Set to false only 
   * known to have a complete and correct set of contextParamStableValues
   * @param assignedWeight weight of the query
   * @param context additional information to be passed to ProcessQueries (unused by SqlQueries)
   * @throws WdkModelException
   * @throws WdkUserException
   */
  protected QueryInstance(User user, T query, ValidatedParamStableValues contextParamStableValues, boolean validate,
      int assignedWeight, Map<String, String> context) throws WdkModelException, WdkUserException {
    _user = user;
    _query = query;
    _wdkModel = query.getWdkModel();
    _assignedWeight = assignedWeight;
    _context = new HashMap<>(context);
    _checksum = null;

    _context.put(Utilities.QUERY_CTX_QUERY, query.getFullName());
    _context.put(Utilities.QUERY_CTX_USER, String.valueOf(user.getUserId()));

//    setContextParamStableValues(contextParamStableValues, validate);
  }

  public Query getQuery() {
    return _query;
  }

  /**
   * @return the instanceId
   */
  public long getInstanceId() {
    return _instanceId;
  }

  /**
   * @param instanceId
   *          the instanceId to set
   */
  public void setInstanceId(long instanceId) {
    _instanceId = instanceId;
  }

//  private void setContextParamStableValues(ValidParamValueMap contextParamStableValues, boolean validate)
//      throws WdkModelException, WdkUserException {
//    LOG.trace("----- input value for [" + _query.getFullName() + "] -----");
//    for (String paramName : contextParamStableValues.keySet()) {
//      LOG.trace(paramName + "='" + contextParamStableValues.get(paramName) + "'");
//    }
//
//    // add user_id into the param values
//    //Map<String, Param> params = _query.getParamMap();
//    //String userKey = Utilities.PARAM_USER_ID;
//    //if (params.containsKey(userKey) && !contextParamStableValues.containsKey(userKey)) {
//    //  contextParamStableValues.put(userKey, Long.toString(_user.getUserId()));
//    //}
//
//    if (validate)
//      //validateContextValuesAndFillEmptyWithDefaults(_user, contextParamStableValues);
//
//    // passed, assign the value
//    _contextParamStableValues = contextParamStableValues;
//    _checksum = null;
//  }

  public String getResultMessage() {
    // make sure the result message is loaded by getting instance id
    getInstanceId();
    return _resultMessage;
  }

  public void setResultMessage(String message) {
    _resultMessage = message;
  }

  /**
   * @return the cached
   */
  public boolean getIsCacheable() {
    return _query.getIsCacheable();
  }

  public String getChecksum() throws WdkModelException, WdkUserException {
    if (_checksum == null) {
      JSONObject jsQuery = getJSONContent();
      _checksum = EncryptionUtil.encrypt(JsonUtil.serialize(jsQuery));
    }
    return _checksum;
  }

  public JSONObject getJSONContent() throws WdkModelException, WdkUserException {
    try {
      JSONObject jsInstance = new JSONObject();
      jsInstance.put("project", _wdkModel.getProjectId());
      jsInstance.put("query", _query.getFullName());
      jsInstance.put("queryChecksum", _query.getChecksum());

      jsInstance.put("params", getParamSignatures());
      jsInstance.put("assignedWeight", _assignedWeight);

      // include extra info from child
      appendJSONContent(jsInstance);

      LOG.debug("json:\n" + jsInstance.toString(2));

      return jsInstance;
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to build JSON object.", e);
    }
  }

  public JSONObject getParamSignatures() throws WdkModelException, WdkUserException {
    // the values are dependent values. need to convert it into independent values
    Map<String, String> signatures = _query.getSignatures(_user, _contextParamStableValues);

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
    }
    catch (JSONException e) {
      throw new WdkModelException("Error while converting param values to JSON." + e);
    }
  }

  public ResultList getResults() throws WdkModelException, WdkUserException {
    return getResults(true);
  }

  public ResultList getResultsUnsorted() throws WdkModelException, WdkUserException {
    return getResults(false);
  }

  private ResultList getResults(boolean performSorting) throws WdkModelException, WdkUserException {
    return (getIsCacheable()) ? getCachedResults(performSorting) : getUncachedResults();
  }

  public int getResultSize() throws WdkModelException, WdkUserException {
    try {
      //logger.debug("start getting query size");
      StringBuffer sql = new StringBuffer("SELECT count(*) FROM (");
      sql.append(getSql()).append(") f");
      DataSource dataSource = _wdkModel.getAppDb().getDataSource();
      Object objSize = SqlUtils.executeScalar(dataSource, sql.toString(), _query.getFullName() + "__count");
      int resultSize = Integer.parseInt(objSize.toString());
      //logger.debug("end getting query size");
      return resultSize;
    }
    catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }

  public ValidatedParamStableValues getParamStableValues() {
    return _contextParamStableValues;
  }

  private ResultList getCachedResults(boolean performSorting) throws WdkModelException, WdkUserException {
    ResultFactory factory = _wdkModel.getResultFactory();
    return factory.getCachedResults(this, performSorting);
  }

  protected String getCachedSql(boolean performSorting) throws WdkModelException, WdkUserException {
    ResultFactory resultFactory = _wdkModel.getResultFactory();
    return resultFactory.getCachedSql(this, performSorting);
  }

  protected Map<String, String> getParamInternalValues() throws WdkModelException, WdkUserException {

    if (_paramInternalValues == null) {
    	  // _contextParaStableValues should already have the empty values filled in. 
      _paramInternalValues = new LinkedHashMap<String, String>();
      Map<String, Param> params = _query.getParamMap();
      for (String paramName : params.keySet()) {
        Param param = params.get(paramName);
        //String internalValue, stableValue = stableValues.get(paramName);
        String internalValue, stableValue = _contextParamStableValues.get(paramName);
        internalValue = param.getInternalValue(_user, stableValue, _contextParamStableValues);
        _paramInternalValues.put(paramName, internalValue);
      }
    }
    return Collections.unmodifiableMap(_paramInternalValues);
  }
  
  protected void executePostCacheUpdateSql(String tableName, long instanceId) throws WdkModelException {
    List<PostCacheUpdateSql> list = _query.getPostCacheUpdateSqls() != null ? _query.getPostCacheUpdateSqls()
        : _query.getQuerySet().getPostCacheUpdateSqls();
    for (PostCacheUpdateSql pcis : list) {

      String sql = pcis.getSql();
      sql = sql.replace(Utilities.MACRO_CACHE_TABLE, tableName);
      sql = sql.replace(Utilities.MACRO_CACHE_INSTANCE_ID, Long.toString(instanceId));

      LOG.debug("POST sql: " + sql);
      // get results and time process();
      DataSource dataSource = _wdkModel.getAppDb().getDataSource();
      try {
        SqlUtils.executeUpdate(dataSource, sql, _query.getQuerySet().getName() + "__postCacheUpdateSql",
            false);
      }
      catch (SQLException ex) {
        throw new WdkModelException("Unable to run postCacheinsertSql:  " + sql, ex);
      }
    }

  }

  public int getAssignedWeight() {
    return _assignedWeight;
  }
}
