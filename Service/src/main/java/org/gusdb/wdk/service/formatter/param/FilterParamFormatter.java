package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamFormatter extends AbstractEnumParamFormatter implements VocabProvider  {

  protected FilterParam filterParam; 
  
  FilterParamFormatter(FilterParam param) {
    super(param);
    this.filterParam = param;
  }
  
  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = super.getJson();
    pJson.put("vocab", getVocabJson(user, dependedParamValues));
    pJson.put("metaData", getMetaDataJson(user, dependedParamValues));
    pJson.put("metaDataSpec", getMetaDataSpecJson(user, dependedParamValues));
    return pJson;
  }
  
  public JSONObject getMetaDataJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    Map<String, Map<String, String>> metaDataMap = filterParam.getMetadata(user, dependedParamValues);
    JSONObject metaDataJson = new JSONObject();
    for (String term : metaDataMap.keySet()) {
      JSONObject termJson = new JSONObject();
      for (String prop : metaDataMap.get(term).keySet()) {
        JSONObject propJson = new JSONObject();
        propJson.put(prop, metaDataMap.get(term).get(prop));
      }
      metaDataJson.put(term, termJson);
    }
    return metaDataJson; 
  }

  public JSONObject getMetaDataSpecJson(User user, Map<String, String> dependedParamValues) throws JSONException, WdkModelException, WdkUserException {
    Map<String, Map<String, String>> metaDataSpecMap = filterParam.getMetadataSpec(user, dependedParamValues);
    JSONObject metaDataSpecJson = new JSONObject();
    for (String prop : metaDataSpecMap.keySet()) {
      JSONObject propJson = new JSONObject();
      for (String specProp : metaDataSpecMap.get(prop).keySet()) {
        JSONObject specPropJson = new JSONObject();
        specPropJson.put(specProp, metaDataSpecMap.get(prop).get(specProp));
      }
      metaDataSpecJson.put(prop, propJson);
    }
    return metaDataSpecJson; 
  }
}
