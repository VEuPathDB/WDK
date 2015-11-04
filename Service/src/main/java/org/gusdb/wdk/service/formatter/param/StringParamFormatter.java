package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.json.JSONException;
import org.json.JSONObject;

public class StringParamFormatter extends ParamFormatter<StringParam> {

  StringParamFormatter(StringParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson() throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = super.getJson();
    pJson.put("length", _param.getLength());
    return pJson;
  }
}
