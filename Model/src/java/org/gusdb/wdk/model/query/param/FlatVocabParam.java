package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The FlatVocab param represents a list of param values that user can choose from. The difference between
 * FlatVocabParam and EnumParam is that EnumParam declares the list of param values in the model, while
 * FlatVocabParam get the list from a param query.
 * 
 * The param query doesn't usually have any param, but if the FlatVocabParam depends on another param, its
 * param query will have a param that is a reference to the other param.
 * 
 * @author jerric
 * 
 */
public class FlatVocabParam extends AbstractEnumParam {

  public static final String PARAM_SERVED_QUERY = "ServedQuery";
  public static final String DEPENDED_VALUE = "depended_value";

  public static final String COLUMN_TERM = "term";
  public static final String COLUMN_INTERNAL = "internal";
  public static final String COLUMN_DISPLAY = "display";
  public static final String COLUMN_PARENT_TERM = "parentTerm";

  private static final String COLUMN_PROPERTY = "property";
  private static final String COLUMN_VALUE = "value";

  private static final String COLUMN_SPEC_PROPERTY = "spec_property";
  private static final String COLUMN_SPEC_VALUE = "spec_value";

  private Query vocabQuery;
  private String vocabQueryRef;

  /**
   * The name of the query where is param is used. Please note that each query hold a separate copy of the
   * params, so each object of param will belong to only one query.
   * 
   * This data is used mostly when the param query is a ProcessQuery, and this information is passed from
   * portal to the component sites, so that the component can find the correct param to use from its parent
   * query. The param can't be simply looked up with the name from model, since each query might have
   * customized the param, therefore, we can only get the correct param from the correct query. (If the query
   * is an ID query, we will need to get the correct question first, then get query from question.)
   */
  private String servedQueryName = "unknown";

  private String metadataQueryRef;
  private Query metadataQuery;

  private String metadataSpecQueryRef;
  private Query metadataSpecQuery;

  public FlatVocabParam() {}

