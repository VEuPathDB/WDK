package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.cache.ItemCache;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
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

  static final String PARAM_SERVED_QUERY = "ServedQuery";
  static final String DEPENDED_VALUE = "depended_value";
  static final String VOCAB_QUERY_REF_KEY = "vocabQueryRef";
  static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  public static final String COLUMN_TERM = "term";
  public static final String COLUMN_INTERNAL = "internal";
  public static final String COLUMN_DISPLAY = "display";
  public static final String COLUMN_PARENT_TERM = "parentTerm";

  private static ItemCache<String, EnumParamVocabInstance> VOCAB_CACHE = new ItemCache<String, EnumParamVocabInstance>();

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
    try {
      return VOCAB_CACHE.getItem(getCacheKey(dependedParamValues), new FlatVocabularyFetcher(user, this));
    }
    catch (UnfetchableItemException e) {
      throw new WdkModelException(e);
    }
  }

  private String getCacheKey(Map<String, String> dependedParamValues) throws WdkModelException, JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(VOCAB_QUERY_REF_KEY, vocabQueryRef);
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY, getDependedParamValuesJson(dependedParamValues));
    return cacheKeyJson.toString();
  }

  private JSONObject getDependedParamValuesJson(Map<String, String> dependedParamValues) throws WdkModelException {
    JSONObject dependedParamValuesJson = new JSONObject();
    Set<Param> dependedParams = getDependedParams();
    if (dependedParams == null || dependedParams.isEmpty())
      return dependedParamValuesJson;
    // get depended param names in advance since getDependedParams() is expensive
    List<String> dependedParamNames = Functions.mapToList(
        dependedParams, new Function<Param, String>() {
          @Override public String apply(Param obj) { return obj.getName(); }});
    for (String paramName : dependedParamValues.keySet()) {
      if (dependedParamNames.contains(paramName)) {
        dependedParamValuesJson.put(paramName, dependedParamValues.get(paramName));
      }
    }
    return dependedParamValuesJson;
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
      servedQueryName = contextQuery.getFullName();
  }

  public String getServedQueryName() {
    return servedQueryName;
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // also print out the vocab query
    vocabQuery.printDependency(writer, indent);
  }

}
