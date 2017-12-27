package org.gusdb.wdk.model.query.param;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.user.User;


public class NumberParamHandler extends AbstractParamHandler {

  public NumberParamHandler(){}
  
  public NumberParamHandler(NumberParamHandler handler, Param param) {
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
  public String toRawValue(User user, String stableValue) {
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
  public String toSignature(User user, CompleteValidStableValues contextParamValues)
      throws WdkModelException {
    String stableValue = contextParamValues.get(_param.getName());
    if (stableValue == null || stableValue.length() == 0) return "";
    return EncryptionUtil.encrypt(stableValue);
  }

  /**
   * Formats the stableValue into a value that can be applied to SQL statements.
   * @throws WdkModelException
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, CompleteValidStableValues contextParamValues)
      throws WdkModelException {
    String stableValue = contextParamValues.get(_param.getName());

    // Something to do with the portal - left this alone 
    if(_param.isNoTranslation()) {
      return stableValue;
    }

    // Set internalValue to stableValue and determine whether additional modifications are needed.
    String internalValue = stableValue;

    // If the number is in exponential form, change to decimal form
    if(stableValue.matches("^.*(e|E).*$")) {
      internalValue = new BigDecimal(stableValue).toPlainString();
    }

    // If the number is not an integer, round it according to the number of decimal places
    // requested (or the default value).
    if(!((NumberParam)_param).isInteger()) {
      MathContext mathContext = new MathContext(((NumberParam)_param).getNumDecimalPlaces(), RoundingMode.HALF_UP);
      internalValue = (new BigDecimal(internalValue, mathContext)).toPlainString();
    }  
    return internalValue;
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
  public void prepareDisplay(User user, RequestParams requestParams)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(_param.getName());
    if (stableValue == null) {
      stableValue = _param.getXmlDefault();
      if (stableValue != null)
        requestParams.setParam(_param.getName(), stableValue);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new NumberParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, CompleteValidStableValues stableValues) throws WdkModelException {
    return toRawValue(user, stableValues.get(_param.getName()));
  }

}
