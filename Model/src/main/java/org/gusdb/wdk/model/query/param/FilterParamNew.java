package org.gusdb.wdk.model.query.param;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.cache.SqlCountCache;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * The filter param is similar to a flatVocabParam in that it provides SQL suitable to embed in an IN clause.
 * The SQL returns a list of internal values, similar to flatVocabParam.
 * 
 * It is configured by two queries: ontologyQuery and metadataQuery. The former returns a tree of categories
 * and information describing them. The latter maps internal values to entries in the ontology (such as age)
 * and provides a value for the ontology entry (such as 18).
 * 
 * Both these queries can be dependent.
 * 
 * The input from the user is in the form of a set of filters. each filter specifies an ontology entry and one
 * or more values for it. The param returns all internal values from the metadata query that match all
 * supplied filters (i.e. the filtering is done on the backend).
 * 
 * the filter param also provides summary information to the client about the distribution of values for a
 * requested ontology entry.
 * 
 * The values returned are:
 * 
 * raw value: a JSON string of applied filters
 * 
 * stable value: same as raw value
 * 
 * signature: a checksum of the JSON string, but values are sorted
 * 
 * internal: SQL to return a set of internal values from the metadata query
 * 
 * @author steve
 */
public class FilterParamNew extends AbstractDependentParam {

  private static final Logger LOG = Logger.getLogger(FilterParamNew.class);

  public static class OntologyCache extends InMemoryCache<String, Map<String, OntologyItem>> {}

  public static class MetadataNewCache extends InMemoryCache<String, Map<String, MetaDataItem>> {}

  public static class FilterParamNewCache extends InMemoryCache<String, FilterParamNewInstance> {}

  public static class FilterParamSummaryCounts {
    public long unfilteredFilterItemCount = 0;
    public long filteredFilterItemCount = 0;
    public long unfilteredRecordCount = 0;
    public long filteredRecordCount = 0;
  }

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
  static final String COLUMN_FILTER_ITEM_ID = "filter_item_id"; // (optional) id to filter on, if different than internal
  static final String COLUMN_RECORD_ID = "record_id"; // (optional) id to count records, if different than internal
  
  private static final int FETCH_SIZE = 1000;

  private String _metadataQueryRef;
  private Query _metadataQuery;

  private String _ontologyQueryRef;
  private Query _ontologyQuery;

  private String _backgroundQueryRef;
  private Query _backgroundQuery;
  
  private String _filterDataTypeDisplayName;

  private String _filterItemIdColumn = COLUMN_INTERNAL;  // default to internal
  private String _recordIdColumn = COLUMN_INTERNAL;  // default to internal
  
  // the output count of this param can be used to predict the final search results count, if all downstream
  // params (in dependency tree) have default values
  private boolean _countPredictsAnswerCount = false;
  
  // Minumum number of selected items required. Defaults to 1.
  private int _minSelectedCount = -1;

  // remove non-terminal nodes with a single child
  private boolean _trimMetadataTerms = true;

  public FilterParamNew() {
    // register handlers
    setHandler(new FilterParamNewHandler());
  }

  /**
   * @param param
   */
  public FilterParamNew(FilterParamNew param) {
    super(param);

    _metadataQueryRef = param._metadataQueryRef;
     if (param._metadataQuery != null)
       _metadataQuery = param._metadataQuery.clone();

    _ontologyQueryRef = param._ontologyQueryRef;
    if (param._ontologyQuery != null)
      _ontologyQuery = param._ontologyQuery.clone();

    _backgroundQueryRef = param._backgroundQueryRef;
    if (param._backgroundQuery != null)
      _backgroundQuery = param._backgroundQuery.clone();
    
    _trimMetadataTerms = param._trimMetadataTerms;
    _filterDataTypeDisplayName = param._filterDataTypeDisplayName;
    _filterItemIdColumn = param._filterItemIdColumn;
    _recordIdColumn = param._recordIdColumn;
    _countPredictsAnswerCount = param._countPredictsAnswerCount;
    _minSelectedCount = param._minSelectedCount;
  }

  @Override
  public Set<String> getContainedQueryFullNames() {
    Set<String> names = new HashSet<String>();
    names.add(_metadataQueryRef);
    names.add(_ontologyQueryRef);
    names.add(_backgroundQueryRef);
 
    return names;
  }

