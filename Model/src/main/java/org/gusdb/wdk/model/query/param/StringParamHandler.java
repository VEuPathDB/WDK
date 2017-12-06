/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.fgputil.EncryptionUtil;
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
  public String toStableValue(User user, Object rawValue)
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
  public String toRawValue(User user, String stableValue, ValidatedParamStableValues contextParamValues) {
    return stableValue;
  }

  /**
   * the signature is a checksum of the stable value.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, Map)
   */
  @Override
  public String toSignature(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0) return "";
    return EncryptionUtil.encrypt(stableValue);
  }

  /**
   * If number is true, the internal is a string representation of a parsed Double; otherwise, quotes are
   * properly applied; If noTranslation is true, the reference value is used without any change.
   *
   * @throws WdkModelException
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException {
    if (_param.isNoTranslation())
      return stableValue;

    StringParam stringParam = (StringParam) _param;

    if (stringParam.isNumber()) {
      stableValue = stableValue.replaceAll(",", "");
      return stableValue;
    }
    else if (stringParam.getIsSql()) {
	return stableValue;
    } else {
      stableValue = stableValue.replaceAll("'", "''");
      return "'" + stableValue + "'";
    }
  }

  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    return validateStableValueSyntax(user, requestParams.getParam(_param.getName()));
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException, WdkModelException {
    String stableValue = inputStableValue;
    if (stableValue == null) {
      if (!_param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required");
      stableValue = _param.getEmptyValue();
    }
    if (stableValue != null)
      stableValue = stableValue.trim();
    return stableValue;
  }
  
  @Override
  public void prepareDisplay(User user, RequestParams requestParams, ValidatedParamStableValues contextParamValues)
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
    return new StringParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException {
    return toRawValue(user, stableValue, contextParamValues);
  }

}
