package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamFormatter<T extends Param> {

  protected T _param;

  ParamFormatter(T param) {
    _param = param;
  }

  /**
   * Formats this param into JSON.
   * 
   * @param user current user (may be used to execute vocab queries, etc.)
   * @param stableValues complete stable values from which param values should be retrieved
   * @return This param's data as JSON
   * @throws JSONException if problem generating JSON
   * @throws WdkModelException if system problem occurs
   * @throws WdkUserException if data in param is invalid for some reason
   */
  public JSONObject getJson(User user, CompleteValidStableValues stableValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = new JSONObject();
    pJson.put(Keys.NAME, _param.getName());
    pJson.put(Keys.DISPLAY_NAME, _param.getPrompt());
    pJson.put(Keys.HELP, _param.getHelp());
    pJson.put(Keys.TYPE, _param.getClass().getSimpleName());
    pJson.put(Keys.IS_VISIBLE, _param.isVisible());
    pJson.put(Keys.GROUP, _param.getGroup().getName());
    pJson.put(Keys.IS_READ_ONLY, _param.isReadonly());
    JSONArray dependentParamsJson = new JSONArray();
    for (Param p : _param.getDependentParams()) {
      dependentParamsJson.put(p.getName());
    }
    pJson.put(Keys.DEPENDENT_PARAMS, dependentParamsJson);
    pJson.put(Keys.DEFAULT_VALUE, stableValues.get(_param.getName()));
    return pJson;
  }

}
