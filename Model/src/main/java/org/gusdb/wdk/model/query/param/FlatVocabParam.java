package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
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
public class FlatVocabParam extends AbstractEnumParam implements ItemFetcher<String, EnumParamVocabInstance>{

  public static final String PARAM_SERVED_QUERY = "ServedQuery";
  public static final String DEPENDED_VALUE = "depended_value";

  public static final String COLUMN_TERM = "term";
  public static final String COLUMN_INTERNAL = "internal";
  public static final String COLUMN_DISPLAY = "display";
  public static final String COLUMN_PARENT_TERM = "parentTerm";

  private static final String VOCAB_QUERY_REF_KEY = "vocabQueryRef";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  private Query vocabQuery;
  private String vocabQueryRef;
  private static ItemCache<String, EnumParamVocabInstance> vocabCache = new ItemCache<String, EnumParamVocabInstance>();

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

  public FlatVocabParam() {}

  public FlatVocabParam(FlatVocabParam param) {
    super(param);
    this.servedQueryName = param.servedQueryName;
    this.vocabQueryRef = param.vocabQueryRef;
    if (param.vocabQuery != null)
      this.vocabQuery = param.vocabQuery.clone();
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
  }

  protected Query resolveQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
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
  protected EnumParamVocabInstance createVocabInstance(User user, Map<String, String> dependedParamValues)
      throws WdkModelException, WdkUserException {
    logger.trace("Entering createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");

    EnumParamVocabInstance vocabInstance = null;
    try {
      vocabInstance = vocabCache.getItem(getCacheKey(dependedParamValues), this);
    } catch (UnfetchableItemException e) {
      throw new WdkModelException(e);
    }
    
    // check if the query has "display" column
    boolean hasDisplay = vocabQuery.getColumnMap().containsKey(COLUMN_DISPLAY);
    boolean hasParent = vocabQuery.getColumnMap().containsKey(COLUMN_PARENT_TERM);

    // prepare param values
    Map<String, String> values = new LinkedHashMap<String, String>();
    values.put(PARAM_SERVED_QUERY, servedQueryName);

    // add depended value if is dependent param
    Set<Param> dependedParams = getDependedParams();
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

    QueryInstance<?> instance = vocabQuery.makeInstance(user, values, false, 0, context);
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

      vocabInstance.addTermValues(term, value, display, parentTerm);
    }
    if (vocabInstance.isEmpty()) {
      if (vocabQuery instanceof SqlQuery)
        logger.warn("vocab query returned 0 rows:" + ((SqlQuery) vocabQuery).getSql());
      throw new WdkModelException("No item returned by the query [" + vocabQuery.getFullName() +
          "] of FlatVocabParam [" + getFullName() + "].");
    }
    else {
      logger.debug("Query [" + vocabQuery.getFullName() + "] returned " + vocabInstance.getNumTerms() +
          " of FlatVocabParam [" + getFullName() + "].");
    }
    initTreeMap(vocabInstance);
    applySelectMode(vocabInstance);
    logger.debug("Leaving createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");
    logger.debug("Returning cache with default value '" + vocabInstance.getDefaultValue() +
        "' out of possible terms: " + FormatUtil.arrayToString(vocabInstance.getTerms().toArray()));
    return vocabInstance;
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
   * We don't need to read the vocabQueryRef from the cache key, because we know it 
   * is the same as the one in this's state.
   */
  public EnumParamVocabInstance fetchItem(String cacheKey) {
    JSONObject cacheKeyJson = new JSONObject(cacheKey);
    JSONObject dependedParamValuesJson = cacheKeyJson.getJSONObject(DEPENDED_PARAM_VALUES_KEY);
    Iterator<String> paramNames = dependedParamValuesJson.keys();
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    while( paramNames.hasNext() ) {
        String paramName = (String)paramNames.next();
        dependedParamValues.put(paramName, dependedParamValuesJson.getString(paramName));
    }
    return new EnumParamVocabInstance(dependedParamValues);
  }
  
  public EnumParamVocabInstance updateItem(String key, EnumParamVocabInstance item) {
    return null;
  }

  private String getCacheKey(Map<String, String> dependedParamValues) throws WdkModelException, JSONException {
   JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(VOCAB_QUERY_REF_KEY, vocabQueryRef);
    JSONObject dependedParamValuesJson = new JSONObject();
    for (String paramName : dependedParamValues.keySet()) 
      if (getDependedParams() != null && getDependedParams().contains(paramName)) dependedParamValuesJson.put(paramName, dependedParamValues.get(paramName));
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY, dependedParamValuesJson);
    return cacheKeyJson.toString();
  }
  
  public boolean itemNeedsUpdating(EnumParamVocabInstance item) {
    return false;
   }
   

}
