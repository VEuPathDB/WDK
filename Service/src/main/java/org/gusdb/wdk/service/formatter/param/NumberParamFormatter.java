package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.NumberParam;
import org.json.JSONException;
import org.json.JSONObject;

public class NumberParamFormatter extends ParamFormatter<NumberParam> {

  NumberParamFormatter(NumberParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson()
      throws JSONException, WdkModelException, WdkUserException {
    return super.getJson()
        .put(JsonKeys.DEFAULT_VALUE, this._param.getDefault())
        .put(JsonKeys.MIN_VALUE, this._param.getMin())
        .put(JsonKeys.MAX_VALUE, this._param.getMax())
        .put(JsonKeys.STEP, this._param.getStep());
  }
}
