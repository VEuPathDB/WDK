package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.DateRangeParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

public class DateRangeParamFormatter extends ParamFormatter<DateRangeParam> {

  DateRangeParamFormatter(DateRangeParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(DisplayablyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getBaseJson(spec)
        .put(JsonKeys.MIN_DATE, _param.getMinDate())
        .put(JsonKeys.MAX_DATE, _param.getMaxDate());
  }
  
  @Override
  public String getParamType() {
    return JsonKeys.DATE_RANGE_PARAM_TYPE;
  }
}
