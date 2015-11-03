package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaDataItemFetcher implements ItemFetcher<String, Map<String, Map<String, String>>> {
  
  private Query query;
  private Map<String, String> paramValues;
  private User user;
  
  public MetaDataItemFetcher(Query metaDataQuery, Map<String, String> paramValues, User user) {
    this.query = metaDataQuery;
    this.paramValues = paramValues;
    this.user = user;
  }
  
  public Map<String, Map<String, String>> fetchItem(String cacheKey) throws UnfetchableItemException {

    try {
    QueryInstance<?> instance = query.makeInstance(user, paramValues, true, 0, paramValues);
    Map<String, Map<String, String>> properties = new LinkedHashMap<>();
    ResultList resultList = instance.getResults();
    try {
      while (resultList.next()) {
        String term = (String) resultList.get(FilterParam.COLUMN_TERM);
        String property = (String) resultList.get(FilterParam.COLUMN_PROPERTY);
        String value = (String) resultList.get(FilterParam.COLUMN_VALUE);
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
    } catch (WdkModelException | WdkUserException ex) {
      throw new UnfetchableItemException(ex);
    }
  }
  
  public Map<String, Map<String, String>> updateItem(String key, Map<String, Map<String, String>> item) {
    return null;
  }

  public String getCacheKey() throws WdkModelException, JSONException {
   JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put("queryName", query.getName());
    JSONObject paramValuesJson = new JSONObject();
    for (String paramName : paramValues.keySet()) 
      if (query.getParamMap() != null && query.getParamMap().containsKey(paramName)) paramValuesJson.put(paramName, paramValues.get(paramName));
    cacheKeyJson.put("paramValues", paramValuesJson);
    return cacheKeyJson.toString();
  }
  
  public boolean itemNeedsUpdating(Map<String, Map<String, String>> item) {
    return false;
   }
   

}
