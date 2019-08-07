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
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The filter param is similar to a flatVocabParam in that it provides SQL
 * suitable to embed in an IN clause. The SQL returns a list of internal values,
 * similar to flatVocabParam.
 *
 * It is configured by two queries: ontologyQuery and metadataQuery. The former
 * returns a tree of categories and information describing them. The latter maps
 * internal values to entries in the ontology (such as age) and provides a value
 * for the ontology entry (such as 18).
 *
 * Both these queries can be dependent.
 *
 * The input from the user is in the form of a set of filters. each filter
 * specifies an ontology entry and one or more values for it. The param returns
 * all internal values from the metadata query that match all supplied filters
 * (i.e. the filtering is done on the backend).
 *
 * the filter param also provides summary information to the client about the
 * distribution of values for a requested ontology entry.
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
  private List<String> _metadataValueColumns = new ArrayList<>();  // the value columns actually found in the metadata query

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
    _metadataValueColumns = new ArrayList<>(param._metadataValueColumns);
  }

  @Override
  public Set<String> getContainedQueryFullNames() {
    Set<String> names = new HashSet<>();
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

    _metadataValueColumns = new ArrayList<>();
    _metadataValueColumns.add(COLUMN_INTERNAL);

    List<String> metadataCols = new ArrayList<>(OntologyItemType.getTypedValueColumnNames());
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
      _metadataValueColumns.add(COLUMN_FILTER_ITEM_ID);
    }
    if (columns.containsKey(COLUMN_RECORD_ID)) { // if model includes this optional column, use it
      metadataCols.add(COLUMN_RECORD_ID);
      _recordIdColumn = COLUMN_RECORD_ID;
      _metadataValueColumns.add(COLUMN_RECORD_ID);
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
   * We cache this because typically ontologies are sensitive only to the
   * grossest dependent param (e.g. dataset), so will be reused across users.
   *
   * @return <propertyName, <infoKey, infoValue>>
   */
  public Map<String, OntologyItem> getOntology(User user, Map<String,String> paramValues)
      throws WdkModelException {
    RunnableObj<QueryInstanceSpec> ontologySpec = QueryInstanceSpec.builder()
        .putAll(paramValues)
        .buildRunnable(user, _ontologyQuery, StepContainer.emptyContainer());
    OntologyItemNewFetcher fetcher = new OntologyItemNewFetcher(ontologySpec);
    try {
      return CacheMgr.get().getOntologyNewCache().getValue(fetcher.getCacheKey(), fetcher);
    }
    catch (ValueProductionException ex) {
      return WdkModelException.unwrap(ex);
    }
  }

  /**
   * @param appliedFilters
   *          (in stable value format)
   * @return { totalCount: number; filteredCount: number; }
   */
  public FilterParamSummaryCounts getTotalsSummary(SemanticallyValid<QueryInstanceSpec> validSpec,
      JSONObject appliedFilters) throws WdkModelException {

    ////////////////////////////////////////
    /* GET UNFILTERED (BACKGROUND) COUNTS */
    ////////////////////////////////////////

    QueryInstanceSpec spec = validSpec.get();
    String bgdSql = getInternalQuerySql(spec.getUser(), spec.toMap(), _backgroundQuery);

    // Set up counts store
    FilterParamSummaryCounts fpsc = new FilterParamSummaryCounts();

    // get unfiltered count of filter items
    String sql = "SELECT count(distinct md." + _filterItemIdColumn + ") FROM (" + bgdSql + ") md";
    fpsc.unfilteredFilterItemCount = runCountSql(sql, "filterItemIdsCount-unfiltered");

    // get unfiltered count of records, unless the record id column is the same as the filter item id column
    if (_recordIdColumn.equals(_filterItemIdColumn)) {
      fpsc.unfilteredRecordCount = fpsc.unfilteredFilterItemCount;
    }
    else {
      sql = "SELECT count (distinct md." + _recordIdColumn + ") FROM (" + bgdSql + ") md";
      fpsc.unfilteredRecordCount = runCountSql(sql, "recordIds-unfiltered");
    }

    /////////////////////////
    /* GET FILTERED COUNTS */
    /////////////////////////

    // sql to find the filtered count
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);

    // get filtered filter_item_ids  count
    String filteredItemIdsSql = getFilteredFilterItemIdsSql(validSpec, stableValue, _metadataQuery, _filterItemIdColumn, null);
    sql = "select count( distinct " + _filterItemIdColumn + ") as CNT from (" + filteredItemIdsSql + ") c";
    fpsc.filteredFilterItemCount = runCountSql(sql, "filterItemIds-filtered");

    if (_filterItemIdColumn.equals(_recordIdColumn)) {
      fpsc.filteredRecordCount = fpsc.filteredFilterItemCount;
    }
    else {
      String filteredMetadataSql = getFilteredMetadataSql(validSpec, stableValue, _metadataQuery, null);
      sql = "select count( distinct " + _recordIdColumn + ") as CNT from (" + filteredMetadataSql + ")";
      fpsc.filteredRecordCount = runCountSql(sql, "recordIds-filtered");
    }
    return fpsc;
  }

  private static String getInternalQuerySql(User user, Map<String,String> paramValues, Query query) throws WdkModelException {
    RunnableObj<QueryInstanceSpec> backgroundSpec = QueryInstanceSpec.builder()
        .putAll(paramValues)
        .buildRunnable(user, query, StepContainer.emptyContainer());
    return Query.makeQueryInstance(backgroundSpec).getSql();
  }

  private long runCountSql(String sql, String qName) throws WdkModelException {
    SqlCountCache cache = CacheMgr.get().getSqlCountCache(_wdkModel.getAppDb());
    try {
      return cache.getItem(sql, "filter-param-counts-" + qName);
    } catch (ValueProductionException e) { throw new WdkModelException(e); }
  }

  ////////////////////////////////////////////
  ////////////////////////////////////////////
  /* Ontology Term Summary */
  ////////////////////////////////////////////
  ////////////////////////////////////////////
  /**
   * @param validSpec valid spec for this param's parent query
   * @param ontologyItem item for which summary should be returned
   * @param appliedFilters any filters to apply
   * @param ontologyItemClass class of the values
   * @param <T> The type of the values
   * @return a map from a value for this ontology term to the counts for that value.
   * @throws WdkModelException
   * TODO: MULTI-FILTER upgrade:  take a list of ontology terms, and return a map of maps, one per term.
   */
  public <T> OntologyTermSummary<T> getOntologyTermSummary(SemanticallyValid<QueryInstanceSpec> validSpec,
      OntologyItem ontologyItem, JSONObject appliedFilters, Class<T> ontologyItemClass)
          throws WdkModelException {

    ////////////////////////////////////////////
    /* GET UNFILTERED MAP AND POPULATE COUNTS */
    ////////////////////////////////////////////

    QueryInstanceSpec spec = validSpec.get();
    String bgdSql = getInternalQuerySql(spec.getUser(), spec.toMap(), _backgroundQuery);

    // limit it to our ontology_id
    List<String> metadataCols = new ArrayList<>(OntologyItemType.getTypedValueColumnNames());
    metadataCols.add(_filterItemIdColumn);
    metadataCols.add(COLUMN_ONTOLOGY_ID);

    String cols = metadataCols.stream().collect(Collectors.joining(", "));
    String unfilteredSqlPerOntologyId =
        "/* START unfilteredSqlPerOntologyId */ " +
        "SELECT distinct " + cols +
        " FROM ( /* START bgdSql */ " + bgdSql + " /* END bgdSql */) mq" +
        " WHERE mq." + COLUMN_ONTOLOGY_ID + " = ? " +
        "/* END unfilteredSqlPerOntologyId */ ";

    // read into a map of filter_item_id -> value(s)
    Map<T, Long> unfiltered = countMetaDataForOntologyTerm(validSpec, ontologyItem,
        unfilteredSqlPerOntologyId, ontologyItemClass);

    // get histogram of those, stored in JSON
    Map<T, FilterParamSummaryCounts> summaryCountsMap = new HashMap<>();
    populateSummaryCounts(unfiltered, summaryCountsMap, false); // stuff in to 0th position in array

    //////////////////////////////////////////
    /* GET FILTERED MAP AND POPULATE COUNTS */
    //////////////////////////////////////////

    // get sql for the set internal ids that are pruned by the filters
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);
    String filteredFilterItemIdSql = getFilteredFilterItemIdsSql(validSpec, stableValue, getMetadataQuery(), _filterItemIdColumn,
        " where " + COLUMN_ONTOLOGY_ID + " = '" + ontologyItem.getOntologyId().replaceAll("'", "''") + "'");

    // use that set of ids to limit our ontology id's metadata
    String andClause = " /* START andClause */ AND " + _filterItemIdColumn + " in ( select " + _filterItemIdColumn + " from (" + filteredFilterItemIdSql + ") a)  /* END andClause */ ";
    String filteredSqlPerOntologyId = unfilteredSqlPerOntologyId + andClause;

    // read this filtered set into map of internal -> value(s)
    Map<T, Long> filtered = countMetaDataForOntologyTerm(validSpec, ontologyItem,
        filteredSqlPerOntologyId, ontologyItemClass);

    // add the filtered set into the histogram
    populateSummaryCounts(filtered, summaryCountsMap, true); // stuff in to 1st position in array

    OntologyTermSummary <T> summary = new OntologyTermSummary<T>(summaryCountsMap);

    /////////////////////////////////////
    /* GET DISTINCT FILTER ITEM COUNTS */
    /////////////////////////////////////
    String oneColumnBgdSql = "select " + _filterItemIdColumn + " from (" + bgdSql + ") bq WHERE bq." + COLUMN_ONTOLOGY_ID +  " = ?";
    String unfilteredDistinctFilterItemsSql = "SELECT count (distinct " + _filterItemIdColumn + ") as cnt FROM (" + oneColumnBgdSql + ") fd";

    String filteredDistinctFilterItemsSql = "SELECT count (distinct " + _filterItemIdColumn + ") as cnt FROM (" + oneColumnBgdSql +
        " INTERSECT select " + _filterItemIdColumn + " from (" + filteredFilterItemIdSql + ") fi) fs";

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
      LOG.info(filteredDistinctFilterItemsSql);
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
   * stuff counts per ontology term value into json structure. first pair
   * position is unfiltered, second is filtered
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

  /**
   * for a specified ontology term, return a map of value -> count of that value
   * @param spec
   * @param ontologyItem
   * @param metaDataSql
   * @param ontologyItemClass
   * @return
   * @throws WdkModelException
   */
  private <T> Map<T, Long> countMetaDataForOntologyTerm(SemanticallyValid<QueryInstanceSpec> spec,
      OntologyItem ontologyItem, String metaDataSql, Class<T> ontologyItemClass)
          throws WdkModelException {

    String valueColumnName = ontologyItem.getType().getMetadataQueryColumn();

    String sql = "/* START countMetaDataForOntologyTerm */ SELECT " + valueColumnName +
        ", count(*) as CNT FROM (" + metaDataSql + ") mq WHERE mq."
    + COLUMN_ONTOLOGY_ID + " = ? GROUP BY " + valueColumnName + "/* END countMetaDataForOntologyTerm */ ";

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

  public Map<String, Set<String>> getValuesMap(User user, Map<String,String> paramValues) throws WdkModelException {
    return getValuesMap(user, paramValues, null, getOntology(user, paramValues));
  }

  /**
   * return map of ontology field name to values (as strings), for the provided list of ontology terms.
   * only includes values for terms that are not isRange.
   *
   * @param paramValues spec for this param's parent query
   * @param ontologyTerms
   *          a list of ontology term to include in result.  NULL to use all terms found in bgd query
   * @return a map from field name -> a set of valid values. (We convert number values to strings)
   */
  Map<String, Set<String>> getValuesMap(User user, Map<String,String> paramValues,
      Set<String> ontologyTerms, Map<String, OntologyItem> ontology)
          throws WdkModelException {

    // the names of value columns in the background query.
    List<String> ontologyValuesCols = new ArrayList<>(OntologyItemType.getTypedValueColumnNames());
    ontologyValuesCols.add(COLUMN_ONTOLOGY_ID);

    // get query instances for both ontology and background query
    // and extract from them their sql.  we'll compose those sqls into our own query
    String bgdQuerySql = getInternalQuerySql(user, paramValues, _backgroundQuery);
    String ontologyQuerySql = getInternalQuerySql(user, paramValues, _ontologyQuery);

    String ontologyTermsWhereClause = "";

    // if ontology terms supplied, construct a where clause from them
    // (otherwise, we do not limit by ontology term, instead using all those in the bgd query)
    if (ontologyTerms != null) {
      String ontologyTermsString = ontologyTerms
        .stream()
        .map(term -> term.replaceAll("'", "''"))
        .collect(Collectors.joining("', '"));

      ontologyTermsWhereClause = " where " + FilterParamNew.COLUMN_ONTOLOGY_ID + " IN ('" +
          ontologyTermsString + "')";
    }

    // sql to find all rows in bgd query where ontology_id points to ontology term that is not isRange
    String bgdQueryNotIsRangeSql = "select bgd.* from (" + bgdQuerySql + ") bgd, (" + ontologyQuerySql + ") onto where bgd." + COLUMN_ONTOLOGY_ID + " = onto." + COLUMN_ONTOLOGY_ID + " and onto." + COLUMN_IS_RANGE + " = 0";

    // sql to find the distinct values (from value columns) in the bgd query for non isRange terms
    String filterSelectSql = "SELECT distinct " + String.join(", ", ontologyValuesCols) + " FROM (" + bgdQueryNotIsRangeSql + ") bgd" + ontologyTermsWhereClause;

    // the map that we will return
    Map<String, Set<String>> ontologyValues = new HashMap<>();

    // Now that we've constructed the sql, we want to use the WDK cache for it,
    //   so have to force the sql into a new SqlQuery object.
    SqlQuery sqlQuery = createValuesMapQuery(_wdkModel, filterSelectSql, ontologyValuesCols, _backgroundQuery.getParams(), _ontologyQuery.getParams());

    // run the sqlQuery to get all the values, and stuff into the map
    RunnableObj<QueryInstanceSpec> valuesMapSpec = QueryInstanceSpec.builder()
        .putAll(paramValues)
        .buildRunnable(user, sqlQuery, StepContainer.emptyContainer());
    try (ResultList resultList = Query.makeQueryInstance(valuesMapSpec).getResults()) {
      while (resultList.next()) {
        String field = (String)resultList.get(FilterParamNew.COLUMN_ONTOLOGY_ID);
        OntologyItem ontologyItem = ontology.get(field);
        if (ontologyItem == null)
          continue; // in the value map sql, but not in the ontology query. skip
        String value = OntologyItemType.getStringValue(resultList, ontologyItem);
        String valueString = value == null ? "NULL" : value;
        if (!ontologyValues.containsKey(field))
          ontologyValues.put(field, new HashSet<>());
        ontologyValues.get(field).add(valueString);
      }
    }
    return ontologyValues;
  }

  // given sql to provide data for the values map, construct a wdk-cacheable sqlQuery from it
  // because the sql embeds the ontology and bgd queries, which might have parameters, this query
  // must inherit their parameters
  private SqlQuery createValuesMapQuery(WdkModel wdkModel, String sql, List<String> colNames, Param[] bgdQueryParams, Param[] ontoQueryParams)
      throws WdkModelException {
    QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
    SqlQuery query = new SqlQuery();
    query.setName(getFullName() + "_values_map");
    query.setIsCacheable(true);
    query.setSql(sql);
    Set <String> paramsSeen = new HashSet<>();
    for (Param param : bgdQueryParams) {
      query.addParamRef(new ParamReference(param.getFullName()));
      paramsSeen.add(param.getFullName());
    }
    for (Param param : ontoQueryParams) {
      if (!paramsSeen.contains(param.getFullName())) {
        query.addParamRef(new ParamReference(param.getFullName()));
      }
    }
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
   * Return sql that provides a filtered set of IDs, using the id column provided.
   *
   * @param validSpec valid parameter set for this param's parent query
   * @param stableValue
   * @param metadataQuery
   * @param idColumn  the id column in the metadata query to return
   * @param defaultFilterClause if there are no active filters, use this as a default.  if null, don't use a default filter.
   * @return sql
   * @throws WdkModelException
   */
  String getFilteredFilterItemIdsSql(SemanticallyValid<QueryInstanceSpec> validSpec,
      FilterParamNewStableValue stableValue, Query metadataQuery,
      String idColumn, String defaultFilterClause)
          throws WdkModelException {
    // get sql that selects the full set of distinct internals from the metadata query
    QueryInstanceSpec spec = validSpec.get();
    String metadataSql = getInternalQuerySql(spec.getUser(), spec.toMap(), metadataQuery);

    String metadataTableName = "md";
    String filterSelectSql =
        "/* START filterSelectSql */ " +
        "SELECT distinct " + metadataTableName + "." + idColumn +
        " FROM ( /* START metadataSql */ " + metadataSql + " /* END metadataSql */ )" + metadataTableName +
        " /* END filterSelectSql */";
    return getFilteredIdsSql(validSpec, stableValue, metadataTableName, filterSelectSql, defaultFilterClause);
  }

  /**
   * Apply provided filters to metadata sql, returning all three value columns
   */
  String getFilteredMetadataSql(SemanticallyValid<QueryInstanceSpec> validSpec,
      FilterParamNewStableValue stableValue, Query metadataQuery, String defaultFilterClause)
          throws WdkModelException {
    // get sql that selects the full set of distinct internals from the metadata query
    QueryInstanceSpec spec = validSpec.get();
    String metadataSql = getInternalQuerySql(spec.getUser(), spec.toMap(), metadataQuery);

    String metadataTableName = "md";
    String selectCols = String.join(", " + metadataTableName + ".", _metadataValueColumns);
    String filterSelectSql = "SELECT md." + selectCols + " FROM (" + metadataSql + ") " + metadataTableName;
    return getFilteredIdsSql(validSpec, stableValue, metadataTableName, filterSelectSql, defaultFilterClause);
  }

  private String getFilteredIdsSql(SemanticallyValid<QueryInstanceSpec> validSpec,
      FilterParamNewStableValue stableValue, String metadataTableAbbrev,
      String filterSelectSql, String defaultFilterClause)
          throws WdkModelException {

    // get the applied filters and the ontology
    List<FilterParamNewStableValue.Filter> filters = stableValue.getFilters();
    QueryInstanceSpec spec = validSpec.get();
    Map<String, OntologyItem> ontology = getOntology(spec.getUser(), spec.toMap());

    // if no filters, return sql for the full set of internals
    String filteredSql;
    if (filters.size() == 0) {
      filteredSql = filterSelectSql;
      if (defaultFilterClause != null) {
        filteredSql += defaultFilterClause;
      }
      LOG.debug("filteredSql:\n" + filteredSql); 
      return " /* START filteredIdsSql */ " + filteredSql + " /* END filteredIdsSql */ ";
    }

    // otherwise apply the filters
    else {
      List<String> filterSqls = new ArrayList<String>();
      for (FilterParamNewStableValue.Filter filter : filters) {
        filterSqls.add(filter.getFilterAsWhereClause(metadataTableAbbrev, ontology, filterSelectSql));
      }
      filteredSql = FormatUtil.join(filterSqls, " INTERSECT ");
    }
    LOG.debug("filteredSql:\n" + filteredSql);
    return filteredSql;
  }

  public JSONObject getJsonValues(SemanticallyValid<QueryInstanceSpec> validSpec)
      throws WdkModelException {
    JSONObject jsParam = new JSONObject();
    try {
      // create json for the ontology
      JSONObject jsOntology = new JSONObject();
      QueryInstanceSpec spec = validSpec.get();
      Map<String, OntologyItem> ontologyMap = getOntology(spec.getUser(), spec.toMap());
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
      return jsParam;
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
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

  /**
   * Default is always no filter... until there is pressure to change this.
   */
  @Override
  protected String getDefault(PartiallyValidatedStableValues stableValues) {
    return new JSONObject().put(FilterParamNewHandler.FILTERS_KEY, new JSONArray()).toString();
  }

  @Override
  public void setContextQuestion(Question question) throws WdkModelException {

    super.setContextQuestion(question);

    // also set context question and param in the metadata & ontology queries
    _metadataQuery.setContextQuestion(question);
    _metadataQuery.setContextParam(this);
    _ontologyQuery.setContextQuestion(question);
    _ontologyQuery.setContextParam(this);
    _backgroundQuery.setContextQuestion(question);
    _backgroundQuery.setContextParam(this);
  }

  /**
   * not sure this is used anyplace for real in the UI. Not used by question service.
   */
  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) {
    return rawValue.toString();
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {}

  /**
   * check each filter in stable value. add each invalid one to error message, and throw user exception.
   */
  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues contextParamValues, ValidationLevel level)
      throws WdkModelException {
    final String name = getName();
    final FilterParamNewStableValue stableValue =
        new FilterParamNewStableValue(contextParamValues.get(getName()), this);
    final String err = level.isGreaterThanOrEqualTo(ValidationLevel.SYNTACTIC)
      ? stableValue.validateSyntaxAndSemantics(contextParamValues)
      : stableValue.validateSyntax();
    return err == null ?
      contextParamValues.setValid(name) :
      contextParamValues.setInvalid(name, err);
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    throw new UnsupportedOperationException(); // this method seems to go nowhere. TODO: remove its whole call
                                               // stack up to stepBean

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

  @Override
  public String getSanityDefault(User user, Map<String, String> contextParamValues,
      SelectMode sanitySelectMode) {
    return getDefault(null);
  }

  @Override
  public List<Query> getQueries() {
    List<Query> queries = new ArrayList<>();
    if (_backgroundQuery != null) {
      queries.add(_backgroundQuery);
    }
    queries.add(_metadataQuery);
    queries.add(_ontologyQuery);
    return queries;
  }

}
