package org.gusdb.wdk.model.query;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
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
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
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
  protected final RunnableObj<QueryInstanceSpec> _spec;
  protected final ReadOnlyMap<String, String> _context;

  private Map<String, String> _paramInternalValues;
  private long _instanceId;
  protected String _resultMessage;
  private boolean _resultMessageSet = false;

  private String _checksum;

  /**
   * @param spec query instance spec
   */
  @SuppressWarnings("unchecked")
  protected QueryInstance(RunnableObj<QueryInstanceSpec> spec) {
    // can cast here since the only way to get to the instance subclass is via the query subclass
    _query = (T)spec.get().getQuery();
    _user = spec.get().getUser();
    _wdkModel = _query.getWdkModel();
    _spec = spec;
    _context = createContext();
  }

  private ReadOnlyMap<String, String> createContext() {
    Question question = _query.getContextQuestion();
    Param param = _query.getContextParam();
    return new ReadOnlyHashMap<>(
      new MapBuilder<String,String>()
        .put(Utilities.QUERY_CTX_USER, String.valueOf(_user.getUserId()))
        .put(Utilities.QUERY_CTX_QUERY, _query.getFullName())
        .putIf(question != null, Utilities.QUERY_CTX_QUESTION, () -> question.getFullName())
        .putIf(param != null, Utilities.QUERY_CTX_PARAM, () -> param.getFullName())
        .toMap());
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

  protected void setResultMessage(String resultMessage) {
    _resultMessage = resultMessage;
    _resultMessageSet = true;
  }

  public String getResultMessage() throws WdkModelException {
    if (!_resultMessageSet) {
      _resultMessage = new ResultFactory(_wdkModel).getResultMessage(this);
      _resultMessageSet = true;
    }
    return _resultMessage;
  }

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
      jsInstance.put("assignedWeight", _spec.get().getAssignedWeight());

      // include extra info from child
      appendJSONContent(jsInstance);

      LOG.debug("json:\n" + jsInstance.toString(2));

      return jsInstance;
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to build JSON object.", e);
    }
  }

  private Map<String, String> getSignatures() throws WdkModelException {
    Map<String, String> signatures = new HashMap<String, String>();
    for (Param param : _query.getParamMap().values()) {
      signatures.put(param.getName(), param.getSignature(_spec));
    }
    return signatures;
  }

  public JSONObject getParamSignatures() throws WdkModelException {
    // the values are dependent values. need to convert it into independent values
    Map<String, String> signatures = getSignatures();

    // build JSON from signatures; slightly different than new JSONObject(map)
    try {
      JSONObject jsParams = new JSONObject();
      for (String paramName : _spec.get().getQuery().getParamMap().keySet()) {
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
    return new ReadOnlyHashMap<String,String>(_spec.get().toMap());
  }

  private ResultList getCachedResults(boolean performSorting) throws WdkModelException {
    ResultFactory factory = new ResultFactory(_wdkModel);
    return factory.getCachedResults(this, performSorting);
  }

  protected String getCachedSql(boolean performSorting) throws WdkModelException {
    ResultFactory resultFactory = new ResultFactory(_wdkModel);
    return resultFactory.getCachedSql(this, performSorting);
  }

  protected Map<String, String> getParamInternalValues() throws WdkModelException {
    if (_paramInternalValues == null) {
      _paramInternalValues = new LinkedHashMap<String, String>();
      for (Param param : _query.getParamMap().values()) {
        _paramInternalValues.put(param.getName(), param.getInternalValue(_spec));
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
    return _spec.get().getAssignedWeight();
  }
}
