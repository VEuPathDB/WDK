package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
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
  public JSONObject getJson(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getBaseJson(spec)
        .put(JsonKeys.MIN_DATE, _param.getMinDate())
        .put(JsonKeys.MAX_DATE, _param.getMaxDate());
  }

  @Override
  protected String getParamType() {
    return "DateRangeParam";
  }
}
