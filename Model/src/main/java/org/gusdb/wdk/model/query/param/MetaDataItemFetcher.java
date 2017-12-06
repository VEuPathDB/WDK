package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.cache.ItemFetcher;
import org.gusdb.fgputil.cache.UnfetchableItemException;
import org.gusdb.fgputil.json.JsonUtil;
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
  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  public MetaDataItemFetcher(Query metaDataQuery, Map<String, String> paramValues, User user) {
    this.query = metaDataQuery;
    this.paramValues = paramValues;
    this.user = user;
  }

  @Override
  public Map<String, Map<String, String>> fetchItem(String cacheKey) throws UnfetchableItemException {
    try {
      QueryInstance<?> instance = getQueryInstance(user, paramValues, query);
      Map<String, Map<String, String>> properties = new LinkedHashMap<>();
      try (ResultList resultList = instance.getResults()) {
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
      return properties;
    }
    catch (WdkModelException | WdkUserException ex) {
      throw new UnfetchableItemException(ex);
    }
  }
  
  static QueryInstance<?> getQueryInstance(User user, Map<String, String> paramValues, Query query) throws WdkModelException, WdkUserException {
    // trim away param values not needed by query, to avoid warnings
    Map<String, String> requiredParamValues = new HashMap<String, String>();
    for (String paramName : paramValues.keySet())
      if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
        requiredParamValues.put(paramName, paramValues.get(paramName));

    //TODO CWL - Verify that selected method is correct
    ValidatedParamStableValues validatedParamStableValues =
    	    ValidatedParamStableValues.createFromCompleteValues(user, new ParamStableValues(query, requiredParamValues));
    return query.makeInstance(user, validatedParamStableValues, true, 0, new HashMap<String, String>());
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
  public boolean itemNeedsUpdating(Map<String, Map<String, String>> item) {
    return false;
  }

  @Override
  public Map<String, Map<String, String>> updateItem(String key, Map<String, Map<String, String>> item) {
    throw new UnsupportedOperationException(
        "This method should never be called since itemNeedsUpdating() always returns false.");
  }
}
