package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

public class DateParamHandler extends AbstractParamHandler {

  public DateParamHandler() {}

  public DateParamHandler(DateParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * The raw value is the same as stable value.
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    return (String) rawValue;
  }

  /**
   * The internal value is the same as stable value.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    return "date '" + ctxParamVals.get().get(_param.getName()) + "'";
  }

  @Override
  public String toEmptyInternalValue() {
    return "?";
  }

  /**
   * The stable value is the same as signature.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    return ctxParamVals.get().get(_param.getName());
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DateParamHandler(this, param);
  }
}
