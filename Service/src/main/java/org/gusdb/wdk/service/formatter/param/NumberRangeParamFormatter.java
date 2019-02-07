package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.NumberRangeParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

public class NumberRangeParamFormatter extends ParamFormatter<NumberRangeParam> {

  NumberRangeParamFormatter(NumberRangeParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getBaseJson(spec)
        .put(JsonKeys.MIN_VALUE, _param.getMin())
        .put(JsonKeys.MAX_VALUE, _param.getMax())
        .put(JsonKeys.STEP, _param.getStep());
  }
  
  @Override
  public String getParamType() {
    return JsonKeys.NUMBER_RANGE_PARAM_TYPE;
  }

}
