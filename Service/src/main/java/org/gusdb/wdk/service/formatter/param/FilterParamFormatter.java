package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamFormatter extends AbstractEnumParamFormatter implements VocabProvider  {

  FilterParamFormatter(FilterParam param) {
    super(param);
  }
  
  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException {
    JSONObject pJson = super.getJson();
    pJson.put("vocab", getVocabJson(user, dependedParamValues));
    return pJson;
  }

}
