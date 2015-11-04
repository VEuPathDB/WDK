package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public interface VocabProvider {

  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException;

}
