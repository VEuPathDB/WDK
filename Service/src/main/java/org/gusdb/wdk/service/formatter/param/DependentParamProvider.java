package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.ValidatedParamStableValues;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public interface DependentParamProvider {

  public JSONObject getJson(User user, ValidatedParamStableValues dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException;

}
