package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.NumberParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

public class NumberParamFormatter extends ParamFormatter<NumberParam> {

  NumberParamFormatter(NumberParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getBaseJson(spec)
        .put(JsonKeys.MIN_VALUE, this._param.getMin())
        .put(JsonKeys.MAX_VALUE, this._param.getMax())
        .put(JsonKeys.STEP, this._param.getStep());
  }

  @Override
  protected String getParamType() {
    return "NumberParam";
  }
}
