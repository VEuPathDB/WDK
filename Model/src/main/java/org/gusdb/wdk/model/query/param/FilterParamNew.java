package org.gusdb.wdk.model.query.param;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author steve
 * 
 *         The filter param is similar to a flatVocabParam in that it provides SQL suitable to embed in an
 *         IN clause.  The SQL returns a list of internal values, similar to flatVocabParam. 
 *         
 *         It is configured by two queries: ontologyQuery and metadataQuery.  The former returns a tree of categories
 *         and information describing them.  The latter maps internal values to entries in the ontology (such as age)
 *         and provides a value for the ontology entry (such as 18).
 *         
 *         Both these queries can be dependent.
 *               
 *         The input from the user is in the form of a set of filters.  each filter specifies an ontology entry
 *         and one or more values for it.  The param returns all internal values from the metadata query that match all
 *         supplied filters.  (Ie, the filtering is done on the backend).
 *         
 *         the filter param also provides summary information to the client about the distribution of
 *         values for a requested ontology entry.
 *         
 * 
 *         The values returned are:
 *         
 *           raw value: a JSON string of applied filters.
 * 
 *           stable value: same as raw value.
 * 
 *           signature: a checksum of the JSON string, but values are sorted
 * 
 *           internal: SQL to return a set of internal values from the metadata query
 * 
 */
public class FilterParamNew extends AbstractDependentParam {

  public static class OntologyCache extends ItemCache<String, Map<String, OntologyItem>> { }
  public static class MetadataNewCache extends ItemCache<String, Map<String, MetaDataItem>> { }
  public static class FilterParamNewCache extends ItemCache<String, FilterParamNewInstance> { }

  // ontology query columns
  static final String COLUMN_ONTOLOGY_ID = "ontology_term_name";
  static final String COLUMN_PARENT_ONTOLOGY_ID = "parent_ontology_term_name";
  static final String COLUMN_DISPLAY_NAME = "display_name";
  static final String COLUMN_DESCRIPTION = "description";
  static final String COLUMN_TYPE = "type";
  static final String COLUMN_UNITS = "units";
  static final String COLUMN_PRECISION = "precision";
  static final String COLUMN_IS_RANGE = "is_range";  
  
  // metadata query columns
  static final String COLUMN_INTERNAL = "internal"; // similar to the internal column in a flat vocab param
  static final String COLUMN_NUMBER_VALUE = "number_value";
  static final String COLUMN_DATE_VALUE = "date_value";
  static final String COLUMN_STRING_VALUE = "string_value";
  
  /**
   * convert a specification of type to the corresponding column name
   * @param type
   * @return
   */
  public static String typeToColumn(String type) { 
    switch(type) {
      case OntologyItem.TYPE_DATE:
      return COLUMN_DATE_VALUE;
      case OntologyItem.TYPE_NUMBER:
      return COLUMN_NUMBER_VALUE;
      case OntologyItem.TYPE_STRING:
      default:
      return COLUMN_STRING_VALUE;
    }
  }

  private static final int FETCH_SIZE = 1000;

  private String metadataQueryRef;
  private Query metadataQuery;

  private String ontologyQueryRef;
  private Query ontologyQuery;
  
  // remove non-terminal nodes with a single child
  private boolean trimMetadataTerms = true;

  public FilterParamNew() {
    // register handlers
    setHandler(new FilterParamNewHandler());
  }

