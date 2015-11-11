package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.wdk.model.Utilities;
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

public class FlatVocabularyFetcher implements ItemFetcher<String, EnumParamVocabInstance> {

  private static final Logger logger = Logger.getLogger(FlatVocabularyFetcher.class);

  private static final String VOCAB_QUERY_REF_KEY = "vocabQueryRef";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";
  
  private final User _user;
  private final FlatVocabParam _param;
  private final Query _vocabQuery;

  public FlatVocabularyFetcher(User user, FlatVocabParam param) {
    _user = user;
    _param = param;
    _vocabQuery = param.getQuery();
  }

  public String getCacheKey(Map<String, String> dependedParamValues) throws WdkModelException, JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(VOCAB_QUERY_REF_KEY, _vocabQuery.getFullName());
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY,
        getDependedParamValuesJson(dependedParamValues, _param.getDependedParams()));
    return cacheKeyJson.toString();
  }

  private static JSONObject getDependedParamValuesJson(
      Map<String, String> dependedParamValues, Set<Param> dependedParams) {
    JSONObject dependedParamValuesJson = new JSONObject();
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
  /**
   * We don't need to read the vocabQueryRef from the cache key, because we know
   * it is the same as the one in the param's state.
   * 
   * @throws UnfetchableItemException if unable to fetch item
   */
  @Override
  public EnumParamVocabInstance fetchItem(String cacheKey) throws UnfetchableItemException {

    JSONObject cacheKeyJson = new JSONObject(cacheKey);
    logger.info("Fetching vocab instance for key: " + cacheKeyJson.toString(2));
    JSONObject dependedParamValuesJson = cacheKeyJson.getJSONObject(DEPENDED_PARAM_VALUES_KEY);
    Iterator<String> paramNames = dependedParamValuesJson.keys();
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    while (paramNames.hasNext()) {
      String paramName = paramNames.next();
      dependedParamValues.put(paramName, dependedParamValuesJson.getString(paramName));
    }
    // create and populate vocab instance
    EnumParamVocabInstance vocabInstance = new EnumParamVocabInstance(dependedParamValues);
    populateVocabInstance(vocabInstance);
    return vocabInstance;
  }

  private void populateVocabInstance(EnumParamVocabInstance vocabInstance) throws UnfetchableItemException {
    try {
      // check if the query has "display" column
      boolean hasDisplay = _vocabQuery.getColumnMap().containsKey(FlatVocabParam.COLUMN_DISPLAY);
      boolean hasParent = _vocabQuery.getColumnMap().containsKey(FlatVocabParam.COLUMN_PARENT_TERM);

      // prepare param values
      Map<String, String> values = new LinkedHashMap<String, String>();
      values.put(FlatVocabParam.PARAM_SERVED_QUERY, _param.getServedQueryName());

      // add depended value if is dependent param
      Set<Param> dependedParams = _param.getDependedParams();
      if (_param.isDependentParam()) {
        // use the depended param as the input param for the vocab query,
        // since the depended param might be overridden by question or
        // query, while the original input param in the vocab query
        // does not know about it.
        for (Param param : dependedParams) {
          // preserve the context query
          Query contextQuery = param.getContextQuery();
          param = param.clone();
          _vocabQuery.addParam(param);
          param.setContextQuery(contextQuery);
          String value = vocabInstance.getDependedValues().get(param.getName());
          values.put(param.getName(), value);
        }
      }

      Question contextQuestion = _param.getContextQuestion();
      Query contextQuery = _param.getContextQuery();
      Map<String, String> context = new LinkedHashMap<String, String>();
      context.put(Utilities.QUERY_CTX_PARAM, _param.getFullName());
      if (_param.getContextQuestion() != null)
        context.put(Utilities.QUERY_CTX_QUESTION, contextQuestion.getFullName());
      logger.debug("PARAM [" + _param.getFullName() + "] query=" + _vocabQuery.getFullName() +
          ", context Question: " + ((contextQuestion == null) ? "N/A" : contextQuestion.getFullName()) +
          ", context Query: " + ((contextQuery == null) ? "N/A" : contextQuery.getFullName()));

      QueryInstance<?> instance = _vocabQuery.makeInstance(_user, values, false, 0, context);
      ResultList result = instance.getResults();
      while (result.next()) {
        Object objTerm = result.get(FlatVocabParam.COLUMN_TERM);
        Object objInternal = result.get(FlatVocabParam.COLUMN_INTERNAL);
        if (objTerm == null)
          throw new WdkModelException("The term of flatVocabParam [" + _param.getFullName() +
              "] is null. query [" + _vocabQuery.getFullName() + "].\n" + instance.getSql());
        if (objInternal == null)
          throw new WdkModelException("The internal of flatVocabParam [" + _param.getFullName() +
              "] is null. query [" + _vocabQuery.getFullName() + "].\n" + instance.getSql());

        String term = objTerm.toString().trim();
        String value = objInternal.toString().trim();
        String display = hasDisplay ? result.get(FlatVocabParam.COLUMN_DISPLAY).toString().trim() : term;
        String parentTerm = null;
        if (hasParent) {
          Object parent = result.get(FlatVocabParam.COLUMN_PARENT_TERM);
          if (parent != null)
            parentTerm = parent.toString().trim();
        }

        if (term.indexOf(',') >= 0 && dependedParams != null)
          throw new WdkModelException(_param.getFullName() + ":" +
              "The term cannot contain comma: '" + term + "'");

        if (parentTerm != null && parentTerm.indexOf(',') >= 0)
          throw new WdkModelException(_param.getFullName() +
              ": The parent term cannot contain " + "comma: '" + parentTerm + "'");

        vocabInstance.addTermValues(term, value, display, parentTerm);
      }

      if (vocabInstance.isEmpty()) {
        if (_vocabQuery instanceof SqlQuery)
          logger.warn("vocab query returned 0 rows:" + ((SqlQuery) _vocabQuery).getSql());
        throw new WdkModelException("No item returned by the query [" + _vocabQuery.getFullName() +
            "] of FlatVocabParam [" + _param.getFullName() + "].");
      }
      else {
        logger.debug("Query [" + _vocabQuery.getFullName() + "] returned " + vocabInstance.getNumTerms() +
            " of FlatVocabParam [" + _param.getFullName() + "].");
      }

      _param.initTreeMap(vocabInstance);
      _param.applySelectMode(vocabInstance);

      logger.debug("Leaving populateVocabInstance(" + FormatUtil.prettyPrint(values) + ")");
      logger.debug("Returning instance with default value '" + vocabInstance.getDefaultValue() +
          "' out of possible terms: " + FormatUtil.arrayToString(vocabInstance.getTerms().toArray()));
    }
    catch (WdkModelException | WdkUserException e) {
      throw new UnfetchableItemException(e);
    }
  }

  @Override
  public EnumParamVocabInstance updateItem(String key, EnumParamVocabInstance item) {
    throw new UnsupportedOperationException(
        "This should never be called since itemNeedsUpdating() always returns false.");
  }

  @Override
  public boolean itemNeedsUpdating(EnumParamVocabInstance item) {
    return false;
  }

}
