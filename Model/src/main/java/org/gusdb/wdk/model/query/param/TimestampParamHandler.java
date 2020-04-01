package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
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
   * Returns the number of full intervals that have occurred since midnight on
   * January 1, 1970 UTC.  By doing this, the signature changes over time so
   * that cache lookups expire each time an interval passes.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    long interval = ((TimestampParam)_param).getInterval();
    return Long.toString(System.currentTimeMillis() / (1000 * interval));
  }

  @Override
  public ParamHandler clone(Param param) {
    return new TimestampParamHandler(this, param);
  }
}