  /**
   * @param param
   */
  public FilterParamNew(FilterParamNew param) {
    super(param);
    this.metadataQueryRef = param.metadataQueryRef;
    if (param.metadataQuery != null)
      this.metadataQuery = param.metadataQuery.clone();
    this.ontologyQueryRef = param.ontologyQueryRef;
    if (param.ontologyQuery != null)
      this.ontologyQuery = param.ontologyQuery.clone();
    this.trimMetadataTerms = param.trimMetadataTerms;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#clone()
   */
  @Override
  public Param clone() {
    return new FilterParamNew(this);
  }

  /**
   * @return the metadata query ref
   */
  public String getMetadataQueryRef() {
    return metadataQueryRef;
  }

  /**
   * @param metadataQueryRef
   *          the metadata query ref to set
   */
  public void setMetadataQueryRef(String metadataQueryRef) {
    this.metadataQueryRef = metadataQueryRef;
  }

  /**
   * @return the metadataQuery
   */
  public Query getMetadataQuery() {
    return metadataQuery;
  }

  public void setMetadataQuery(Query metadataQuery) {
    this.metadataQuery = metadataQuery;
  }

  /**
   * @return the onotology Query Name
   */
  public String getOntologyQueryRef() {
    return ontologyQueryRef;
  }

  /**
   * @param ontologyQueryRef
   *          the ontologyName to set
   */
  public void setOntologyQueryRef(String ontologyQueryRef) {
    this.ontologyQueryRef = ontologyQueryRef;
  }

  /**
   * @return the ontologyQuery
   */
  public Query getOntologyQuery() {
    return ontologyQuery;
  }

  /**
   * @param ontologyQuery the ontologyQuery to set
   */
  public void setOntologyQuery(Query ontologyQuery) {
    this.ontologyQuery = ontologyQuery;
  }


  /**
   * @return the trimMetadataTerms
   * TODO: either add this to service layer or remove
   */
  public boolean getTrimMetadataTerms() {
    return trimMetadataTerms;
  }

  /**
   * @param trimMetadataTerms
   *          the metadataQuery to set
   */
  public void setTrimMetadataTerms(boolean trimMetadataTerms) {
    this.trimMetadataTerms = trimMetadataTerms;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // resolve ontology query
    if (ontologyQueryRef != null) {
      
      // validate dependent params
      this.ontologyQuery = resolveDependentQuery(model, ontologyQueryRef, "ontology query");

      // validate columns
      Map<String, Column> columns = ontologyQuery.getColumnMap();
      String[] cols = { COLUMN_ONTOLOGY_ID, COLUMN_PARENT_ONTOLOGY_ID, COLUMN_DISPLAY_NAME, COLUMN_DESCRIPTION, COLUMN_TYPE, COLUMN_UNITS, COLUMN_PRECISION};
      for (String col : cols)
        if (!columns.containsKey(col))
        throw new WdkModelException("The ontologyQuery " + ontologyQueryRef + " in filterParam " +
            getFullName() + " must include column: " + col);
    }

    // resolve metadata query
    if (metadataQueryRef != null) {
      
      // validate dependent params
      this.metadataQuery = resolveDependentQuery(model, metadataQueryRef, "metadata query");

      // validate columns.
      Map<String, Column> columns = metadataQuery.getColumnMap();
      String[] cols = { COLUMN_INTERNAL, COLUMN_STRING_VALUE, COLUMN_NUMBER_VALUE, COLUMN_DATE_VALUE };
      for (String col : cols)
        if (!columns.containsKey(col))
          throw new WdkModelException("The metadata query " + metadataQueryRef + " in filterParam " +
              getFullName() + " must include column: " + col);
    }
  }

  /**
   * We cache this because typically ontologies are sensitive only to the grossest dependent param (eg, dataset), so will
   * be reused across users.
   * @return <propertyName, <infoKey, infoValue>>, or null if metadataQuery is not specified
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<String, OntologyItem> getOntology(User user, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    
    OntologyItemNewFetcher fetcher = new OntologyItemNewFetcher(ontologyQuery, contextParamValues, user);
    Map<String, OntologyItem> map = null;
    try {
      map = CacheMgr.get().getOntologyNewCache().getItem(fetcher.getCacheKey(), fetcher);
    }
    catch (UnfetchableItemException ex) {
      decodeException(ex);
    }
    return map;
  }
  
  /**
   * 
   * @param user
   * @param contextParamValues holds the depended param values (and possibly others)
   * @param appliedFilters (in stable value format)
   * @return { totalCount: number; filteredCount: number; }
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public FilterParamSummaryCounts getTotalsSummary(User user, Map<String, String> contextParamValues, JSONObject appliedFilters) throws WdkModelException, WdkUserException {

    /* GET UNFILTERED (TOTAL) COUNTS */
    // get base metadata query
    QueryInstance<?> queryInstance = metadataQuery.makeInstance(user, contextParamValues, true, 0, new HashMap<String, String>());
    String metadataSql = queryInstance.getSql();
    
    // reduce it to a set of distinct internals
    // we know that each ontology_term_id has a full set of internals, so we just need to query 
    // one ontology_term_id.
    String distinctInternalsSql = "SELECT distinct md." + COLUMN_INTERNAL + " FROM (" + metadataSql + ") md" 
          + " WHERE md." + COLUMN_ONTOLOGY_ID + " IN (select " + COLUMN_ONTOLOGY_ID + " from (" + metadataSql + ") where rownum = 1)";
    
    // get count
    String sql = "select count(*) as cnt from (" + distinctInternalsSql + ")";
    FilterParamSummaryCounts fpsc = new FilterParamSummaryCounts();
    fpsc.unfilteredCount = runCountSql(sql);


    /* GET FILTERED COUNTS */
    // sql to find the filtered count
    String filteredInternalsSql = FilterParamNewHandler.toInternalValue(user, appliedFilters, contextParamValues, this);

    // get count
    sql = "select count(*) as CNT from (" + filteredInternalsSql + ")";

    fpsc.unfilteredCount = runCountSql(sql);
    
    return fpsc;
  }
  
