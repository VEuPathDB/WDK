package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.StringParam;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONObject;

public class StringParamFormatter extends ParamFormatter<StringParam> {

  StringParamFormatter(StringParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return getBaseJson(spec)
      .put(JsonKeys.LENGTH, _param.getLength())
      .put(JsonKeys.IS_MULTILINE, _param.getMultiLine())
      .put(JsonKeys.IS_NUMBER, _param.isNumber());
  }

  @Override
  public String getParamType() {
    return JsonKeys.STRING_PARAM_TYPE;
  }

}
