package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.ValidStableValues;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONException;
import org.json.JSONObject;

public class StringParamFormatter extends ParamFormatter<StringParam> {

  StringParamFormatter(StringParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(User user, ValidStableValues stableValues)
      throws JSONException, WdkModelException, WdkUserException {
    return super.getJson(user, stableValues)
        .put(Keys.LENGTH, _param.getLength());
  }
}
