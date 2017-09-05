package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DateRangeParam;
import org.gusdb.wdk.service.formatter.Keys;
import org.json.JSONException;
import org.json.JSONObject;

public class DateRangeParamFormatter extends ParamFormatter<DateRangeParam> {

  DateRangeParamFormatter(DateRangeParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson()
      throws JSONException, WdkModelException, WdkUserException {
    return super.getJson()
        .put(Keys.DEFAULT_VALUE, this._param.getDefault())
        .put(Keys.MIN_DATE, this._param.getMinDate())
        .put(Keys.MAX_DATE, this._param.getMaxDate());
  }
}
