/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;


public class DateParamHandler extends AbstractParamHandler {

  public DateParamHandler() {}

  public DateParamHandler(DateParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * The raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextParamValues) {
    return (String) rawValue;
  }

  /**
   * The raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String stableValue, Map<String, String> contextParamValues) {
    return stableValue;
  }

  /**
   * The internal value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextParamValues) {
    return "date '" + stableValue + "'";
  }

  /**
   * The stable value is the same as signature.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextParamValues) {
    return stableValue;
  }

  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    return validateStableValueSyntax(user, requestParams.getParam(_param.getName()));
  }
  
  @Override 
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException,
  WdkModelException {
    String stableValue = inputStableValue;
    if (inputStableValue == null) {
      if (!_param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required");
      stableValue = _param.getEmptyValue();
    }
    return stableValue;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(_param.getName());
    if (stableValue == null) {
      stableValue = _param.getDefault();
      if (stableValue != null)
        requestParams.setParam(_param.getName(), stableValue);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DateParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    return toRawValue(user, stableValue, contextParamValues);
  }

}
