package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFetcher implements ItemFetcher<String, FilterParamNewInstance> {

  private static final Logger logger = Logger.getLogger(FlatVocabularyFetcher.class);

  private static final String PROJECT_ID = "project_id";
  private static final String ONTOLOGY_QUERY_REF_KEY = "ontologyQueryRef";
  private static final String METADATA_QUERY_REF_KEY = "metadataQueryRef";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";
  
  private final User _user;
  private final FilterParamNew _param;
  private final Query _metadataQuery;
  private final Query _ontologyQuery;

  public FilterParamNewFetcher(User user, FilterParamNew param) {
    _user = user;
    _param = param;
    _metadataQuery = param.getMetadataQuery();
    _ontologyQuery = param.getOntologyQuery();
  }

  public String getCacheKey(Map<String, String> dependedParamValues) throws WdkModelException, JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(PROJECT_ID, _ontologyQuery.getWdkModel().getProjectId());
    cacheKeyJson.put(ONTOLOGY_QUERY_REF_KEY, _ontologyQuery.getFullName());
    cacheKeyJson.put(METADATA_QUERY_REF_KEY, _metadataQuery.getFullName());
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY,
        AbstractDependentParam.getDependedParamValuesJson(dependedParamValues, _param.getDependedParams()));
    return cacheKeyJson.toString();
  }

  /**
   * We don't need to read the vocabQueryRef from the cache key, because we know
   * it is the same as the one in the param's state.
   * 
   * @throws UnfetchableItemException if unable to fetch item
   */
  @Override
  public FilterParamNewInstance fetchItem(String cacheKey) throws UnfetchableItemException {

    JSONObject cacheKeyJson = new JSONObject(cacheKey);
    logger.info("Fetching vocab instance for key: " + cacheKeyJson.toString(2));
    JSONObject dependedParamValuesJson = cacheKeyJson.getJSONObject(DEPENDED_PARAM_VALUES_KEY);
    Iterator<String> paramNames = dependedParamValuesJson.keys();
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    while (paramNames.hasNext()) {
      String paramName = paramNames.next();
      dependedParamValues.put(paramName, dependedParamValuesJson.getString(paramName));
    }
    return fetchItem(dependedParamValues);
  }

  public FilterParamNewInstance fetchItem(Map<String, String> dependedParamValues) throws UnfetchableItemException {
    // create and populate vocab instance
    FilterParamNewInstance vocabInstance = new FilterParamNewInstance(dependedParamValues, _param);
    populateVocabInstance(vocabInstance);
    return vocabInstance;
  }

  private void populateVocabInstance(FilterParamNewInstance vocabInstance) throws UnfetchableItemException {
    // TODO phase 2 - implement.  must produce stringValuesMap
   }

  @Override
  public FilterParamNewInstance updateItem(String key, FilterParamNewInstance item) {
    throw new UnsupportedOperationException(
        "This should never be called since itemNeedsUpdating() always returns false.");
  }

  @Override
  public boolean itemNeedsUpdating(FilterParamNewInstance item) {
    return false;
  }

}
