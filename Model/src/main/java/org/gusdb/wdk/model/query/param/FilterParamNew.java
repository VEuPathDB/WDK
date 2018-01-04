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
import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.values.StableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.ValidStableValues;
import org.gusdb.wdk.model.query.param.values.WriteableStableValues;
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

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(FilterParamNew.class);

  public static class OntologyCache extends ItemCache<String, Map<String, OntologyItem>> {}

  public static class MetadataNewCache extends ItemCache<String, Map<String, MetaDataItem>> {}

  public static class FilterParamNewCache extends ItemCache<String, FilterParamNewInstance> {}

  public static class FilterParamSummaryCounts {
    public long unfilteredCount = 0;
    public long filteredCount = 0;
    public long untransformedUnfilteredCount = 0;
    public long untransformedFilteredCount = 0;
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
  static final String COLUMN_GLOBAL_INTERNAL = "internal_for_counts"; // the type of ID as returned by the search, rather than the filter
  
  private static final String FILTERED_IDS_MACRO = "##FILTERED_IDS##";

  private static final int FETCH_SIZE = 1000;

  private String _metadataQueryRef;
  private Query _metadataQuery;

  private String _ontologyQueryRef;
  private Query _ontologyQuery;

  private String _backgroundQueryRef;
  private Query _backgroundQuery;
  
  private String _ontologyValuesQueryRef;
  private Query _ontologyValuesQuery;

  private String _idTransformQueryRef;
  private Query _idTransformQuery;

  private String _filterDataTypeDisplayName;

  private boolean _useIdTransformSqlForInternalValue = false;
  
  // the output count of this param can be used to predict the final search results count, if all downstream
  // params (in dependency tree) have default values
  private boolean countPredictsAnswerCount = false;

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
    
    _ontologyValuesQueryRef = param._ontologyValuesQueryRef;
    if (param._ontologyValuesQuery != null)
      _ontologyValuesQuery = param._ontologyValuesQuery.clone();

    _idTransformQueryRef = param._idTransformQueryRef;
    if (param._idTransformQuery != null)
      _idTransformQuery = param._idTransformQuery.clone();

    _trimMetadataTerms = param._trimMetadataTerms;
    _useIdTransformSqlForInternalValue = param._useIdTransformSqlForInternalValue;
    _filterDataTypeDisplayName = param._filterDataTypeDisplayName;
  }

  @Override
  public Set<String> getContainedQueryFullNames() {
    Set<String> names = new HashSet<String>();
    names.add(_metadataQueryRef);
    names.add(_ontologyQueryRef);
    names.add(_backgroundQueryRef);
    names.add(_ontologyValuesQueryRef);
    names.add(_idTransformQueryRef);

    return names;
  }
  public void setIdTransformSql(Object o) {}

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
  
  public String getOntologyValuesQueryRef() {
    return _ontologyValuesQueryRef;
  }

  public void setOntologyValuesQueryRef(String ontologyValuesQueryRef) {
    _ontologyValuesQueryRef = ontologyValuesQueryRef;
  }

  public String getIdTransformQueryRef() {
    return _idTransformQueryRef;
  }

  public void setIdTransformQueryRef(String idTransformQueryRef) {
    _idTransformQueryRef = idTransformQueryRef;
  }

  public Query getValuesMapQuery() {
    return _ontologyValuesQuery;
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

  public void setUseIdTransformSqlForInternalValue(boolean useIt) {
    _useIdTransformSqlForInternalValue = useIt;
  }

  public boolean getUseIdTransformSqlForInternalValue() {
    return _useIdTransformSqlForInternalValue;
  }
  
  public boolean getCountPredictsAnswerCount() {
    return countPredictsAnswerCount;
  }
  
  public void setCountPredictsAnswerCount(boolean canUse) {
    countPredictsAnswerCount = canUse;
  }

  /**
   * @param trimMetadataTerms
   *          the metadataQuery to set
   */
  public void setTrimMetadataTerms(boolean trimMetadataTerms) {
    _trimMetadataTerms = trimMetadataTerms;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // resolve ontology query
    if (_ontologyQueryRef != null) {

      // validate dependent params
      _ontologyQuery = resolveDependentQuery(model, _ontologyQueryRef, "ontology query");

      // validate columns
      Map<String, Column> columns = _ontologyQuery.getColumnMap();
      String[] cols = { COLUMN_ONTOLOGY_ID, COLUMN_PARENT_ONTOLOGY_ID, COLUMN_DISPLAY_NAME,
          COLUMN_DESCRIPTION, COLUMN_TYPE, COLUMN_UNITS, COLUMN_PRECISION };
      for (String col : cols)
        if (!columns.containsKey(col))
          throw new WdkModelException("The ontologyQuery " + _ontologyQueryRef + " in filterParam " +
              getFullName() + " must include column: " + col);
    }

    List<String> metadataCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    metadataCols.add(COLUMN_INTERNAL);
    metadataCols.add(COLUMN_GLOBAL_INTERNAL);

    // resolve background query
    if (_backgroundQueryRef != null) {

      // validate dependent params
      _backgroundQuery = resolveDependentQuery(model, _backgroundQueryRef, "background query");

      // validate columns
      Map<String, Column> columns = _backgroundQuery.getColumnMap();
      for (String col : metadataCols)
        if (!columns.containsKey(col))
          throw new WdkModelException("The backgroundQuery " + _backgroundQueryRef + " in filterParam " +
              getFullName() + " must include column: " + col);
    }

    List<String> ontologyValuesCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    ontologyValuesCols.add(COLUMN_ONTOLOGY_ID);

    // resolve values map query
    if (_ontologyValuesQueryRef != null) {

      // validate dependent params
      _ontologyValuesQuery = resolveDependentQuery(model, _ontologyValuesQueryRef, "ontologyValues query");

      // validate columns
      Map<String, Column> columns = _ontologyValuesQuery.getColumnMap();
      for (String col : ontologyValuesCols)
        if (!columns.containsKey(col))
          throw new WdkModelException("The ontologyValuesQuery " + _ontologyValuesQueryRef + " in filterParam " +
              getFullName() + " must include column: " + col);
    }

    // resolve metadata query
    if (_metadataQueryRef != null) {

      // validate dependent params
      _metadataQuery = resolveDependentQuery(model, _metadataQueryRef, "metadata query");

      // validate columns.
      Map<String, Column> columns = _metadataQuery.getColumnMap();
      for (String col : metadataCols)
        if (!columns.containsKey(col))
          throw new WdkModelException("The metadata query " + _metadataQueryRef + " in filterParam " +
              getFullName() + " must include column: " + col);
    }
    
    // resolve id transform query
    if (_idTransformQueryRef != null) {

      // validate dependent params
      _idTransformQuery = resolveDependentQuery(model, _idTransformQueryRef, "idTransform query");
      _idTransformQuery.setIsCacheable(false); // don't cache because we run this sql ourselves, manually, in this file.

      // validate columns
      Map<String, Column> columns = _idTransformQuery.getColumnMap();
      if (columns.size() != 1 || !columns.containsKey(COLUMN_INTERNAL))
          throw new WdkModelException("The idTransformQuery " + _idTransformQueryRef + " in filterParam " +
              getFullName() + " must include have only one column, 'internal'");
    }
  }

  /**
   * We cache this because typically ontologies are sensitive only to the grossest dependent param (eg,
   * dataset), so will be reused across users.
   * 
   * @return <propertyName, <infoKey, infoValue>> 
   * @throws WdkModelException
   */
  public Map<String, OntologyItem> getOntology(User user, StableValues contextParamValues)
      throws WdkModelException {
    try {
      // apply incoming param values to the ontology query
      CompleteValidStableValues ontologyStableValues = ValidStableValuesFactory.createFromSupersetValues(user,
          new WriteableStableValues(_ontologyQuery, contextParamValues));

      OntologyItemNewFetcher fetcher = new OntologyItemNewFetcher(ontologyStableValues, user);
      return CacheMgr.get().getOntologyNewCache().getItem(fetcher.getCacheKey(), fetcher);
    }
    catch (UnfetchableItemException ex) {
      decodeException(ex);
      return null;
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Incoming values did not contain all values needed by ontology query", e);
    }
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
  //TODO - CWL Verify 
  public FilterParamSummaryCounts getTotalsSummary(User user, StableValues contextParamValues,
      JSONObject appliedFilters) throws WdkModelException {

    /* GET UNFILTERED (BACKGROUND) COUNTS */
    // use background query if provided, else use metadata query

    // get base background query
    Query bgdQuery = _backgroundQuery == null ? _metadataQuery : _backgroundQuery;
    CompleteValidStableValues bgdStableValues = ValidStableValuesFactory.createFromSupersetValues(user, contextParamValues);
    String bgdSql;
    try {
      QueryInstance<?> queryInstance = bgdQuery.makeInstance(user, bgdStableValues);
      bgdSql = queryInstance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    // Set up counts store
    FilterParamSummaryCounts fpsc = new FilterParamSummaryCounts();

    // get untransformed unfiltered count
    String sql = "SELECT count(distinct md." + COLUMN_INTERNAL + ") FROM (" + bgdSql + ") md";
    fpsc.untransformedUnfilteredCount = runCountSql(sql);

    // get transformed unfiltered count
    sql = "SELECT count (distinct md." + COLUMN_GLOBAL_INTERNAL + ") FROM (" + bgdSql + ") md";
    fpsc.unfilteredCount = runCountSql(sql);

    /* GET FILTERED COUNTS */
    // sql to find the filtered count
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);
    String filteredInternalsSql = getFilteredValue(user, stableValue, contextParamValues, _metadataQuery);

    // get untransformed filtered count
    sql = "select distinct count(" + COLUMN_INTERNAL + ") as CNT from (" + filteredInternalsSql + ")";
    fpsc.untransformedFilteredCount = runCountSql(sql);

    sql = "select distinct count(" + COLUMN_GLOBAL_INTERNAL + ") as CNT from (" + filteredInternalsSql + ")";
    fpsc.filteredCount = runCountSql(sql);

    return fpsc;
  }

  String transformIdSql(String idSql, User user, Map<String, String> contextParamValues) throws WdkModelException {
    if (_idTransformQuery == null)
      return idSql;
    
    String idTransformSql;
    try {
      QueryInstance<?> instance = _idTransformQuery.makeInstance(user, contextParamValues, true, 0,
          new HashMap<String, String>());
      idTransformSql = instance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    return idTransformSql.replace(FILTERED_IDS_MACRO, idSql);
  }

  private long runCountSql(String sql) {
    return new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "filter-param-counts").executeQuery(
        new SingleLongResultSetHandler()).getRetrievedValue();
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
      CompleteValidStableValues contextParamValues, OntologyItem ontologyItem, JSONObject appliedFilters,
      Class<T> ontologyItemClass) throws WdkModelException {

    //contextParamValues = ensureRequiredContext(user, contextParamValues);

    FilterParamNewInstance paramInstance = createFilterParamNewInstance();

    /* GET UNFILTERED COUNTS */
    // use background query if provided, else use metadata query

    // get base bgd query
    Query bgdQuery = _backgroundQuery == null ? _metadataQuery : _backgroundQuery;
    String bgdSql;
    try {
      CompleteValidStableValues bgdValues = ValidStableValuesFactory.createFromSupersetValues(
          user, new WriteableStableValues(bgdQuery, contextParamValues));
      QueryInstance<?> queryInstance = bgdQuery.makeInstance(user, bgdValues);
      bgdSql = queryInstance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }

    // limit it to our ontology_id
    List<String> metadataCols = new ArrayList<String>(OntologyItemType.getTypedValueColumnNames());
    metadataCols.add(COLUMN_INTERNAL);
    metadataCols.add(COLUMN_ONTOLOGY_ID);
    String cols = metadataCols.stream().collect(Collectors.joining(", "));
    String metadataSqlPerOntologyId = "SELECT distinct " + cols + " FROM (" + bgdSql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID +
        " = ?";
    
    // while we are here, format sql to find distinct internals in unfiltered
    String internalsSqlPerOntologyId = "SELECT count (distinct " + COLUMN_INTERNAL + ") as cnt FROM (" + bgdSql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID +
        " = ?";

    // read into a map of internal -> value(s)
    Map<String, List<T>> unfiltered = getMetaData(user, contextParamValues, ontologyItem, paramInstance,
        metadataSqlPerOntologyId, ontologyItemClass);

    // get histogram of those, stored in JSON
    Map<T, FilterParamSummaryCounts> summaryCountsMap = new HashMap<>();
    populateSummaryCounts(unfiltered, summaryCountsMap, false); // stuff in to 0th position in array

    /* GET FILTERED COUNTS */
    // get sql for the set internal ids that are pruned by the filters
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(appliedFilters, this);
    String internalSql = getFilteredValue(user, stableValue, contextParamValues, getMetadataQuery());


    // use that set of ids to limit our ontology id's metadata
    String andClause = " AND " + COLUMN_INTERNAL + " in ( select internal from (" + internalSql + "))";
    String metadataSqlPerOntologyIdFiltered = metadataSqlPerOntologyId + andClause;

    // while we are here, format sql to find distinct internals in filtered
    String internalsSqlPerOntologyIdFiltered = internalsSqlPerOntologyId + andClause;

    // read this filtered set into map of internal -> value(s)
    Map<String, List<T>> filtered = getMetaData(user, contextParamValues, ontologyItem, paramInstance,
        metadataSqlPerOntologyIdFiltered, ontologyItemClass);

    // add the filtered set into the histogram
    populateSummaryCounts(filtered, summaryCountsMap, true); // stuff in to 1st position in array

    OntologyTermSummary <T> summary = new OntologyTermSummary<T>(summaryCountsMap);
    
    getCountsOfInternals(summary, internalsSqlPerOntologyId, internalsSqlPerOntologyIdFiltered, ontologyItem.getOntologyId());
     
    return summary;
  }
  
  /**
   * update the provided OntologyTermSummary with counts of internals.
   * @param summary
   * @param internalsSqlPerOntologyId
   * @param internalsSqlPerOntologyIdFiltered
   * @param ontologyId
   * @throws WdkModelException
   */
  private <T> void getCountsOfInternals(OntologyTermSummary <T> summary, String internalsSqlPerOntologyId, 
      String internalsSqlPerOntologyIdFiltered, String ontologyId) throws WdkModelException {
 
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    PreparedStatement ps = null;
    ResultSet resultSet = null;

    try {
      ps = SqlUtils.getPreparedStatement(dataSource, internalsSqlPerOntologyId);
      ps.setString(1, ontologyId);
      resultSet = ps.executeQuery();
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
      ps = SqlUtils.getPreparedStatement(dataSource, internalsSqlPerOntologyIdFiltered);
      ps.setString(1, ontologyId);
      resultSet = ps.executeQuery();
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
   * @param metadataForOntologyId
   * @param metadataForOntologyId
   * @param summary
   */
  private <T> void populateSummaryCounts(Map<String, List<T>> metadataForOntologyId,
      Map<T, FilterParamSummaryCounts> summary, boolean filtered) {
    for (List<T> values : metadataForOntologyId.values()) {
      for (T value : values) {
        FilterParamSummaryCounts counts;
        if (summary.containsKey(value)) {
          counts = summary.get(value);
        }
        else {
          counts = new FilterParamSummaryCounts();
          summary.put(value, counts);
        }
        if (filtered)
          counts.filteredCount++;
        else
          counts.unfilteredCount++;
      }
    }
  }

  private void decodeException(UnfetchableItemException ex) throws WdkModelException {
    Throwable nestedException = ex.getCause();
    if (nestedException == null)
      throw new WdkModelException(ex.getMessage());
    if (nestedException instanceof WdkModelException)
      throw (WdkModelException) nestedException;
    throw new WdkModelException(nestedException);
  }

  /**
   * get an in-memory copy of meta data for a specified ontology_id
   * 
   * @param user
   * @param contextParamValues
   * @param cache
   *          the cache is needed, to make sure the contextParamValues are initialized correctly. (it is
   *          initialized when a cache is created.)
   * @param metaDataSql
   *          - sql that provides the meta data. has a single bind variable for ontology id
   * @return map from internal_id to that id's values.
   * @throws WdkModelException
   * TODO: MULTI-FILTER upgrade:  take a list of ontology terms, and return a map of maps, one per term.
   */
  //TODO - CWL Verify 
  private <T> Map<String, List<T>> getMetaData(User user, CompleteValidStableValues contextParamValues,
      OntologyItem ontologyItem, FilterParamNewInstance cache, String metaDataSql, Class<T> ontologyItemClass)
      throws WdkModelException {

    String sql = "SELECT mq.* FROM (" + metaDataSql + ") mq WHERE mq." + COLUMN_ONTOLOGY_ID + " = ?";

    // run the composed sql, and get the metadata back
    Map<String, List<T>> metadata = new LinkedHashMap<>();
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    DataSource dataSource = _wdkModel.getAppDb().getDataSource();
    try {
      ps = SqlUtils.getPreparedStatement(dataSource, sql);
      ps.setFetchSize(FETCH_SIZE);
      ps.setString(1, ontologyItem.getOntologyId());
      ps.setString(2, ontologyItem.getOntologyId());
      resultSet = ps.executeQuery();
      while (resultSet.next()) {
        String internal = resultSet.getString(COLUMN_INTERNAL);
        T value = OntologyItemType.resolveTypedValue(resultSet, ontologyItem, ontologyItemClass);

        // get list of values for this internal value, creating if not yet present
        List<T> values = metadata.get(internal);
        if (values == null) {
          values = new ArrayList<>();
          metadata.put(internal, values);
        }

        // add next value to the list
        values.add(value);
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
  
  //TODO - CWL Verify
  public Map<String, Set<String>> getValuesMap(User user,
      ValidStableValues contextParamValues) throws WdkModelException {
    
    //contextParamValues = ensureRequiredContext(user, contextParamValues);

    Map<String, OntologyItem> ontology = getOntology(user, contextParamValues);
    return getValuesMap(user, contextParamValues, null, ontology, _wdkModel.getAppDb().getDataSource());
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
  //TODO - CWL Verify 
  Map<String, Set<String>> getValuesMap(User user,
      ValidStableValues contextParamValues, Set<String> ontologyTerms,
      Map<String, OntologyItem> ontology, DataSource dataSource) throws WdkModelException {

    //TODO: temporary till val map query is required
    if (_ontologyValuesQuery == null) return null;
    
    // TODO: restore the following line when the value map query becomes required.
    //if (_ontologyValuesQuery == null) throw new WdkModelException("");
 
    String ontologyValuesSql;
    try {
      CompleteValidStableValues ontologyValues = ValidStableValuesFactory.createFromSupersetValues(
          user, new WriteableStableValues(_ontologyValuesQuery, contextParamValues));
      QueryInstance<?> instance = _ontologyValuesQuery.makeInstance(user, ontologyValues);
      ontologyValuesSql = instance.getSql();
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
    
    String ontologyTermsWhereClause = "";

    if (ontologyTerms != null) {
    // find ontology terms used in our set of member filters
      String ontologyTermsString = ontologyTerms.stream().collect(Collectors.joining("', '"));
      ontologyTermsWhereClause = " where " + FilterParamNew.COLUMN_ONTOLOGY_ID +
          " IN ('"  + ontologyTermsString + "')";
    }

    String filterSelectSql = "SELECT * FROM (" + ontologyValuesSql + ") vm" +  ontologyTermsWhereClause;

    // run sql, and stuff results into map of term -> values
    Map<String, Set<String>> ontologyValues = new HashMap<String, Set<String>>();
    try {
      new SQLRunner(dataSource, filterSelectSql, getFullName() + "__values_map").executeQuery(
          rs -> {
            while (rs.next()) {
              String field = rs.getString(FilterParamNew.COLUMN_ONTOLOGY_ID);
              OntologyItem ontologyItem = ontology.get(field);
	      if (ontologyItem == null) continue;   // in the value map sql, but not in the ontology query.  skip
              OntologyItemType type = ontologyItem.getType();
              Object value = OntologyItemType.resolveTypedValue(rs, ontologyItem, type.getJavaClass());
              String valueString = value == null? "NULL" : value.toString();
              if (!ontologyValues.containsKey(field)) ontologyValues.put(field, new HashSet<String>());
              ontologyValues.get(field).add(valueString);
            }
          });
      return ontologyValues;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException(e.getCause());
    }
  }

   
  /**
   * this is factored out to allow use with an alternative metadata query (eg, the summaryMetadataQuery)
   * @return sql that provides a filtered set of internal IDs
   */
   String getFilteredValue(User user, FilterParamNewStableValue stableValue, ValidStableValues contextParamValues, Query metadataQuery)
       throws WdkModelException {

     try {
       
       // get sql that selects the full set of distinct internals from the metadata query
       String metadataSql;
       CompleteValidStableValues metadataValues = ValidStableValuesFactory.createFromSupersetValues(
           user, new WriteableStableValues(metadataQuery, contextParamValues));
       QueryInstance<?> instance = metadataQuery.makeInstance(user, metadataValues);
       metadataSql = instance.getSql();
       String metadataTableName = "md";
       String filterSelectSql = "SELECT distinct md." + FilterParamNew.COLUMN_INTERNAL + ", md." + FilterParamNew.COLUMN_GLOBAL_INTERNAL + " FROM (" + metadataSql + ") md";
       
       // get the applied filters and the ontology
       List<FilterParamNewStableValue.Filter> filters = stableValue.getFilters();
       Map<String, OntologyItem> ontology = getOntology(user, contextParamValues);
      
       // if no filters, return sql for the full set of internals
       String filteredSql;
       if (filters.size() == 0) {
         filteredSql = filterSelectSql;
       } 
       
       // otherwise apply the filters
       else {
        List<String> filterSqls = new ArrayList<String>();
        for (FilterParamNewStableValue.Filter filter : filters)
          filterSqls.add(filterSelectSql + filter.getFilterAsWhereClause(metadataTableName, ontology));

        filteredSql = FormatUtil.join(filterSqls, " INTERSECT ");
       }
  
       return filteredSql;
     }
     catch (JSONException | WdkUserException ex) {
       throw new WdkModelException(ex);
     }
   }
   

  //TODO - CWL Verify
  public JSONObject getJsonValues(User user, CompleteValidStableValues contextParamValues)
      throws WdkModelException {
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

  /**
   * Default is always no filter... until there is pressure to change this.
   */
  @Override
  public String getDefault(User user, PartiallyValidatedStableValues contextParamValues) {
    return new JSONObject().put(FilterParamNewHandler.FILTERS_KEY, new JSONArray()).toString();
  }

  @Override
  public void setContextQuestion(Question question) throws WdkModelException {

    super.setContextQuestion(question);

    // also set context question in the metadata & ontology queries
    if (_metadataQuery != null)
      _metadataQuery.setContextQuestion(question);
    if (_ontologyQuery != null)
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
  protected ParamValidity validateValue(User user, PartiallyValidatedStableValues contextParamValues)
      throws WdkModelException {

    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(contextParamValues.get(getName()), this);

    String err = stableValue.validateSyntaxAndSemantics(user, contextParamValues, _wdkModel.getAppDb().getDataSource());

    if (err != null) throw new WdkModelException(err);
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
   * this param is stale if either its ontology or value map queries consumes any of the stale depended params
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

      if (_ontologyValuesQuery != null) {
        for (Param param : _ontologyValuesQuery.getParams()) {
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
  protected DependentParamInstance createDependentParamInstance(User user,
      ValidStableValues dependedParamValues) throws WdkModelException {
    return createFilterParamNewInstance();
  }

  @Override
  public String getSanityDefault(User user, ValidStableValues contextParamValues,
      SelectMode sanitySelectMode) {
    return getDefault(user, contextParamValues);
  }

  @Override
  public List<Query> getQueries() {
    List<Query> queries = new ArrayList<Query>();
    if (_backgroundQuery != null) {
      queries.add(_backgroundQuery);
    }
    if (_metadataQuery != null) {
      queries.add(_metadataQuery);
    }
    if (_ontologyQuery != null) {
      queries.add(_ontologyQuery);
    }
    return queries;
  }

}
