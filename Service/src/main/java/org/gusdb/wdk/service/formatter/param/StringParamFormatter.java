package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

public class StringParamFormatter extends ParamFormatter<StringParam> {

  StringParamFormatter(StringParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson(DisplayablyValid<QueryInstanceSpec> spec) throws WdkModelException {
    return getBaseJson(spec).put(JsonKeys.LENGTH, _param.getLength());
  }

  @Override
  public String getParamType() {
    return JsonKeys.STRING_PARAM_TYPE;
  }

}
