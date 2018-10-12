package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
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
   * @return This param's data as JSON
   * @throws JSONException if problem generating JSON
   * @throws WdkModelException if system problem occurs
   * @throws WdkUserException if data in param is invalid for some reason
   */
  public JSONObject getJson()
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = new JSONObject();
    pJson.put(JsonKeys.NAME, _param.getName());
    pJson.put(JsonKeys.DISPLAY_NAME, _param.getPrompt());
    pJson.put(JsonKeys.HELP, _param.getHelp());
    pJson.put(JsonKeys.TYPE, _param.getClass().getSimpleName());
    pJson.put(JsonKeys.IS_VISIBLE, _param.isVisible());
    pJson.put(JsonKeys.GROUP, _param.getGroup().getName());
    pJson.put(JsonKeys.IS_READ_ONLY, _param.isReadonly());
    JSONArray dependentParamsJson = new JSONArray();
    for (Param p : _param.getDependentParams()) dependentParamsJson.put(p.getName());
    pJson.put(JsonKeys.DEPENDENT_PARAMS, dependentParamsJson);
    pJson.put(JsonKeys.DEFAULT_VALUE, getDefault());
    return pJson;
  }

  /**
   * Returns a context-free default value.  Subclasses that override the
   * default value in JSON should override this method and return null since
   * determining the default value can be expensive.
   * 
   * @return default value
   * @throws WdkModelException
   */
  protected String getDefault() throws WdkModelException {
    return _param.getDefault();
  }

}