  public FlatVocabParam(FlatVocabParam param) {
    super(param);
    this.servedQueryName = param.servedQueryName;
    this.vocabQueryRef = param.vocabQueryRef;
    if (param.vocabQuery != null)
      this.vocabQuery = param.vocabQuery.clone();
    this.metadataQueryRef = param.metadataQueryRef;
    if (param.metadataQuery != null)
      this.metadataQuery = param.metadataQuery.clone();
    this.metadataSpecQueryRef = param.metadataSpecQueryRef;
    if (param.metadataSpecQuery != null)
      this.metadataSpecQuery = param.metadataSpecQuery.clone();
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public void setQueryRef(String queryTwoPartName) {

    this.vocabQueryRef = queryTwoPartName;
  }

  public Query getQuery() {
    return vocabQuery;
  }

  /**
   * @return the propertyQueryName
   */
  public String getMetadataQueryRef() {
    return metadataQueryRef;
  }

  /**
   * @param metadataQueryRef
   *          the propertyQueryName to set
   */
  public void setMetadataQueryRef(String metadataQueryRef) {
    this.metadataQueryRef = metadataQueryRef;
  }

  /**
   * @return the propertyQuery
   */
  public Query getMetadataQuery() {
    return metadataQuery;
  }

  /**
   * @param propertyQuery
   *          the propertyQuery to set
   */
  public void setMetadataQuery(Query metadataQuery) {
    this.metadataQuery = metadataQuery;
  }

  /**
   * @return the metadataSpec Query Name
   */
  public String getMetadataSpecQueryRef() {
    return metadataSpecQueryRef;
  }

  /**
   * @param metadataSpecQueryRef
   *          the metadataQueryName to set
   */
  public void setMetadataSpecQueryRef(String metadataSpecQueryRef) {
    this.metadataSpecQueryRef = metadataSpecQueryRef;
  }

  /**
   * @return the metadataQuery
   */
  public Query getMetadataSpecQuery() {
    return metadataSpecQuery;
  }

  /**
   * @param metadataSpecQuery
   *          the metadataQuery to set
   */
  public void setMetadataSpecQuery(Query metadataSpecQuery) {
    this.metadataSpecQuery = metadataSpecQuery;
  }

  @Override
  public void setContextQuestion(Question question) {
    super.setContextQuestion(question);
    vocabQuery.setContextQuestion(question);
  }

  /**
   * @param servedQueryName
   *          the servedQueryName to set
   */
  public void setServedQueryName(String servedQueryName) {
    this.servedQueryName = servedQueryName;
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#resolveReferences(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // resolve vocab query
    this.vocabQuery = resolveQuery(model, vocabQueryRef, "vocab query");

    // resolve property query, the property query should have the same params as vocab query
    if (metadataQueryRef != null) {
      this.metadataQuery = resolveQuery(model, metadataQueryRef, "property query");
      // the propertyQuery must have exactly 3 columns: term, property, and value.
      Map<String, Column> columns = metadataQuery.getColumnMap();
      if (columns.size() != 3 || !columns.containsKey(COLUMN_TERM) || !columns.containsKey(COLUMN_PROPERTY) ||
          !columns.containsKey(COLUMN_VALUE))
        throw new WdkModelException("The propertyQuery " + metadataQueryRef + " in flatVocabParam " +
            getFullName() + " must have exactly three columns: " + COLUMN_TERM + ", " + COLUMN_PROPERTY +
            ", and " + COLUMN_VALUE + ".");
    }

    // resolve metadata query, which should not have any param
    if (metadataSpecQueryRef != null) {
      // no need to clone metadata query, since it's not overriden in any way here.
      this.metadataSpecQuery = (Query) model.resolveReference(metadataSpecQueryRef);
      // make sure metadata query don't have any params
      Param[] params = metadataSpecQuery.getParams();
      if (params.length > 2 || (params.length == 0 && !params[0].getName().equals(Utilities.PARAM_USER_ID)))
        throw new WdkModelException("The metadata query " + metadataSpecQueryRef + " in FlatVocabParam " +
            getFullName() + " cannot have any params.");

      // the metadata query must have exactly 3 columns: property, info, data.
      Map<String, Column> columns = metadataSpecQuery.getColumnMap();
      if (columns.size() != 3 || !columns.containsKey(COLUMN_PROPERTY) || !columns.containsKey(COLUMN_SPEC_PROPERTY) ||
          !columns.containsKey(COLUMN_SPEC_VALUE))
        throw new WdkModelException("The metadataQuery " + metadataSpecQueryRef + " in flatVocabParam " +
            getFullName() + " must have exactly three columns: " + COLUMN_PROPERTY + ", " + COLUMN_SPEC_PROPERTY +
            ", and " + COLUMN_SPEC_VALUE + ".");
    }
  }

  private Query resolveQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
    queryType += " ";

    // the vocab query is always cloned to keep a reference to the param
    Query query = (Query) model.resolveReference(queryName);
    query.resolveReferences(model);
    query = query.clone();

    // if the query has params, they should match the depended params
    Set<Param> params = getDependedParams();
    Set<String> paramNames = new HashSet<>();
    if (params != null) {
      for (Param param : params) {
        paramNames.add(param.getName());
      }
    }

    // all param in the vocab param should match the depended params;
    for (Param param : query.getParams()) {
      String paramName = param.getName();
      if (paramName.equals(PARAM_SERVED_QUERY))
        continue;
      if (!paramNames.contains(paramName))
        throw new WdkModelException("The " + queryType + query.getFullName() + " requires a depended param " +
            paramName + ", but the vocab param " + getFullName() + " doesn't depend on it.");
    }
    // all depended params should match the params in the vocab query;
    Map<String, Param> vocabParams = query.getParamMap();
    for (String paramName : paramNames) {
      if (!vocabParams.containsKey(paramName))
        throw new WdkModelException("The dependent param " + getFullName() + " depends on param " +
            paramName + ", but the " + queryType + query.getFullName() + " doesn't use this depended param.");

    }

    // add a served query param into flatVocabQuery, if it doesn't exist
    ParamSet paramSet = model.getParamSet(Utilities.INTERNAL_PARAM_SET);
    StringParam param = new StringParam();
    param.setName(PARAM_SERVED_QUERY);
    param.setDefault(servedQueryName);
    param.setAllowEmpty(true);
    param.resolveReferences(model);
    param.setResources(model);
    paramSet.addParam(param);
    query.addParam(param);
    return query;

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#setResources(org.gusdb.wdk.model.WdkModel)
   */
  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    super.setResources(model);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.param.AbstractEnumParam#initVocabMap()
   */
  @Override
  protected EnumParamCache createEnumParamCache(User user, Map<String, String> dependedParamValues)
      throws WdkModelException {
    logger.trace("Entering createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");

    Set<Param> dependedParams = getDependedParams();

    // String errorStr = "Could not retrieve flat vocab values for param "
    // + getName() + " using depended value "
    // + Utilities.print(dependedParamValues);

    EnumParamCache cache = new EnumParamCache(this, dependedParamValues);

    // check if the query has "display" column
    boolean hasDisplay = vocabQuery.getColumnMap().containsKey(COLUMN_DISPLAY);
    boolean hasParent = vocabQuery.getColumnMap().containsKey(COLUMN_PARENT_TERM);

    // prepare param values
    Map<String, String> values = new LinkedHashMap<String, String>();
    values.put(PARAM_SERVED_QUERY, servedQueryName);

    // add depended value if is dependent param
    if (isDependentParam()) {
      // use the depended param as the input param for the vocab query,
      // since the depended param might be overridden by question or
      // query, while the original input param in the vocab query
      // does not know about it.
      for (Param param : dependedParams) {
        // preserve the context query
        Query contextQuery = param.getContextQuery();
        param = param.clone();
        vocabQuery.addParam(param);
        param.setContextQuery(contextQuery);
        String value = dependedParamValues.get(param.getName());
        values.put(param.getName(), value);
      }
    }

    Map<String, String> context = new LinkedHashMap<String, String>();
    context.put(Utilities.QUERY_CTX_PARAM, getFullName());
    if (contextQuestion != null)
      context.put(Utilities.QUERY_CTX_QUESTION, contextQuestion.getFullName());
    logger.debug("PARAM [" + getFullName() + "] query=" + vocabQuery.getFullName() + ", context Question: " +
        ((contextQuestion == null) ? "N/A" : contextQuestion.getFullName()) + ", context Query: " +
        ((contextQuery == null) ? "N/A" : contextQuery.getFullName()));

    QueryInstance instance = vocabQuery.makeInstance(user, values, false, 0, context);
    ResultList result = instance.getResults();
    while (result.next()) {
      Object objTerm = result.get(COLUMN_TERM);
      Object objInternal = result.get(COLUMN_INTERNAL);
      if (objTerm == null)
        throw new WdkModelException("The term of flatVocabParam [" + getFullName() + "] is null. query [" +
            vocabQuery.getFullName() + "].\n" + instance.getSql());
      if (objInternal == null)
        throw new WdkModelException("The internal of flatVocabParam [" + getFullName() +
            "] is null. query [" + vocabQuery.getFullName() + "].\n" + instance.getSql());

      String term = objTerm.toString().trim();
      String value = objInternal.toString().trim();
      String display = hasDisplay ? result.get(COLUMN_DISPLAY).toString().trim() : term;
      String parentTerm = null;
      if (hasParent) {
        Object parent = result.get(COLUMN_PARENT_TERM);
        if (parent != null)
          parentTerm = parent.toString().trim();
      }

      // escape the term & parentTerm
      // term = term.replaceAll("[,]", "_");
      // if (parentTerm != null)
      // parentTerm = parentTerm.replaceAll("[,]", "_");
      if (term.indexOf(',') >= 0 && dependedParams != null)
        throw new WdkModelException(this.getFullName() + ": The term cannot contain comma: '" + term + "'");
      if (parentTerm != null && parentTerm.indexOf(',') >= 0)
        throw new WdkModelException(this.getFullName() + ": The parent term cannot contain " + "comma: '" +
            parentTerm + "'");

      cache.addTermValues(term, value, display, parentTerm);
    }
    if (cache.isEmpty()) {
      if (vocabQuery instanceof SqlQuery)
        logger.warn("vocab query returned 0 rows:" + ((SqlQuery) vocabQuery).getSql());
      throw new WdkModelException("No item returned by the query [" + vocabQuery.getFullName() +
          "] of FlatVocabParam [" + getFullName() + "].");
    }
    else {
      logger.debug("Query [" + vocabQuery.getFullName() + "] returned " + cache.getNumTerms() +
          " of FlatVocabParam [" + getFullName() + "].");
    }
    initTreeMap(cache);
    applySelectMode(cache);
    logger.trace("Leaving createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");
    return cache;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#clone()
   */
  @Override
  public Param clone() {
    return new FlatVocabParam(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
   */
  @Override
  protected void appendJSONContent(JSONObject jsParam, boolean extra) throws JSONException {
    if (extra) {
      // add underlying query name to it
      jsParam.append("query", vocabQuery.getFullName());
    }
  }

  @Override
  public void setContextQuery(Query query) {
    super.setContextQuery(query);
    if (contextQuery != null)
      this.servedQueryName = contextQuery.getFullName();
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // also print out the vocab query
    vocabQuery.printDependency(writer, indent);
  }

  /**
   * @return <propertyName, <infoKey, infoValue>>, or null if metadataQuery is not specified
   * @throws WdkModelException
   */
  public Map<String, Map<String, String>> getMetadataSpec(User user) throws WdkModelException {
    if (metadataSpecQuery == null)
      return null;

    // run metadataQuery. By definition, it doesn't have any params
    Map<String, String> params = new HashMap<>();
    QueryInstance instance = metadataSpecQuery.makeInstance(user, params, true, 0, new HashMap<String, String>());
    Map<String, Map<String, String>> metadata = new LinkedHashMap<>();
    ResultList resultList = instance.getResults();
    try {
      while (resultList.next()) {
        String property = (String) resultList.get(COLUMN_PROPERTY);
        String info = (String) resultList.get(COLUMN_SPEC_PROPERTY);
        String data = (String) resultList.get(COLUMN_SPEC_VALUE);
        Map<String, String> propertyMeta = metadata.get(property);
        if (propertyMeta == null) {
          propertyMeta = new LinkedHashMap<>();
          metadata.put(property, propertyMeta);
        }
        propertyMeta.put(info, data);
      }
    }
    finally {
      resultList.close();
    }
    return metadata;
  }

  /**
   * @return <term, <propertyName, propertyValue>>, or null if propertyQuery is not specified.
   * @throws WdkModelException
   */
  public Map<String, Map<String, String>> getMetadata(User user, Map<String, String> contextValues)
      throws WdkModelException {
    if (metadataQuery == null)
      return null;

    QueryInstance instance = metadataQuery.makeInstance(user, contextValues, true, 0,
        new HashMap<String, String>());
    Map<String, Map<String, String>> properties = new LinkedHashMap<>();
    ResultList resultList = instance.getResults();
    try {
      while (resultList.next()) {
        String term = (String) resultList.get(COLUMN_TERM);
        String property = (String) resultList.get(COLUMN_PROPERTY);
        String value = (String) resultList.get(COLUMN_VALUE);
        Map<String, String> termProp = properties.get(term);
        if (termProp == null) {
          termProp = new LinkedHashMap<>();
          properties.put(term, termProp);
        }
        termProp.put(property, value);
      }
    }
    finally {
      resultList.close();
    }
    return properties;
  }

  public boolean isFilterParam() {
    return (metadataSpecQuery != null);
  }

  @Override
  public JSONObject getJSONValues(User user, Map<String, String> contextValues, EnumParamCache cache) throws WdkModelException {
    JSONObject jsParam = super.getJSONValues(user, contextValues, cache);
    try { // add additional info into the json
      appendJSONFilterValue(jsParam, user, contextValues);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsParam;
  }

  private void appendJSONFilterValue(JSONObject jsParam, User user, Map<String, String> contextValues)
      throws JSONException, WdkModelException {
    if (metadataSpecQuery == null)
      return;

    // create json for the metadata
    JSONObject jsMetadataSpec = new JSONObject();
    Map<String, Map<String, String>> metadataSpec = getMetadataSpec(user);
    for (String property : metadataSpec.keySet()) {
      JSONObject jsSpec = new JSONObject();
      Map<String, String> spec = metadataSpec.get(property);
      for (String specProp : spec.keySet()) {
        jsSpec.put(specProp, spec.get(specProp));
      }
      jsMetadataSpec.put(property, jsSpec);
    }
    jsParam.put("metadataSpec", jsMetadataSpec);

    // create json for the properties
    JSONObject jsMetadata = new JSONObject();
    Map<String, Map<String, String>> metadata = getMetadata(user, contextValues);
    for (String term : metadata.keySet()) {
      JSONObject jsProperty = new JSONObject();
      Map<String, String> property = metadata.get(term);
      for (String propName : property.keySet()) {
        jsProperty.put(propName, property.get(propName));
      }
      jsMetadata.put(term, jsProperty);
    }
    jsParam.put("metadata", jsMetadata);
  }
}