  @Override
  public Param clone() {
    return new FilterParamNew(this);
  }

  /**
   * @return the metadata query ref
   */
  public String getMetadataQueryRef() {
    return _metadataQueryRef;
  }

  /**
   * @param metadataQueryRef
   *          the metadata query ref to set
   */
  public void setMetadataQueryRef(String metadataQueryRef) {
    _metadataQueryRef = metadataQueryRef;
  }

  /**
   * @return the metadataQuery
   */
  public Query getMetadataQuery() {
    return _metadataQuery;
  }

  public void setMetadataQuery(Query metadataQuery) {
    _metadataQuery = metadataQuery;
  }

  /**
   * @return the onotology Query Name
   */
  public String getOntologyQueryRef() {
    return _ontologyQueryRef;
  }

  /**
   * @param ontologyQueryRef
   *          the ontologyName to set
   */
  public void setOntologyQueryRef(String ontologyQueryRef) {
    _ontologyQueryRef = ontologyQueryRef;
  }

  /**
   * @return the ontologyQuery
   */
  public Query getOntologyQuery() {
    return _ontologyQuery;
  }

  /**
   * @param ontologyQuery
   *          the ontologyQuery to set
   */
  public void setOntologyQuery(Query ontologyQuery) {
    _ontologyQuery = ontologyQuery;
  }


  public String getBackgroundQueryRef() {
    return _backgroundQueryRef;
  }

  /**
   * @param backgroundQueryRef
   *          the backgroundName to set
   */
  public void setBackgroundQueryRef(String backgroundQueryRef) {
    _backgroundQueryRef = backgroundQueryRef;
  }

  /**
   * @return the backgroundQuery
   */
  public Query getBackgroundQuery() {
    return _backgroundQuery;
  }

  /**
   * @param backgroundQuery
   *          the backgroundQuery to set
   */
  public void setBackgroundQuery(Query backgroundQuery) {
    _backgroundQuery = backgroundQuery;
  }
  
  /**
   * @param trimMetadataTerms
   *          the metadataQuery to set
   */
  public void setTrimMetadataTerms(boolean trimMetadataTerms) {
    _trimMetadataTerms = trimMetadataTerms;
  }

  /**
   * @return the trimMetadataTerms
   */
  public boolean getTrimMetadataTerms() {
    return _trimMetadataTerms;
  }

  public void setFilterDataTypeDisplayName(String filterDataTypeDisplayName) {
    _filterDataTypeDisplayName = filterDataTypeDisplayName;
  }

  public String getFilterDataTypeDisplayName() {
    return _filterDataTypeDisplayName;
  }
  
  public boolean getCountPredictsAnswerCount() {
    return _countPredictsAnswerCount;
  }
  
  public void setCountPredictsAnswerCount(boolean canUse) {
    _countPredictsAnswerCount = canUse;
  }
  
  public int getMinSelectedCount() {
    return _minSelectedCount;
  }
  
