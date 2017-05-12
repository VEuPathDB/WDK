package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;


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
import org.apache.log4j.Logger;

public class OntologyItemNewFetcher implements ItemFetcher<String, Map<String, OntologyItem>> {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(OntologyItemNewFetcher.class);

  private Query query;
  private Map<String, String> paramValues;
  private User user;
  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  public OntologyItemNewFetcher(Query ontologyQuery, Map<String, String> paramValues, User user) {
    this.query = ontologyQuery;
    this.paramValues = paramValues;
    this.user = user;
  }

  @Override
  public Map<String, OntologyItem> fetchItem(String cacheKey) throws UnfetchableItemException {
    try {
      // trim away param values not needed by query, to avoid warnings
      Map<String, String> requiredParamValues = new HashMap<String, String>();
      for (String paramName : paramValues.keySet())
        if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
          requiredParamValues.put(paramName, paramValues.get(paramName));

      QueryInstance<?> instance = query.makeInstance(user, requiredParamValues, true, 0,
          new HashMap<String, String>());
      Map<String, OntologyItem> ontologyItemMap = new LinkedHashMap<>();
      ResultList resultList = instance.getResults();
      try {
        while (resultList.next()) {
          OntologyItem oItem = new OntologyItem();
          oItem.setOntologyId((String) resultList.get(FilterParamNew.COLUMN_ONTOLOGY_ID));
          oItem.setParentOntologyId((String) resultList.get(FilterParamNew.COLUMN_PARENT_ONTOLOGY_ID));
          oItem.setDisplayName((String) resultList.get(FilterParamNew.COLUMN_DISPLAY_NAME));
          oItem.setDescription((String) resultList.get(FilterParamNew.COLUMN_DESCRIPTION));
          oItem.setType((String) resultList.get(FilterParamNew.COLUMN_TYPE));
          oItem.setUnits((String) resultList.get(FilterParamNew.COLUMN_UNITS));

	  BigDecimal precision = (BigDecimal)resultList.get(FilterParamNew.COLUMN_PRECISION);

          if (precision != null) oItem.setPrecision(precision.toBigInteger().longValue() );
	  BigDecimal isRange = (BigDecimal)resultList.get(FilterParamNew.COLUMN_IS_RANGE);
          if (isRange != null) oItem.setIsRange(isRange.toBigInteger().intValue() != 0);

          ontologyItemMap.put(oItem.getOntologyId(), oItem);
        }
      }
      finally {
        resultList.close();
      }
      return ontologyItemMap;
    }
    catch (WdkModelException | WdkUserException ex) {
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
    return cacheKeyJson.toString();
  }

  @Override
  public boolean itemNeedsUpdating(Map<String, OntologyItem> item) {
    return false;
  }

  @Override
  public Map<String, OntologyItem> updateItem(String key, Map<String, OntologyItem> item) {
    throw new UnsupportedOperationException(
        "This method should never be called since itemNeedsUpdating() always returns false.");
  }
  
}
