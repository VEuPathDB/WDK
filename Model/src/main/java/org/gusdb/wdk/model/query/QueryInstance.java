package org.gusdb.wdk.model.query;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.AbstractDependentParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
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
  public abstract void createCache(String tableName, long instanceId) throws WdkModelException;

  /**
   * @param tableName name of table
   * @param instanceId id of query cache table
   */
  public abstract void insertToCache(String tableName, long instanceId) throws WdkModelException, WdkUserException;

  public abstract String getSql() throws WdkModelException;

  public abstract String getSqlUnsorted() throws WdkModelException, WdkUserException;

  protected abstract void appendJSONContent(JSONObject jsInstance) throws JSONException;

  protected abstract ResultList getUncachedResults() throws WdkModelException;

  protected final User _user;
  protected final T _query;
  protected final WdkModel _wdkModel;
  protected final Map<String, String> _paramStableValues;
  protected final int _assignedWeight;
  protected final Map<String, String> _context;

  private Map<String, String> _paramInternalValues;
  private long _instanceId;
  protected String _resultMessage;
  private boolean _resultMessageSet = false;

  private String _checksum;

  /**
   * @param user user to execute query as
   * @param query query to create instance for
   * @param paramStableValues stable values of all params in the query's context
   * @param assignedWeight weight of the query
   * @throws WdkModelException
   */
  protected QueryInstance(User user, T query, ReadOnlyMap<String, String> paramStableValues,
      int assignedWeight) throws WdkModelException {
    _user = user;
    _query = query;
    _wdkModel = query.getWdkModel();
    _paramStableValues = addUserId(paramStableValues);
    _assignedWeight = assignedWeight;
    _context = createContext();
  }

  private Map<String, String> createContext() {
    Question question = _query.getContextQuestion();
    Param param = _query.getContextParam();
    return new MapBuilder<String,String>()
      .put(Utilities.QUERY_CTX_USER, String.valueOf(_user.getUserId()))
      .put(Utilities.QUERY_CTX_QUERY, _query.getFullName())
      .putIf(question != null, Utilities.QUERY_CTX_QUESTION, () -> question.getFullName())
      .putIf(param != null, Utilities.QUERY_CTX_PARAM, () -> param.getFullName())
      .toMap();
  }

  private Map<String, String> addUserId(ReadOnlyMap<String, String> incomingParamValues) {
    // add user_id into the param values
    Map<String, String> supplementedParamValues = incomingParamValues.toWriteableMap();
    Map<String, Param> params = _query.getParamMap();
    String userKey = Utilities.PARAM_USER_ID;
    if (params.containsKey(userKey) && !incomingParamValues.containsKey(userKey)) {
      supplementedParamValues.put(userKey, Long.toString(_user.getUserId()));
    }
    return supplementedParamValues;
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

  public String getResultMessage() throws WdkModelException, WdkUserException {
    if (!_resultMessageSet) {
      // make sure the result message is loaded by caching results
      new ResultFactory(_wdkModel).getCachedSql(this, false);
    }
    return _resultMessage;
  }

  public void setResultMessage(String message) {
    _resultMessage = message;
    _resultMessageSet = true;
  }

  /**
   * @return the cached
   */
  public boolean getIsCacheable() {
    return _query.getIsCacheable();
  }

  public String getChecksum() throws WdkModelException {
    if (_checksum == null) {
      JSONObject jsQuery = getJSONContent();
      _checksum = EncryptionUtil.encrypt(JsonUtil.serialize(jsQuery));
    }
    return _checksum;
  }

  public JSONObject getJSONContent() throws WdkModelException {
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

  public JSONObject getParamSignatures() throws WdkModelException {
    // the values are dependent values. need to convert it into independent values
    Map<String, String> signatures = _query.getSignatures(_user, _paramStableValues);

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

  public ResultList getResults() throws WdkModelException {
    return getResults(true);
  }

  public ResultList getResultsUnsorted() throws WdkModelException {
    return getResults(false);
  }

  private ResultList getResults(boolean performSorting) throws WdkModelException {
    return (getIsCacheable()) ? getCachedResults(performSorting) : getUncachedResults();
  }

  public int getResultSize() throws WdkModelException {
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

  public ReadOnlyMap<String, String> getParamStableValues() {
    return new ReadOnlyHashMap<>(_paramStableValues);
  }

  private ResultList getCachedResults(boolean performSorting) throws WdkModelException {
    ResultFactory factory = _wdkModel.getResultFactory();
    return factory.getCachedResults(this, performSorting);
  }

  protected String getCachedSql(boolean performSorting) throws WdkModelException {
    ResultFactory resultFactory = _wdkModel.getResultFactory();
    return resultFactory.getCachedSql(this, performSorting);
  }

  @Deprecated
  private void validateContextValuesAndFillEmptyWithDefaults(User user, Map<String, String> values) throws
      WdkModelException {
    Map<String, Param> params = _query.getParamMap();
    Map<String, String> errors = null;

    values = fillEmptyValues(values);
    // then check that all params have supplied values
    for (String paramName : values.keySet()) {
      String errMsg = null;
      String dependentValue = values.get(paramName);
      String prompt = paramName;
      try {
        if (!params.containsKey(paramName)) {
          // LOG.warn("The parameter '" + paramName + "' doesn't exist in query " + _query.getFullName());
          continue;
        }

        Param param = params.get(paramName);
        prompt = param.getPrompt();

        // validate param
        param.validate(user, dependentValue, values);
      }
      catch (Exception ex) {
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
      WdkUserException ex = new ParamValuesInvalidException("In query " + _query.getFullName() + " some of the input parameters are invalid or missing.", errors);
      LOG.error(ex);
      throw new WdkModelException("Could not validate and fill values", ex);
    }
  }

  private Map<String, String> fillEmptyValues(Map<String, String> stableValues) throws WdkModelException {
    Map<String, String> newValues = new LinkedHashMap<String, String>(stableValues);
    Map<String, Param> paramMap = _query.getParamMap();

    // iterate through this query's params, filling values
    for (String paramName : paramMap.keySet()) {
      resolveParamValue(paramMap.get(paramName), newValues);
    }
    return newValues;
  }

  private void resolveParamValue(Param param, Map<String, String> stableValues) throws WdkModelException {
    String value;
    if (!stableValues.containsKey(param.getName())) {
      // param not provided, determine value
      if (param instanceof AbstractDependentParam && ((AbstractDependentParam) param).isDependentParam()) {
        // special case; must get value of depended param first
        AbstractDependentParam adParam = (AbstractDependentParam) param;
        Map<String, String> dependedValues = new LinkedHashMap<>();
        for (Param dependedParam : adParam.getDependedParams()) {
          resolveParamValue(dependedParam, stableValues);
          String dependedName = dependedParam.getName();
          dependedValues.put(dependedName, stableValues.get(dependedName));
        }
        value = adParam.getDefault(_user, dependedValues);
      }
      else {
        value = param.getDefault();
      }
    }
    else { // param provided, but it can be empty
      value = stableValues.get(param.getName());
      if (value == null || value.length() == 0) {
        value = param.isAllowEmpty() ? param.getEmptyValue() : null;
      }
    }
    stableValues.put(param.getName(), value);
  }

  protected Map<String, String> getParamInternalValues() throws WdkModelException {

    if (_paramInternalValues == null) {
      // the empty & default values are filled
      Map<String, String> stableValues = fillEmptyValues(_paramStableValues);
      _paramInternalValues = new LinkedHashMap<String, String>();
      Map<String, Param> params = _query.getParamMap();
      for (String paramName : params.keySet()) {
        Param param = params.get(paramName);
        String internalValue, stableValue = stableValues.get(paramName);
        internalValue = param.getInternalValue(_user, stableValue, stableValues);
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