  private long runCountSql(String sql) {
    Object[] args = {};
    BasicResultSetHandler handler = new BasicResultSetHandler();
    new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "invalid-step-report-summary").executeQuery(args, handler);
    List<Map<String,Object>> results = handler.getResults();
    Map<String,Object> row = results.get(0);
    return ((BigDecimal)row.get("CNT")).toBigInteger().longValue();
  }
  
  /**
   * @param user
   * @param contextParamValues
   * @param ontologyId
   * @param appliedFilters
   * @return
   * @throws WdkUserException 
   * @throws WdkModelException 
   */
  public Map<String, FilterParamSummaryCounts> getOntologyTermSummary(User user, Map<String, String> contextParamValues, String ontologyId, JSONObject appliedFilters) throws WdkModelException, WdkUserException {

    FilterParamNewInstance paramInstance = createFilterParamNewInstance(user, contextParamValues);

    /* GET UNFILTERED COUNTS */
    // get base metadata query
    QueryInstance<?> queryInstance = metadataQuery.makeInstance(user, contextParamValues, true, 0, new HashMap<String, String>());
    String metadataSql = queryInstance.getSql();
    
    // limit it to our ontology_id
    String metadataSqlPerOntologyId = "SELECT mq.* FROM (" + metadataSql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID + " = ?";

    // read into a map of internal -> value(s) 
    Map<String, List<String>> unfiltered = getMetaData(user, contextParamValues, ontologyId, paramInstance, metadataSqlPerOntologyId);
    
    // get histogram of those, stored in JSON 
    Map<String, FilterParamSummaryCounts> summaryMap = new HashMap<String, FilterParamSummaryCounts>();
    getSummaryCounts(unfiltered, summaryMap, false);  // stuff in to 0th position in array
    
    
    /* GET FILTERED COUNTS */
    // get sql for the set internal ids that are pruned by the filters
    String internalSql = FilterParamNewHandler.toInternalValue(user, appliedFilters, contextParamValues, this);
    
    // use that set of ids to limit our ontology id's metadata 
    String metadataSqlPerOntologyIdFiltered = metadataSqlPerOntologyId + "where internal in (" + internalSql + ")";
    
    // read this filtered set into map of internal -> value(s)
    Map<String, List<String>> filtered = getMetaData(user, contextParamValues, ontologyId, paramInstance, metadataSqlPerOntologyIdFiltered);
    
    // add the filtered set into the histogram
    getSummaryCounts(filtered, summaryMap, true); // stuff in to 1st position in array
    
    return summaryMap;
  }
  
  public class FilterParamSummaryCounts {
    public long unfilteredCount;
    public long filteredCount;
  }
  
  /**
   * stuff counts per ontology term value into json structure.  first pair position is unfiltered, second is filtered
   * @param metadataForOntologyId
   * @param counts
   * @param pairPosition
   */
  private void getSummaryCounts(Map<String, List<String>> metadataForOntologyId, Map<String, FilterParamSummaryCounts> summary, boolean filtered) {
    
    for (List<String> values : metadataForOntologyId.values()) {
      for (String value : values) {
        FilterParamSummaryCounts counts;
        if (summary.containsKey(value)) counts = summary.get(value);
        else counts = new FilterParamSummaryCounts();
        if (filtered) counts.filteredCount++;
        else counts.unfilteredCount++;
      }
    }
    
  }

  private void decodeException(UnfetchableItemException ex) throws WdkModelException, WdkUserException {
    Throwable nestedException = ex.getCause();
    if (nestedException == null) throw new WdkModelException(ex.getMessage());
    if (nestedException instanceof WdkModelException) throw (WdkModelException) nestedException;
    if (nestedException instanceof WdkUserException) throw (WdkUserException) nestedException;
    throw new WdkModelException(nestedException);
  }

  /**
   * get an in-memory copy of meta data for a specified ontology_id
   * @param user
   * @param contextParamValues
   * @param property
   * @param cache
   *          the cache is needed, to make sure the contextParamValues are initialized correctly. (it is
   *          initialized when a cache is created.)
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  private Map<String, List<String>> getMetaData(User user, Map<String, String> contextParamValues, String ontologyId,
      FilterParamNewInstance cache, String sql) throws WdkModelException, WdkUserException {
 
    sql = "SELECT mq.* FROM (" + sql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID + " = ?";

    // run the composed sql, and get the metadata back
    Map<String, List<String>> metadata = new LinkedHashMap<>();
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    try {
      ps = SqlUtils.getPreparedStatement(dataSource, sql);
      ps.setFetchSize(FETCH_SIZE);
      ps.setString(1, ontologyId);
      resultSet = ps.executeQuery();
      while (resultSet.next()) {
        String internal = resultSet.getString(COLUMN_INTERNAL);
        String dateVal = resultSet.getString(COLUMN_DATE_VALUE);
        String stringVal = resultSet.getString(COLUMN_STRING_VALUE);
        String numberVal = resultSet.getString(COLUMN_NUMBER_VALUE);
        
        List<String> values = metadata.get(internal);

        if (values == null) {
          values = new ArrayList<String>();
          metadata.put(internal, values);
        }

        // one of these should be non-null.
        String value = dateVal;
        if (stringVal != null) value = stringVal;
        if (numberVal != null) value = numberVal;
        
        values.add(value);
      }
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }

    return metadata;
  }

  public JSONObject getJsonValues(User user, Map<String, String> contextParamValues, FilterParamNewInstance cache)
      throws WdkModelException, WdkUserException {
    
    JSONObject jsParam = new JSONObject();
    
    try { 
      // create json for the ontology
      JSONObject jsOntology = new JSONObject();
      Map<String, OntologyItem> ontologyMap = getOntology(user, contextParamValues);
      for (String itemName : ontologyMap.keySet()) {
        OntologyItem item = ontologyMap.get(itemName);
        JSONObject jsOntoItem = new JSONObject();
        jsOntoItem.put(COLUMN_PARENT_ONTOLOGY_ID, item.getParentOntologyId());
        jsOntoItem.put(COLUMN_DISPLAY_NAME, item.getDisplayName());
        jsOntoItem.put(COLUMN_DESCRIPTION, item.getDescription());
        jsOntoItem.put(COLUMN_TYPE, item.getType());
        jsOntoItem.put(COLUMN_UNITS, item.getUnits());
        jsOntoItem.put(COLUMN_PRECISION, item.getPrecision());
        
        jsOntology.put(itemName, jsOntoItem);
      }
      jsParam.put("ontology", jsOntology);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsParam;
  }
  
  /**
   * remove invalid filters from stableValue.  if stableValue empty, use default.
   */

  protected String getValidStableValue(User user, String stableValue, Map<String, String> contextParamValues,
      FilterParamNewInstance cache) throws WdkModelException {
    try {
      if (stableValue == null || stableValue.length() == 0) {
        JSONObject jsNewStableValue = new JSONObject();
        jsNewStableValue.put(FilterParamHandler.FILTERS_KEY, new JSONArray());
        return jsNewStableValue.toString();
      }

      JSONObject jsStableValue = new JSONObject(stableValue);
 
      // TODO: phase 2 clean up stable value
      // need a cached version of uniqueMetadataStringValues query
      // for each filter in the input stableValue, check that:
      //  key is in the ontology
      //  that value type matches ontology declared value type
      //  if string value, that value is in known string values
      
      return jsStableValue.toString();
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }

  @Override
  public String getDefault() throws WdkModelException {
    String defaultValue = new JSONObject().toString();
    return defaultValue;
  }

  public String getDefault(User user, Map<String, String> contextParamValues) throws WdkModelException {
    // TODO phase 2 fix this.
    String defaultValue = new JSONObject().toString();
    return defaultValue;
  }

  @Override
  public void setContextQuestion(Question question)  throws WdkModelException {
    
    super.setContextQuestion(question);
    
    // also set context question in the metadata & ontology queries
    if (metadataQuery != null)
      metadataQuery.setContextQuestion(question);
    if (ontologyQuery != null)
      ontologyQuery.setContextQuestion(question);
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    // TODO phase 2 probably format this nicely
    return rawValue.toString();
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) { }

  /**
   * check each filter in stable value.  add each invalid one to error message, and throw user exception.
   */
  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    // TODO phase 2 validate against ontology and metadata
    
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    // TODO phase 2 - implement if we want filters to cause invalidated steps?
    
  }

  private FilterParamNewInstance createFilterParamNewInstance(User user,
      Map<String, String> dependedParamValues) throws WdkModelException, WdkUserException {
    try {
      FilterParamNewFetcher fetcher = new FilterParamNewFetcher(user, this);

      CacheMgr.get().getFilterParamNewCache().getItem(fetcher.getCacheKey(dependedParamValues), fetcher);
    }
    catch (UnfetchableItemException e) {
      throw new WdkModelException(e);
    }

    return null;
  }
  
  @Override
  protected DependentParamInstance createDependentParamInstance (User user, Map<String, String> dependedParamValues)
      throws WdkModelException, WdkUserException {
    return createFilterParamNewInstance(user,  dependedParamValues);
  }

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) {
    // TODO phase 2 
    return null;
  }


}
 
