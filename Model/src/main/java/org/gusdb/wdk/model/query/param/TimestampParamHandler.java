package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
    return ctxVals.getObject().get(_param.getName());
  }

  /**
   * The stable value is the same as signature.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    return ctxVals.getObject().get(_param.getName());
  }

  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException {
    return validateStableValueSyntax(user, requestParams.getParam(_param.getName()));
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException {
    String stableValue = inputStableValue;
    if (inputStableValue == null) {
      if (!_param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required");
      stableValue = _param.getEmptyValue();
    }
    return stableValue;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues) {
    String stableValue = requestParams.getParam(_param.getName());
    if (stableValue == null) {
      stableValue = _param.getXmlDefault();
      if (stableValue != null)
        requestParams.setParam(_param.getName(), stableValue);
    }
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
