package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.NumberRangeParam;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.json.JSONException;
import org.json.JSONObject;

public class NumberRangeParamFormatter extends ParamFormatter<NumberRangeParam> {

  NumberRangeParamFormatter(NumberRangeParam param) {
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
