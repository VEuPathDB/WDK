package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FlatVocabularyFetcher implements ValueFactory<String, EnumParamVocabInstance> {

  private static final Logger logger = Logger.getLogger(FlatVocabularyFetcher.class);

  private static final String PROJECT_ID = "project_id";
  private static final String VOCAB_QUERY_REF_KEY = "vocabQueryRef";
  private static final String CONTEXT_QUESTION_FULL_NAME_KEY = "contextQuestionFullName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";
  private static final String PARAM_FULL_NAME_KEY = "paramFullName";
  
  private final User _user;
  private final FlatVocabParam _param;
  private final Query _vocabQuery;

  public FlatVocabularyFetcher(User user, FlatVocabParam param) {
    _user = user;
    _param = param;
    _vocabQuery = param.getVocabularyQuery();
    logger.debug("constructor: fetcher created for param: " + _param.getFullName() );
  }

  public String getCacheKey(Map<String, String> dependedParamValues) throws JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    logger.debug("IN FETCHER, getting cache key for:" + _param.getFullName());
    cacheKeyJson.put(PROJECT_ID, _vocabQuery.getWdkModel().getProjectId());
    cacheKeyJson.put(PARAM_FULL_NAME_KEY, _param.getFullName());
    cacheKeyJson.put(CONTEXT_QUESTION_FULL_NAME_KEY, _param.getContextQuestion() == null ? null : _param.getContextQuestion().getFullName());
    cacheKeyJson.put(VOCAB_QUERY_REF_KEY, _vocabQuery.getFullName());
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY,
        AbstractDependentParam.getDependedParamValuesJson(dependedParamValues, _param.getDependedParams()));
    return JsonUtil.serialize(cacheKeyJson);
  }

  /**
   * We don't need to read the vocabQueryRef from the cache key, because we know
   * it is the same as the one in the param's state.
   * 
   * @throws ValueProductionException if unable to fetch item
   */
  @Override
  public EnumParamVocabInstance getNewValue(String cacheKey) throws ValueProductionException {
    JSONObject cacheKeyJson = new JSONObject(cacheKey);
    logger.debug("IN FETCHER: getNewValue: ");
    logger.debug("getNewValue: Fetching vocab instance for cache key: " + cacheKeyJson.toString(2));
    JSONObject dependedParamValuesJson = cacheKeyJson.getJSONObject(DEPENDED_PARAM_VALUES_KEY);
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    for (String paramName : JsonUtil.getKeys(dependedParamValuesJson)) {
      dependedParamValues.put(paramName, dependedParamValuesJson.getString(paramName));
    }
    logger.debug("getNewValue: fetch vocabInstance in fetchItem()");
    return fetchItem(dependedParamValues);
  }

  public EnumParamVocabInstance fetchItem(Map<String, String> dependedParamValues) throws ValueProductionException {
    // create and populate vocab instance
    logger.debug("fetchItem(): (when new or not cacheable)");
    EnumParamVocabInstance vocabInstance = new EnumParamVocabInstance(dependedParamValues);
    populateVocabInstance(vocabInstance);
    return vocabInstance;
  }

  private void populateVocabInstance(EnumParamVocabInstance vocabInstance) throws ValueProductionException {
    try {
      logger.debug("IN populateVocabInstance()");
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
          ParameterContainer contextQuery = param.getContainer();
          param = param.clone();
          _vocabQuery.addParam(param);
          param.setContainer(contextQuery);
          String value = vocabInstance.getDependedValues().get(param.getName());
          values.put(param.getName(), value);
        }
      }

      //logger.debug("IN populateVocabInstance(): get param context question: " +   _param.getContextQuestion() );
      Question contextQuestion = _param.getContextQuestion();
      ParameterContainer contextQuery = _param.getContainer();
      Map<String, String> context = new LinkedHashMap<String, String>();
      context.put(Utilities.CONTEXT_KEY_PARAM_NAME, _param.getFullName());
      if (_param.getContextQuestion() != null)
        context.put(Utilities.CONTEXT_KEY_QUESTION_FULL_NAME, contextQuestion.getFullName());
      logger.debug("PARAM [" + _param.getFullName() + "] query=" + _vocabQuery.getFullName() +
          ", context Question: " + ((contextQuestion == null) ? "N/A" : contextQuestion.getFullName()) +
          ", context Query: " + ((contextQuery == null) ? "N/A" : contextQuery.getFullName()));

      // FIXME: Do we need to send context info above or is the way we are extracting it sufficient??
      QueryInstance<?> instance = Query.makeQueryInstance(QueryInstanceSpec.builder()
          .putAll(values).buildRunnable(_user, _vocabQuery, StepContainer.emptyContainer()));
      try (ResultList result = instance.getResults()) {
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
          String display = term; // use term for display if no display column
          if (hasDisplay) {
            Object displayObj = result.get(FlatVocabParam.COLUMN_DISPLAY);
            if (displayObj == null) {
              throw new WdkModelException("Vocabulary query '" + _vocabQuery.getFullName() + "' has a '" +
                  FlatVocabParam.COLUMN_DISPLAY + "' column but its value is null in at least one row.");
            }
            display = displayObj.toString().trim();
          }
          String parentTerm = null;
          if (hasParent) {
            Object parent = result.get(FlatVocabParam.COLUMN_PARENT_TERM);
            if (parent != null)
              parentTerm = parent.toString().trim();
          }

          /* 11/22/19 No longer disallow commas in terms- converting enum param stable values to JSON to support
          if (term.indexOf(',') >= 0 && dependedParams != null)
            throw new WdkModelException(_param.getFullName() + ":" +
                "The term cannot contain comma: '" + term + "'");

          if (parentTerm != null && parentTerm.indexOf(',') >= 0)
            throw new WdkModelException(_param.getFullName() +
                ": The parent term cannot contain " + "comma: '" + parentTerm + "'");
          */

          vocabInstance.addTermValues(term, value, display, parentTerm);
        }
      }

      boolean emptyVocabAllowed = _param.isAllowEmptyVocabulary() && _param.getDependentParams().isEmpty();
      if (vocabInstance.isEmpty() && !emptyVocabAllowed) {
        if (instance instanceof SqlQueryInstance)
          logger.warn("vocab query returned 0 rows:" + ((SqlQueryInstance)instance).getSql());
        throw new WdkEmptyEnumListException("No item returned by the query [" + _vocabQuery.getFullName() +
            "] of FlatVocabParam [" + _param.getFullName() + "].");
      }
      else {
        logger.debug("FlatVocab Query [" + _vocabQuery.getFullName() + "] returned " + vocabInstance.getNumTerms() +
            " of FlatVocabParam [" + _param.getFullName() + "].");
      }

      _param.initTreeMap(vocabInstance);


      logger.debug("Leaving populateVocabInstance: " + FormatUtil.prettyPrint(values) + ")");
      logger.debug("Returning instance with possible terms: " +
          FormatUtil.arrayToString(vocabInstance.getTerms().toArray()));
    }
    catch (WdkModelException e) {
      throw new ValueProductionException(e);
    }
  }
}
