package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.query.param.AnswerParam;

public class AnswerParamFormatter extends ParamFormatter<AnswerParam>{

  AnswerParamFormatter(AnswerParam param) {
    super(param);
  }

  @Override
  public String getParamType() {
    return JsonKeys.STEP_PARAM_TYPE;
  }

}
