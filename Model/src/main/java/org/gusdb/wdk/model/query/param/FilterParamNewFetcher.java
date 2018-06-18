package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFetcher implements ValueFactory<String, FilterParamNewInstance> {

  private static final Logger LOG = Logger.getLogger(FlatVocabularyFetcher.class);

  private static final String PROJECT_ID = "project_id";
  private static final String ONTOLOGY_QUERY_REF_KEY = "ontologyQueryRef";
  private static final String METADATA_QUERY_REF_KEY = "metadataQueryRef";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  private final FilterParamNew _param;

  public FilterParamNewFetcher(FilterParamNew param) {
    _param = param;
  }

  public String getCacheKey(Map<String, String> dependedParamValues) throws WdkModelException, JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(PROJECT_ID, _param.getWdkModel().getProjectId());
    cacheKeyJson.put(ONTOLOGY_QUERY_REF_KEY, _param.getOntologyQuery().getFullName());
    cacheKeyJson.put(METADATA_QUERY_REF_KEY, _param.getMetadataQuery().getFullName());
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
  public FilterParamNewInstance getNewValue(String cacheKey) throws ValueProductionException {
    JSONObject cacheKeyJson = new JSONObject(cacheKey);
    LOG.info("Fetching filter param new instance for key: " + cacheKeyJson.toString(2));
    JSONObject dependedParamValuesJson = cacheKeyJson.getJSONObject(DEPENDED_PARAM_VALUES_KEY);
    Map<String, String> dependedParamValues = new HashMap<String, String>();
    for (String paramName : JsonUtil.getKeys(dependedParamValuesJson)) {
      dependedParamValues.put(paramName, dependedParamValuesJson.getString(paramName));
    }
    return new FilterParamNewInstance(_param);
  }

}