  public void setMinSelectedCount(int count) {
    _minSelectedCount = count;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    ///////////////////////////
    // resolve ontology query
    ///////////////////////////
    
    // for mysterious reasons, we need these tests for null, even though this is a required query ref.  some resolve res weirdness
    if (_ontologyQueryRef != null) {
    // validate dependent params
    _ontologyQuery = resolveDependentQuery(model, _ontologyQueryRef, "ontology query");

    // validate columns
    String errMsgPrefix = "The ontologyQuery " + _ontologyQueryRef + " in filterParam " + getFullName();
    Map<String, Column> columns = _ontologyQuery.getColumnMap();
    String[] cols = { COLUMN_ONTOLOGY_ID, COLUMN_PARENT_ONTOLOGY_ID, COLUMN_DISPLAY_NAME, COLUMN_DESCRIPTION,
              COLUMN_TYPE, COLUMN_UNITS, COLUMN_PRECISION,COLUMN_IS_RANGE };
    if (columns.size() != cols.length)
      throw new WdkModelException(errMsgPrefix + " has the wrong number of columns (" + columns.size() + ").  It should have " + String.join(",", cols));
    for (String col : cols)
      if (!columns.containsKey(col))
        throw new WdkModelException(errMsgPrefix + " must include column: " + col);
    }
    ///////////////////////////
    // resolve metadata query
    ///////////////////////////

    List<String> metadataCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    metadataCols.add(COLUMN_INTERNAL);
    metadataCols.add(COLUMN_ONTOLOGY_ID);

    if (_metadataQueryRef != null) {
    // validate dependent params
    _metadataQuery = resolveDependentQuery(model, _metadataQueryRef, "metadata query");

    // validate columns.
    String errMsgPrefix = "The metadataQuery " + _metadataQueryRef + " in filterParam " + getFullName();

    Map<String, Column> columns = _metadataQuery.getColumnMap();
    if (columns.containsKey(COLUMN_FILTER_ITEM_ID)) { // if model includes this optional column, use it
      metadataCols.add(COLUMN_FILTER_ITEM_ID);
      _filterItemIdColumn = COLUMN_FILTER_ITEM_ID;
    }
    if (columns.containsKey(COLUMN_RECORD_ID)) { // if model includes this optional column, use it
      metadataCols.add(COLUMN_RECORD_ID);
      _recordIdColumn = COLUMN_RECORD_ID;
    }
    if (columns.size() != metadataCols.size())
      throw new WdkModelException(errMsgPrefix + " has the wrong number of columns (" + columns.size() + ").  It should have " + String.join(",", metadataCols));

    for (String col : metadataCols)
      if (!columns.containsKey(col))
        throw new WdkModelException(errMsgPrefix + " must include column: " + col);    
    }
    ////////////////////////////////////
    // resolve background query
    ////////////////////////////////////

    if (_backgroundQueryRef != null) {

      // validate dependent params
      _backgroundQuery = resolveDependentQuery(model, _backgroundQueryRef, "background query");

      // validate columns
      String errMsgPrefix = "The backgroundQuery " + _backgroundQueryRef + " in filterParam " + getFullName();

      // required to have identical columns as metadata query
      Map<String, Column> columns = _backgroundQuery.getColumnMap();
      if (columns.size() != metadataCols.size())
        throw new WdkModelException(errMsgPrefix + " has the wrong number of columns (" + columns.size() + "). It should have " + String.join(",", metadataCols));

      for (String col : metadataCols)
        if (!columns.containsKey(col))
          throw new WdkModelException(errMsgPrefix + " must include column: " + col);
    }
  }

  /**
   * We cache this because typically ontologies are sensitive only to the grossest dependent param (eg,
   * dataset), so will be reused across users.
   * 
   * @return <propertyName, <infoKey, infoValue>> 
   * @throws WdkModelException
   */
  public Map<String, OntologyItem> getOntology(User user, Map<String, String> contextParamValues)
      throws WdkModelException {

    contextParamValues = ensureRequiredContext(user, contextParamValues);

    OntologyItemNewFetcher fetcher = new OntologyItemNewFetcher(_ontologyQuery, contextParamValues, user);
    Map<String, OntologyItem> map = null;
    try {
      map = CacheMgr.get().getOntologyNewCache().getValue(fetcher.getCacheKey(), fetcher);
    }
    catch (ValueProductionException ex) {
      decodeException(ex);
    }
    return map;
  }

