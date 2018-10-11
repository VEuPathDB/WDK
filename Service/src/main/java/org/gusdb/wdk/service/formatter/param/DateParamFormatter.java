package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DateParam;
import org.json.JSONException;
import org.json.JSONObject;

public class DateParamFormatter extends ParamFormatter<DateParam> {

  DateParamFormatter(DateParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson()
      throws JSONException, WdkModelException, WdkUserException {
    return super.getJson()
        .put(JsonKeys.DEFAULT_VALUE, this._param.getDefault())
        .put(JsonKeys.MIN_DATE, this._param.getMinDate())
        .put(JsonKeys.MAX_DATE, this._param.getMaxDate());
  }
}
