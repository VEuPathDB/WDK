package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

public class DateRangeParamHandler extends AbstractParamHandler {

  public DateRangeParamHandler() {}

  public DateRangeParamHandler(DateRangeParamHandler handler, Param param) {
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
   * The raw value is the same as stable value.
   */
  @Override
  public String toRawValue(User user, String stableValue) {
    return stableValue;
  }

  /**
   * The internal value is the same as stable value.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    return ctxParamVals.getObject().get(_param.getName());
  }

  /**
   * The stable value is the same as signature.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    return ctxParamVals.getObject().get(_param.getName());
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DateRangeParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxParamVals)
      throws WdkModelException {

    return toRawValue(ctxParamVals.getUser(), ctxParamVals.get(_param.getName()))
        .replace("{", "")
        .replace("}", "")
        .replace("\"", "");
  }
}
