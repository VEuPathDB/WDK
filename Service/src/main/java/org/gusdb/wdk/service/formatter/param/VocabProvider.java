package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONException;
import org.json.JSONObject;

public interface VocabProvider {

  public JSONObject getJson(Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException;

}
