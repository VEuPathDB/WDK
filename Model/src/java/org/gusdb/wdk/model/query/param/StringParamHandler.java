/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class StringParamHandler extends AbstractParamHandler {

  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, String rawValue,
      Map<String, String> contextValues) throws WdkUserException {
    return rawValue;
  }

  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String stableValue,
      Map<String, String> contextValues) {
    return stableValue;
  }

  /**
   * the stable value is the same as signature.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) {
    return stableValue;
  }

  /**
   * If number is true, the internal is a string representation of a parsed
   * Double; otherwise, quotes are properly applied; If noTranslation is true,
   * the reference value is used without any change.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) {
    if (param.isNoTranslation()) return stableValue;

    StringParam stringParam = (StringParam) param;
    if (stringParam.isNumber()) {
      stableValue = stableValue.replaceAll(",", "");
      double value = Double.valueOf(stableValue);
      return Double.toString(value);
    } else {
      stableValue = stableValue.replaceAll("'", "''");
      return "'" + stableValue + "'";
    }
  }

}
