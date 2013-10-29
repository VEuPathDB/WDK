/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class TimestampParamHandler extends AbstractParamHandler {

  /**
   * The raw value is the same as stable value.
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
   * The raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String refernceValue,
      Map<String, String> contextValues) throws WdkUserException {
    return refernceValue;
  }

  /**
   * The internal value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    return stableValue;
  }

  /**
   * The stable value is the same as signature.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    return stableValue;
  }

}
