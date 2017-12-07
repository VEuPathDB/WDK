package org.gusdb.wdk.model.query.param;

import java.util.HashMap;
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
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class OntologyItemFetcher extends NoUpdateItemFetcher<String, Map<String, Map<String, String>>> {

  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  private Query query;
  //TODO - CWL Verify
  private ValidatedParamStableValues paramValues;
  private User user;

  //TODO - CWL Verify
  public OntologyItemFetcher(Query metaDataQuery, ValidatedParamStableValues paramValues, User user) {
    this.query = metaDataQuery;
    this.paramValues = paramValues;
    this.user = user;
  }

  //TODO - CWL Verify
  @Override
  public Map<String, Map<String, String>> fetchItem(String cacheKey) throws UnfetchableItemException {
    try {
      // trim away param values not needed by query, to avoid warnings
      Map<String, String> requiredParamValues = new HashMap<String, String>();
      for (String paramName : paramValues.keySet())
        if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
          requiredParamValues.put(paramName, paramValues.get(paramName));

      //TODO CWL - Verify
      ValidatedParamStableValues validatedParamStableValues =
    	      ValidatedParamStableValues.createFromCompleteValues(user, new ParamStableValues(query, requiredParamValues));
      QueryInstance<?> instance = query.makeInstance(user, validatedParamStableValues, true, 0,
          new HashMap<String, String>());
      Map<String, Map<String, String>> metadata = new LinkedHashMap<>();
      ResultList resultList = instance.getResults();
      try {
        while (resultList.next()) {
          String property = (String) resultList.get(FilterParam.COLUMN_PROPERTY);
          String info = (String) resultList.get(FilterParam.COLUMN_SPEC_PROPERTY);
          String data = (String) resultList.get(FilterParam.COLUMN_SPEC_VALUE);
          Map<String, String> propertyMeta = metadata.get(property);
          if (propertyMeta == null) {
            propertyMeta = new LinkedHashMap<>();
            metadata.put(property, propertyMeta);
          }
          propertyMeta.put(info, data);
        }
      }
      finally {
        resultList.close();
      }
      return metadata;
    } catch (WdkUserException ex) {
      throw new UnfetchableItemException(new WdkModelException(ex));
    }
    catch (WdkModelException  ex) {
      throw new UnfetchableItemException(ex);
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
}