  /**
   * 
   * @param user
   * @param contextParamValues
   *          holds the depended param values (and possibly others)
   * @param appliedFilters
   *          (in stable value format)
   * @return { totalCount: number; filteredCount: number; }
   * @throws WdkModelException
   */
  public FilterParamSummaryCounts getTotalsSummary(User user, Map<String, String> contextParamValues,
      JSONObject appliedFilters) throws WdkModelException {

    contextParamValues = ensureRequiredContext(user, contextParamValues);

    ////////////////////////////////////////
    /* GET UNFILTERED (BACKGROUND) COUNTS */
    ////////////////////////////////////////
    
    String bgdSql;
    try {
      QueryInstance<?> queryInstance = _backgroundQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      bgdSql = queryInstance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    // Set up counts store
    FilterParamSummaryCounts fpsc = new FilterParamSummaryCounts();

    // get unfiltered count of filter items
    String sql = "SELECT count(distinct md." + _filterItemIdColumn + ") FROM (" + bgdSql + ") md";
    fpsc.unfilteredFilterItemCount = runCountSql(sql, "filterItemIdsCount-unfiltered");

    // get unfiltered count of records, unless the record id column is the same as the filter item id column
    if (_recordIdColumn.equals(_filterItemIdColumn)) {
      fpsc.unfilteredRecordCount = fpsc.unfilteredFilterItemCount;
    } else {
      sql = "SELECT count (distinct md." + _recordIdColumn + ") FROM (" + bgdSql + ") md";
      fpsc.unfilteredRecordCount = runCountSql(sql, "recordIds-unfiltered");
    }

    /////////////////////////
    /* GET FILTERED COUNTS */
    /////////////////////////
    
    // sql to find the filtered count
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);
    
    // get filtered   filter_item_ids  count
    String filteredItemIdsSql = getFilteredIdsSql(user, stableValue, contextParamValues, _metadataQuery, _filterItemIdColumn);
    sql = "select count( distinct " + _filterItemIdColumn + ") as CNT from (" + filteredItemIdsSql + ")";
    fpsc.filteredFilterItemCount = runCountSql(sql, "filterItemIds-filtered");

    if (_filterItemIdColumn.equals(_recordIdColumn)) {
      fpsc.filteredRecordCount = fpsc.filteredFilterItemCount;
    } else {
      String filteredRecordIdsSql = getFilteredIdsSql(user, stableValue, contextParamValues, _metadataQuery, _recordIdColumn);
      sql = "select count( distinct " + _recordIdColumn + ") as CNT from (" + filteredRecordIdsSql + ")";
      fpsc.filteredRecordCount = runCountSql(sql, "recordIds-filtered");
    }
    return fpsc;
  }

  private long runCountSql(String sql, String qName) throws WdkModelException {
    SqlCountCache cache = CacheMgr.get().getSqlCountCache(_wdkModel.getAppDb());
    try {
      return cache.getItem(sql, "filter-param-counts-" + qName);
    } catch (ValueProductionException e) { throw new WdkModelException(e); }
  }

  /**
   * @param user
   * @param contextParamValues
   * @param ontologyId
   * @param appliedFilters
   * @param <T>
   *          The type of the values
   * @return a map from a value for this ontology term to the counts for that value.
   * @throws WdkModelException
   * TODO: MULTI-FILTER upgrade:  take a list of ontology terms, and return a map of maps, one per term.
   */

