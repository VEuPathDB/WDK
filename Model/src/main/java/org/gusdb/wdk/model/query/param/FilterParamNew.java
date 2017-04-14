package org.gusdb.wdk.model.query.param;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.db.SqlUtils;
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
 *         The input from the user is in the form of a set of filters.  each filter specifies an ontology entry
 *         and one or more values for it.  The param returns all internal values from the metadata query that match all
 *         supplied filters.  (Ie, the filtering is done on the backend).
 *         
 *         (the filter param handler provides summary information to the client about the distribution of
 *         values for a requested ontology entry)
 *         
 *         the param is also configured by a flag in the model xml indicating if the client is allowed to do
 *         filtering as the user interacts, instead of asking the backend for summaries each time.  
 *         doing so requires the client to download the results of the metadata query for a particular
 *         ontology entry.  this should only be allowed if the metadata query cardinality is small.  
 *         when the question is submitted, the client still sends as a raw value the set of applied filters
 *         
 *         the metadataQuery can be a dependent query.
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

  public static class MetadataCache extends ItemCache<String, Map<String, Map<String, String>>> { }

  // ontology query columns
  static final String COLUMN_ONTOLOGY_ID = "ontology_term_name";
  static final String COLUMN_PARENT_ONTOLOGY_ID = "parent_ontology_term_name";
  static final String COLUMN_DISPLAY_NAME = "display_name";
  static final String COLUMN_DESCRIPTION = "description";
  static final String COLUMN_TYPE = "type";
  static final String COLUMN_UNITS = "units";
  static final String COLUMN_PRECISION = "precision";
  
  // metadata query columns
  private static final String COLUMN_INTERNAL = "internal";
  static final String COLUMN_NUMBER_VALUE = "number_value";
  static final String COLUMN_DATE_VALUE = "date_value";
  static final String COLUMN_STRING_VALUE = "string_value";

  private static final int FETCH_SIZE = 1000;

  private String metadataQueryRef;
  private Query metadataQuery;

  private String ontologyQueryRef;
  private Query ontologyQuery;

  private String defaultColumns;
  
 /*
  * set true in model xml if metadata size is small enuf to send to client.  if so, the client can
  * adjust histograms, etc, as user interacts, without having to go to the server for each interaction.
  */
  private Boolean allowClientFiltering;   

  // remove non-terminal nodes with a single child
  private boolean trimMetadataTerms = true;

  public FilterParamNew() {
    // register handlers
    setHandler(new FilterParamHandler());
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
    if (param.defaultColumns != null)
      this.defaultColumns = param.defaultColumns;
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
   * @return the defaultColumns
   */
  public String getDefaultColumns() {
    return defaultColumns;
  }

  /**
   * @param defaultColumns
   *          the metadataQuery to set
   */
  public void setDefaultColumns(String defaultColumns) {
    this.defaultColumns = defaultColumns;
  }

  /**
   * @return the trimMetadataTerms
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

    // resolve ontology query, which should not have any params
    if (ontologyQueryRef != null) {
      // no need to clone metadataSpec query, since it's not overridden in any way here.
      this.ontologyQuery = (Query) model.resolveReference(ontologyQueryRef);

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
      this.metadataQuery = resolveQuery(model, metadataQueryRef, "metadata query");

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
   * @return <propertyName, <infoKey, infoValue>>, or null if metadataQuery is not specified
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<String, Map<String, String>> getOntology(User user, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    if (ontologyQuery == null)
      return null;

    OntologyItemFetcher fetcher = new OntologyItemFetcher(ontologyQuery, contextParamValues, user);
    Map<String, Map<String, String>> map = null;
    try {
      map = CacheMgr.get().getOntologyCache().getItem(fetcher.getCacheKey(), fetcher);
    }
    catch (UnfetchableItemException ex) {
      decodeException(ex);
    }
    return map;
  }

  /**
   * @return <term, <propertyName, propertyValue>>, or null if propertyQuery is not specified.
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<String, Map<String, String>> getMetadata(User user, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    
    if (!allowClientFiltering) 
      throw new WdkModelException("FilterParam " + getFullName() + " does not allow client side filtering.  Illegally attempting to get metadata");
    
    if (metadataQuery == null)
      return null;

    MetaDataItemFetcher fetcher = new MetaDataItemFetcher(metadataQuery, contextParamValues, user);
    Map<String, Map<String, String>> map = null;
    try {
      map = CacheMgr.get().getMetadataCache().getItem(fetcher.getCacheKey(), fetcher);
    }
    catch (UnfetchableItemException ex) {
      decodeException(ex);
    }
    return map;
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
  public Map<String, List<String>> getMetaData(User user, Map<String, String> contextParamValues, String ontologyId,
      FilterParamNewInstance cache) throws WdkModelException, WdkUserException {
    
    if (!allowClientFiltering) 
      throw new WdkModelException("FilterParam " + getFullName() + " does not allow client side filtering.  Illegally attempting to get metadata");

    if (metadataQuery == null) return null;
    
    // compose a wrapped sql
    QueryInstance<?> instance = metadataQuery.makeInstance(user, contextParamValues, true, 0, contextParamValues);
    String sql = instance.getSql();
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
      Map<String, Map<String, String>> metadataSpec = getOntology(user, contextParamValues);
      for (String property : metadataSpec.keySet()) {
        JSONObject jsSpec = new JSONObject();
        Map<String, String> spec = metadataSpec.get(property);
        for (String specProp : spec.keySet()) {
          jsSpec.put(specProp, spec.get(specProp));
        }
        jsOntology.put(property, jsSpec);
      }
      jsParam.put("ontology", jsOntology);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsParam;
  }

  protected String getValidStableValue(User user, String stableValue, Map<String, String> contextParamValues,
      FilterParamNewInstance cache) throws WdkModelException {
    try {
      if (stableValue == null || stableValue.length() == 0) {
        JSONObject jsNewStableValue = new JSONObject();
        jsNewStableValue.put(FilterParamHandler.FILTERS_KEY, new JSONArray());
        return jsNewStableValue.toString();
      }

      JSONObject jsStableValue = new JSONObject(stableValue);
 
      // TODO: validate stable value
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
    // TODO fix this.
    String defaultValue = new JSONObject().toString();
    return defaultValue;
  }

  @Override
  public void setContextQuestion(Question question)  throws WdkModelException {
    super.setContextQuestion(question);
    // also set context query to the metadata & spec queries
    if (metadataQuery != null)
      metadataQuery.setContextQuestion(question);
    if (ontologyQuery != null)
      ontologyQuery.setContextQuestion(question);
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    // TODO probably format this nicely
    return rawValue.toString();
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) { }

  @Override
  protected void validateValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    // TODO validate against ontology and metadata
    
  }

  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException {
    // TODO Auto-generated method stub
    
  }
}
 