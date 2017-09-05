package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.NumberRangeParam;
import org.gusdb.wdk.service.formatter.Keys;
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
        .put(Keys.DEFAULT_VALUE, this._param.getDefault())
        .put(Keys.MIN_VALUE, this._param.getMin())
        .put(Keys.MAX_VALUE, this._param.getMax())
        .put(Keys.STEP, this._param.getStep());
  }
}
