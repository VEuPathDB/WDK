package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaDataItemNewFetcher implements ValueFactory<String, Map<String, MetaDataItem>> {

  private Query query;
  private Map<String, String> paramValues;
  private User user;
  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  public MetaDataItemNewFetcher(Query metaDataQuery, Map<String, String> paramValues, User user) {
    this.query = metaDataQuery;
    this.paramValues = paramValues;
    this.user = user;
  }

  @Override
  public Map<String, MetaDataItem> getNewValue(String cacheKey) throws ValueProductionException {
    try {
      // trim away param values not needed by query, to avoid warnings
      Map<String, String> requiredParamValues = new HashMap<String, String>();
      for (String paramName : paramValues.keySet())
        if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
          requiredParamValues.put(paramName, paramValues.get(paramName));

      QueryInstance<?> instance = Query.makeQueryInstance(user, QueryInstanceSpec.builder()
          .putAll(requiredParamValues).buildRunnable(query, null));
      Map<String, MetaDataItem> itemMap = new LinkedHashMap<>();
      ResultList resultList = instance.getResults();
      try {
        while (resultList.next()) {
          MetaDataItem mdItem = new MetaDataItem();
          mdItem.setOntologyId((String) resultList.get(FilterParamNew.COLUMN_ONTOLOGY_ID));
          mdItem.setInternal((String) resultList.get(FilterParamNew.COLUMN_INTERNAL));
          mdItem.setStringValue((String) resultList.get(OntologyItemType.STRING.getMetadataQueryColumn()));
          mdItem.setNumberValue((String) resultList.get(OntologyItemType.NUMBER.getMetadataQueryColumn()));
          mdItem.setDateValue((String) resultList.get(OntologyItemType.DATE.getMetadataQueryColumn()));

          itemMap.put(mdItem.getOntologyId(), mdItem);

        }
      }
      finally {
        resultList.close();
      }
      return itemMap;
    }
    catch (WdkModelException ex) {
      throw new ValueProductionException(ex);
    }
  }

  public String getCacheKey() throws JSONException {
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(QUERY_NAME_KEY, query.getName());
    JSONObject paramValuesJson = new JSONObject();
    for (String paramName : paramValues.keySet())
      if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
        paramValuesJson.put(paramName, paramValues.get(paramName));
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY, paramValuesJson);
    return JsonUtil.serialize(cacheKeyJson);
  }

  @Override
  public boolean valueNeedsUpdating(Map<String, MetaDataItem> item) {
    return false;
  }

  @Override
  public Map<String, MetaDataItem> getUpdatedValue(String key, Map<String, MetaDataItem> item) {
    throw new UnsupportedOperationException(
        "This method should never be called since itemNeedsUpdating() always returns false.");
  }
}
