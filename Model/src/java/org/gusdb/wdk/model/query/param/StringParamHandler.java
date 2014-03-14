/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class StringParamHandler extends AbstractParamHandler {

  public StringParamHandler(){}
  
  public StringParamHandler(StringParamHandler handler, Param param) {
    super(handler, param);
  }
  
  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkUserException {
    return (String) rawValue;
  }

  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Object toRawValue(User user, String stableValue, Map<String, String> contextValues) {
    return stableValue;
  }

  /**
   * the signature is a checksum of the stable value.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    return Utilities.encrypt(stableValue);
  }

  /**
   * If number is true, the internal is a string representation of a parsed Double; otherwise, quotes are
   * properly applied; If noTranslation is true, the reference value is used without any change.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextValues) {
    if (param.isNoTranslation())
      return stableValue;

    StringParam stringParam = (StringParam) param;
    if (stringParam.isNumber()) {
      stableValue = stableValue.replaceAll(",", "");
      return stableValue;
    }
    else {
      stableValue = stableValue.replaceAll("'", "''");
      return "'" + stableValue + "'";
    }
  }

  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    String value = requestParams.getParam(param.getName());
    if (value == null) {
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required");
      value = param.getEmptyValue();
    }
    if (value != null)
      value = value.trim();
    return value;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(param.getName());
    if (stableValue == null) {
      stableValue = param.getDefault();
      if (stableValue != null)
        requestParams.setParam(param.getName(), stableValue);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new StringParamHandler(this, param);
  }

}
