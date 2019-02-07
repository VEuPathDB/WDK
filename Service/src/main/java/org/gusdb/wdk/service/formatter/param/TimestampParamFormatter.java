package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.query.param.TimestampParam;

public class TimestampParamFormatter extends ParamFormatter<TimestampParam> {

  TimestampParamFormatter(TimestampParam param) {
    super(param);
  }

  @Override
  protected String getParamType() {
    return JsonKeys.TIMESTAMP_PARAM_TYPE;
  }

 
}
