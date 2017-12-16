package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.cache.NoUpdateItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaDataItemNewFetcher extends NoUpdateItemFetcher<String, Map<String, MetaDataItem>> {

  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  private final CompleteValidStableValues _paramValues;
  private final User _user;

  public MetaDataItemNewFetcher(CompleteValidStableValues paramValues, User user) {
    _paramValues = paramValues;
    _user = user;
  }

  @Override
  public Map<String, MetaDataItem> fetchItem(String cacheKey) throws UnfetchableItemException {
    try {
      QueryInstance<?> instance = _paramValues.getQuery().makeInstance(_user, _paramValues);
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
    catch (WdkModelException | WdkUserException ex) {
      throw new UnfetchableItemException(ex);
    }
  }

  public String getCacheKey() throws JSONException {
    Query query = _paramValues.getQuery();
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(QUERY_NAME_KEY, query.getName());
    JSONObject paramValuesJson = new JSONObject();
    for (String paramName : _paramValues.keySet())
      if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
        paramValuesJson.put(paramName, _paramValues.get(paramName));
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY, paramValuesJson);
    return JsonUtil.serialize(cacheKeyJson);
  }
}
