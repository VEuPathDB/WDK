package org.gusdb.wdk.model.query.param;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
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

  public static final String COLUMN_TERM = "term";
  public static final String COLUMN_INTERNAL = "internal";
  public static final String COLUMN_DISPLAY = "display";
  public static final String COLUMN_PARENT_TERM = "parentTerm";

  private Query vocabQuery;
  private String vocabQueryRef;

  @Override
  public Set<String> getContainedQueryFullNames() {
    Set<String> names = new HashSet<>();
    names.add(vocabQueryRef);
    return names;
  }

  @Override
  public List<Query> getQueries() {
    List<Query> queries = new ArrayList<>();
    queries.add(vocabQuery);
    return queries;
  }

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
  public void setContextQuestion(Question question)  throws WdkModelException {
    super.setContextQuestion(question);
    vocabQuery.setContextQuestion(question);
    vocabQuery.setContextParam(this);
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
    this.vocabQuery = resolveDependentQuery(model, vocabQueryRef, "vocab query");
  }

  @Override
  protected Query resolveDependentQuery(WdkModel model, String queryName, String queryType) throws WdkModelException {
    Query query = super.resolveDependentQuery(model, queryName, queryType);

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

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    super.setResources(model);
  }

  @Override
  protected EnumParamVocabInstance getVocabInstance(User user, Map<String, String> dependedParamValues)
      throws WdkModelException {
    try {
      FlatVocabularyFetcher fetcher = new FlatVocabularyFetcher(user, this);
      return (vocabQuery.getIsCacheable() ?
          CacheMgr.get().getVocabCache().getValue(fetcher.getCacheKey(dependedParamValues), fetcher) :
          fetcher.fetchItem(dependedParamValues));
    }
    catch (ValueProductionException e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * flat vocab params are always stale if any depended param is stale
   */
  @Override
  public boolean isStale(Set<String> staleDependedParamsFullNames) {
    return true;
  }

  @Override
  public Param clone() {
    return new FlatVocabParam(this);
  }

  @Override
  protected void appendChecksumJSON(JSONObject jsParam, boolean extra) throws JSONException {
    if (extra) {
      // add underlying query name to it
      jsParam.append("query", vocabQuery.getFullName());
    }
  }

  @Override
  public void setContainer(ParameterContainer query) {
    super.setContainer(query);
    if (_container != null)
      servedQueryName = _container.getFullName();
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