  public <T> OntologyTermSummary<T> getOntologyTermSummary(User user,
      Map<String, String> contextParamValues, OntologyItem ontologyItem, JSONObject appliedFilters,
      Class<T> ontologyItemClass) throws WdkModelException {

    contextParamValues = ensureRequiredContext(user, contextParamValues);

    ////////////////////////////////////////////
    /* GET UNFILTERED MAP AND POPULATE COUNTS */
    ////////////////////////////////////////////

    String bgdSql;
    try {
      QueryInstance<?> queryInstance = _backgroundQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      bgdSql = queryInstance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    // limit it to our ontology_id
    List<String> metadataCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    metadataCols.add(_filterItemIdColumn);
    metadataCols.add(COLUMN_ONTOLOGY_ID);
    String cols = metadataCols.stream().collect(Collectors.joining(", "));
    String unfilteredSqlPerOntologyId = "SELECT distinct " + cols + " FROM (" + bgdSql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID +
        " = ?";
    
    // read into a map of filter_item_id -> value(s)
    Map<T, Long> unfiltered = countMetaDataForOntologyTerm(user, contextParamValues, ontologyItem,
        unfilteredSqlPerOntologyId, ontologyItemClass);

    // get histogram of those, stored in JSON
    Map<T, FilterParamSummaryCounts> summaryCountsMap = new HashMap<>();
    populateSummaryCounts(unfiltered, summaryCountsMap, false); // stuff in to 0th position in array

    //////////////////////////////////////////
    /* GET FILTERED MAP AND POPULATE COUNTS */
    //////////////////////////////////////////
    
    // get sql for the set internal ids that are pruned by the filters
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);
    String filteredFilterItemIdSql = getFilteredIdsSql(user, stableValue, contextParamValues, getMetadataQuery(), _filterItemIdColumn,
        " where " + COLUMN_ONTOLOGY_ID + " = '" + ontologyItem.getOntologyId().replaceAll("'", "''") + "'");

    // use that set of ids to limit our ontology id's metadata
    String andClause = " AND " + _filterItemIdColumn + " in ( select " + _filterItemIdColumn + " from (" + filteredFilterItemIdSql + "))";
    String filteredSqlPerOntologyId = unfilteredSqlPerOntologyId + andClause;

    // read this filtered set into map of internal -> value(s)
    Map<T, Long> filtered = countMetaDataForOntologyTerm(user, contextParamValues, ontologyItem,
        filteredSqlPerOntologyId, ontologyItemClass);

    // add the filtered set into the histogram
    populateSummaryCounts(filtered, summaryCountsMap, true); // stuff in to 1st position in array

    OntologyTermSummary <T> summary = new OntologyTermSummary<T>(summaryCountsMap);
    
    /////////////////////////////////////
    /* GET DISTINCT FILTER ITEM COUNTS */
    /////////////////////////////////////
    String oneColumnBgdSql = "select " + _filterItemIdColumn + " from (" + bgdSql + ") bq WHERE bq." + COLUMN_ONTOLOGY_ID +  " = ?";
    String unfilteredDistinctFilterItemsSql = "SELECT count (distinct " + _filterItemIdColumn + ") as cnt FROM (" + oneColumnBgdSql + ")";

    String filteredDistinctFilterItemsSql = "SELECT count (distinct " + _filterItemIdColumn + ") as cnt FROM (" + oneColumnBgdSql +
        " INTERSECT select " + _filterItemIdColumn + " from (" + filteredFilterItemIdSql + "))";

    getCountsOfDistinctFilterItems(summary, unfilteredDistinctFilterItemsSql, filteredDistinctFilterItemsSql, ontologyItem.getOntologyId());

    // return the whole bundle
    return summary;
  }

  
  /**
   * update the provided OntologyTermSummary with counts of internals.
   * @param summary
   * @param unfilteredDistinctFilterItemsSql
   * @param filteredDistinctFilterItemsSql
   * @param ontologyId
   * @throws WdkModelException
   */
  private <T> void getCountsOfDistinctFilterItems(OntologyTermSummary <T> summary, String unfilteredDistinctFilterItemsSql, 
      String filteredDistinctFilterItemsSql, String ontologyId) throws WdkModelException {
 
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    PreparedStatement ps = null;
    ResultSet resultSet = null;

    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(dataSource, unfilteredDistinctFilterItemsSql);
      ps.setString(1, ontologyId);
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(unfilteredDistinctFilterItemsSql, "FilterParamNew-getDistinctCounts-unfiltered", start, resultSet);
      resultSet.next();
      BigDecimal count = resultSet.getBigDecimal(1);
      summary.setDistinctInternal(count.intValue());
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }
  
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(dataSource, filteredDistinctFilterItemsSql);
      ps.setString(1, ontologyId);
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(filteredDistinctFilterItemsSql, "FilterParamNew-getDistinctCounts-filtered", start, resultSet);
      resultSet.next();
      BigDecimal count = resultSet.getBigDecimal(1);
      summary.setDistinctMatchingInternal(count.intValue());
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }

  }
  
  public class OntologyTermSummary <T> {
    OntologyTermSummary(Map<T, FilterParamSummaryCounts> summaryCounts) {
      _summaryCounts = summaryCounts;
    }
    void setDistinctInternal(int di) { _distinctInternal = di; }
    void setDistinctMatchingInternal(int dmi) { _distinctMatchingInternal = dmi; }
    private Map<T, FilterParamSummaryCounts> _summaryCounts;
    private int _distinctInternal;
    private int _distinctMatchingInternal;
    public Map<T, FilterParamSummaryCounts> getSummaryCounts() { return _summaryCounts; }
    public int getDistinctInternal() { return _distinctInternal; }
    public int getDistinctMatchingInternal() { return _distinctMatchingInternal; }

  }

  /**
   * stuff counts per ontology term value into json structure. first pair position is unfiltered, second is
   * filtered
   * 
   */
  private <T> void populateSummaryCounts(Map<T, Long> countsMap, Map<T, FilterParamSummaryCounts> summary,
      boolean filtered) {

    for (T value : countsMap.keySet()) {

      FilterParamSummaryCounts counts;
      if (summary.containsKey(value)) {
        counts = summary.get(value);
      }
      else {
        counts = new FilterParamSummaryCounts();
        summary.put(value, counts);
      }
      if (filtered)
        counts.filteredFilterItemCount = countsMap.get(value);
      else
        counts.unfilteredFilterItemCount = countsMap.get(value);
    }
  }

  private void decodeException(ValueProductionException ex) throws WdkModelException {
    Throwable nestedException = ex.getCause();
    if (nestedException == null)
      throw new WdkModelException(ex.getMessage());
    if (nestedException instanceof WdkModelException)
      throw (WdkModelException) nestedException;
    throw new WdkModelException(nestedException);
  }

  
  /**
   * for a specified ontology term, return a map of value -> count of that value
   * @param user
   * @param contextParamValues
   * @param ontologyItem
   * @param cache
   * @param metaDataSql
   * @param ontologyItemClass
   * @return
   * @throws WdkModelException
   */
  private <T> Map<T, Long> countMetaDataForOntologyTerm(User user, Map<String, String> contextParamValues,
      OntologyItem ontologyItem, String metaDataSql, Class<T> ontologyItemClass)
      throws WdkModelException {
    
    String valueColumnName = ontologyItem.getType().getMetadataQueryColumn();

    String sql = "SELECT " + valueColumnName + ", count(*) as CNT FROM (" + metaDataSql + ") mq WHERE mq."
    + COLUMN_ONTOLOGY_ID + " = ? GROUP BY " + valueColumnName;

     Map<T, Long> metadata = new LinkedHashMap<>();
    
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    try {
      ps = SqlUtils.getPreparedStatement(dataSource, sql);
      ps.setFetchSize(FETCH_SIZE);
      ps.setString(1, ontologyItem.getOntologyId());
      ps.setString(2, ontologyItem.getOntologyId());
      long start = System.currentTimeMillis();
      resultSet = ps.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, "FilterParamNew-countMetaDataForOntologyTerm", start, resultSet);
      while (resultSet.next()) {
        T value = OntologyItemType.resolveTypedValue(resultSet, ontologyItem, ontologyItemClass);
        Long count = resultSet.getLong("CNT");
        metadata.put(value, count);
      }
   }
    catch (SQLException ex) {
      throw new WdkModelException(sql, ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, ps);
    }

    return metadata;
  }
  

  
  public Map<String, Set<String>> getValuesMap(User user,
      Map<String, String> contextParamValues) throws WdkModelException {
    
    contextParamValues = ensureRequiredContext(user, contextParamValues);

    Map<String, OntologyItem> ontology = getOntology(user, contextParamValues);
    return getValuesMap(user, contextParamValues, null, ontology);
  }
  
  /**
   * return map of ontology field name to values (as strings), for the provided list of ontology terms
   * 
   * @param user
   * @param contextParamValues
   * @param ontologyTerms
   *          a list of ontology term to include in result.  NULL to use all terms in ontologyValues query
   * @return a map from field name -> a set of valid values. (We convert number values to strings)
   * @throws WdkModelException
   */
  Map<String, Set<String>> getValuesMap(User user, Map<String, String> contextParamValues,
      Set<String> ontologyTerms, Map<String, OntologyItem> ontology)
      throws WdkModelException {

    List<String> ontologyValuesCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    ontologyValuesCols.add(COLUMN_ONTOLOGY_ID);

    String bgdQuerySql;
    String ontologyQuerySql;
    try {
      QueryInstance<?> instance = _backgroundQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      bgdQuerySql = instance.getSql();
      instance = _ontologyQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      ontologyQuerySql = instance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    String ontologyTermsWhereClause = "";

    if (ontologyTerms != null) {
      // find ontology terms used in our set of member filters
      String ontologyTermsString = ontologyTerms
        .stream()
        .map(term -> term.replaceAll("'", "''"))
        .collect(Collectors.joining("', '"));
      ontologyTermsWhereClause = " where " + FilterParamNew.COLUMN_ONTOLOGY_ID + " IN ('" +
          ontologyTermsString + "')"; 
    }
    
    String bgdQueryNotIsRangeSql = "select bgd.* from (" + bgdQuerySql + ") bgd, (" + ontologyQuerySql + ") onto where bgd." + COLUMN_ONTOLOGY_ID + " = onto." + COLUMN_ONTOLOGY_ID + " and onto." + COLUMN_IS_RANGE + " = 0";
    

    String filterSelectSql = "SELECT distinct " + String.join(", ", ontologyValuesCols) + " FROM (" + bgdQueryNotIsRangeSql + ") bgd" + ontologyTermsWhereClause;

    Map<String, Set<String>> ontologyValues = new HashMap<String, Set<String>>();

    // We want to use the WDK cache for this sql, so have to force the sql into
    // a new SqlQuery object.
    SqlQuery sqlQuery = createValuesMapQuery(_wdkModel, filterSelectSql, ontologyValuesCols, _backgroundQuery.getParams(), _ontologyQuery.getParams());
    try {
      QueryInstance<?> instance = sqlQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      ResultList resultList = instance.getResults();
      while (resultList.next()) {
        String field = (String)resultList.get(FilterParamNew.COLUMN_ONTOLOGY_ID);
        OntologyItem ontologyItem = ontology.get(field);
        if (ontologyItem == null)
          continue; // in the value map sql, but not in the ontology query. skip
        String value = OntologyItemType.getStringValue(resultList, ontologyItem);
        String valueString = value == null ? "NULL" : value;
        if (!ontologyValues.containsKey(field))
          ontologyValues.put(field, new HashSet<String>());
        ontologyValues.get(field).add(valueString);
      }
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
    return ontologyValues;
  }
  
  private SqlQuery createValuesMapQuery(WdkModel wdkModel, String sql, List<String> colNames, Param[] bgdQueryParams, Param[] ontoQueryParams)
      throws WdkModelException {
  QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
  SqlQuery query = new SqlQuery();
  query.setName(getFullName() + "_values_map");
  query.setIsCacheable(true);
  query.setSql(sql);
  for (Param param : bgdQueryParams) query.addParam(param);
  for (Param param : ontoQueryParams) query.addParam(param);
  querySet.addQuery(query);
  for (String colName : colNames) {
      Column column = new Column();
      column.setName(colName);
      column.setQuery(query);
      query.addColumn(column);
  }
  query.resolveReferences(wdkModel);
  return query;
}

   
  /**
   * this is factored out to allow use with an alternative metadata query (eg, the summaryMetadataQuery)
   * @return sql that provides a filtered set of internal IDs
   */
  String getFilteredIdsSql(User user, FilterParamNewStableValue stableValue, Map<String, String> contextParamValues, Query metadataQuery, String idColumn)
      throws WdkModelException {
    return getFilteredIdsSql(user, stableValue, contextParamValues, metadataQuery, idColumn, null);
  }

  /**
   * Return sql that provides a filtered set of IDs.    
   * @param user
   * @param stableValue
   * @param contextParamValues
   * @param metadataQuery
   * @param idColumn  the id column in the metadata query to return
   * @param defaultFilterClause if there are no active filters, use this as a default.  if null, don't use a default filter.
   * @return
   * @throws WdkModelException
   */
  String getFilteredIdsSql(User user, FilterParamNewStableValue stableValue, Map<String, String> contextParamValues, Query metadataQuery, String idColumn, String defaultFilterClause)
       throws WdkModelException {

     try {
       
       // get sql that selects the full set of distinct internals from the metadata query
       String metadataSql;
       QueryInstance<?> instance = metadataQuery.makeInstance(user, contextParamValues, true, 0, new HashMap<String, String>());
       metadataSql = instance.getSql();
       String metadataTableName = "md";
       String filterSelectSql = "SELECT distinct md." + idColumn + " FROM (" + metadataSql + ") md";
       
       // get the applied filters and the ontology
       List<FilterParamNewStableValue.Filter> filters = stableValue.getFilters();
       Map<String, OntologyItem> ontology = getOntology(user, contextParamValues);
      
       // if no filters, return sql for the full set of internals
       String filteredSql;
       if (filters.size() == 0) {
         filteredSql = filterSelectSql;
         if (defaultFilterClause != null) filteredSql += defaultFilterClause;
       } 
       
       // otherwise apply the filters
       else {
        List<String> filterSqls = new ArrayList<String>();
        for (FilterParamNewStableValue.Filter filter : filters)
          filterSqls.add(filter.getFilterAsWhereClause(metadataTableName, ontology, filterSelectSql));

        filteredSql = FormatUtil.join(filterSqls, " INTERSECT ");
       }
       LOG.info("filteredSql:\n" + filteredSql);
       return filteredSql;
     }
     catch (JSONException | WdkUserException ex) {
       throw new WdkModelException(ex);
     }
   }
   

  public JSONObject getJsonValues(User user, Map<String, String> contextParamValues)
      throws WdkModelException {

    contextParamValues = ensureRequiredContext(user, contextParamValues);

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
   * remove invalid filters from stableValue. if stableValue empty, use default.
   */
  /*
   * protected String getValidStableValue(String stableValue) throws WdkModelException { try { if (stableValue
   * == null || stableValue.length() == 0) { JSONObject jsNewStableValue = new JSONObject();
   * jsNewStableValue.put(FilterParamHandler.FILTERS_KEY, new JSONArray()); return
   * JsonUtil.serialize(jsNewStableValue); }
   * 
   * JSONObject jsStableValue = new JSONObject(stableValue);
   * 
   * // need a cached version of uniqueMetadataStringValues query // for each filter in the input stableValue,
   * check that: // key is in the ontology // that value type matches ontology declared value type // if
   * string value, that value is in known string values
   * 
   * return jsStableValue.toString(); } catch (JSONException ex) { throw new WdkModelException(ex); } }
   */

  @Override
  public String getDefault() throws WdkModelException {
    String defaultValue = new JSONObject().put(FilterParamNewHandler.FILTERS_KEY, new JSONArray()).toString();
    return defaultValue;
  }

  /**
   * Default is always no filter... until there is pressure to change this.
   */
  @Override
  public String getDefault(User user, Map<String, String> contextParamValues) {
    String defaultValue = new JSONObject().toString();
    return defaultValue;
  }

  @Override
  public void setContextQuestion(Question question) throws WdkModelException {

    super.setContextQuestion(question);

    // also set context question in the metadata & ontology queries
    _metadataQuery.setContextQuestion(question);
    _ontologyQuery.setContextQuestion(question);
  }

  /**
   * not sure this is used anyplace for real in the UI. Not used by question service.
   */
  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    return rawValue.toString();
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {}

  /**
   * check each filter in stable value. add each invalid one to error message, and throw user exception.
   */
  @Override
  protected void validateValue(User user, String stableValueString, Map<String, String> contextParamValues)
      throws WdkUserException, WdkModelException {

    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, this);

    String err = stableValue.validateSyntaxAndSemantics(user, contextParamValues);

    if (err != null) throw new WdkUserException(err);
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    throw new UnsupportedOperationException(); // this method seems to go nowhere. TODO: remove its whole call
                                               // stack up to stepBean

  }

  private FilterParamNewInstance createFilterParamNewInstance() {
    return new FilterParamNewInstance(this);
  }

  /**
   * this param is stale if either its ontology or background queries consumes any of the stale depended params
   */
  @Override
  public boolean isStale(Set<String> staleDependedParamsFullNames) {
    boolean stale = false;
    for (String fullName : staleDependedParamsFullNames) {
      
      if (_ontologyQuery != null) {
        for (Param param : _ontologyQuery.getParams()) {
          if (param.getFullName().equals(fullName)) {
            stale = true;
            break;
          }
        }
      }

      if (_backgroundQuery != null) {
        for (Param param : _backgroundQuery.getParams()) {
          if (param.getFullName().equals(fullName)) {
            stale = true;
            break;
          }
        }
      }

    }
    return stale;
  }

  protected String getValidStableValue(User user, String stableValueString, Map<String, String> contextParamValues) throws WdkModelException {
    if (stableValueString == null) return getDefault();
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, this);
    String err = stableValue.validateSyntaxAndSemantics(user, contextParamValues);
    return err != null ? getDefault() : stableValueString;
  }

  @Override
  protected DependentParamInstance createDependentParamInstance(User user,
      Map<String, String> dependedParamValues) throws WdkModelException {
    return createFilterParamNewInstance();
  }

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) {
    return getDefault(user, contextParamValues);
  }

  @Override
  public List<Query> getQueries() {
    List<Query> queries = new ArrayList<Query>();
    if (_backgroundQuery != null) {
      queries.add(_backgroundQuery);
    }
    queries.add(_metadataQuery);
    queries.add(_ontologyQuery);
    return queries;
  }

}
