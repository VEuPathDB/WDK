package org.gusdb.wdk.model.query.param;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;


public class NumberRangeParamHandler extends AbstractParamHandler {

  public NumberRangeParamHandler(){}
  
  public NumberRangeParamHandler(NumberRangeParamHandler handler, Param param) {
    super(handler, param);
  }
  
  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextParamValues)
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
  public String toRawValue(User user, String stableValue, Map<String, String> contextParamValues) {
    return stableValue;
  }

  /**
   * the signature is a checksum of the stable value.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  @Override
  public String toSignature(User user, String stableValue)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0) return "";
    return Utilities.encrypt(stableValue);
  }

  /**
   * Formats the stableValue into a value that can be applied to SQL statements.
   * @throws WdkModelException
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
	  
	// Something to do with the portal - left this alone  
	if(param.isNoTranslation()) {
	  return stableValue;
	}

	// By now stableValue parses properly
	JSONObject valueJson = new JSONObject(stableValue);
	Double values[] = { valueJson.getDouble("min"), valueJson.getDouble("max") };
	
	// Modify both ends of the range as needed and reassemble as a JSONObject
	for(Double value : values) {
    
      // If the number is in exponential form, change to decimal form
      if(stableValue.matches("^.*(e|E).*$")) {
        value = new Double(new BigDecimal(value.doubleValue()).toPlainString());
      }
    
      // If the number is not an integer, round it according to the number of decimal places
      // requested (or the default value).
      if(!((NumberRangeParam)param).isInteger()) {
    	MathContext mathContext = new MathContext(((NumberRangeParam)param).getNumDecimalPlaces(), RoundingMode.HALF_UP);
    	value = (new BigDecimal(value.doubleValue(), mathContext)).doubleValue();
      }
	}
    return new JSONObject().put("min", new Double(values[0])).put("max", new Double(values[1])).toString();
  }

  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    return validateStableValueSyntax(user, requestParams.getParam(param.getName()));
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException, WdkModelException {
    String stableValue = inputStableValue;
    if (stableValue == null) {
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required");
      stableValue = param.getEmptyValue();
    }
    if (stableValue != null)
      stableValue = stableValue.trim();
    return stableValue;
  }
  
  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
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
    return new NumberRangeParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    return toRawValue(user, stableValue, contextParamValues);
  }

}
