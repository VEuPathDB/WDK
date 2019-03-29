package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.DateParam;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONObject;

public class DateParamFormatter extends ParamFormatter<DateParam> {

  DateParamFormatter(DateParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return getBaseJson(spec)
        .put(JsonKeys.MIN_DATE, _param.getMinDate())
        .put(JsonKeys.MAX_DATE, _param.getMaxDate());
  }
  
  @Override
  public String getParamType() {
    return JsonKeys.DATE_PARAM_TYPE;
  }

}
