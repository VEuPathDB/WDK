package org.gusdb.wdk.service.formatter.param;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.query.param.Param;

/**
 * a formatter for other boring simple params
 * @author Steve
 *
 */
public class SimpleParamFormatter extends ParamFormatter<Param> {

  public SimpleParamFormatter(Param param) {  super(param); }
  
  @Override
  public String getTypeDisplayName() {
    return JsonKeys.OTHER_PARAM_TYPE;
  }

}
