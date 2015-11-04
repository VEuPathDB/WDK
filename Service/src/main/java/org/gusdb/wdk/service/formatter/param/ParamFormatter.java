package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.Param;
import org.json.JSONException;
import org.json.JSONObject;

public class ParamFormatter<T extends Param> {

  protected T _param;

  ParamFormatter(T param) {
    _param = param;
  }

  public JSONObject getJson()
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = new JSONObject();
    pJson.put("name", _param.getName());
    pJson.put("displayName", _param.getName());
    pJson.put("prompt", _param.getPrompt());
    pJson.put("help", _param.getHelp());
    pJson.put("defaultValue", getDefault());
    pJson.put("type", _param.getClass().getSimpleName());
    pJson.put("isVisible", _param.isVisible());
    pJson.put("group", _param.getGroup());
    pJson.put("isReadOnly", _param.isReadonly());
    pJson.put("fullName", _param.getFullName());
    pJson.put("id", _param.getId());
    return pJson;
  }
  
  public T getParam() { return _param; }
  
  protected String getDefault() throws WdkModelException {
    return _param.getDefault();
  }

}
