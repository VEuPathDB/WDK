package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class TimestampParamHandler extends AbstractParamHandler {

  public TimestampParamHandler() {}

  public TimestampParamHandler(TimestampParamHandler handler, Param param) {
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
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) {
    return ctxVals.get().get(_param.getName());
  }

  @Override
  public String toEmptyInternalValue() {
    return "?";
  }

  /**
   * The stable value is the same as signature.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    return ctxVals.get().get(_param.getName());
  }

  @Override
  public ParamHandler clone(Param param) {
    return new TimestampParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals) throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName()));
  }
}
