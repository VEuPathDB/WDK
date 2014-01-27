package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * A param handler can be used to process the internal param value before it is consumed by the query
 * instance.
 * 
 * @author jerric
 * 
 */
public interface ParamHandler {

  void setParam(Param param);

  void setWdkModel(WdkModel wdkModel);

  void setProperties(Map<String, String> properties) throws WdkModelException;

  /**
   * get the raw value from the input request params; and if empty value is allowed, get the empty value if no
   * value is given by the user.
   * 
   * @param user
   * @param requestParams
   * @return the raw value of a param
   * @throws WdkUserException
   * @throws WdkModelException
   */
  Object getRawValue(User user, RequestParams requestParams) throws WdkUserException, WdkModelException;

  /**
   * convert raw value into stable value.
   * 
   * @param user
   * @param rawValue
   * @param contextValues
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  String toStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkUserException, WdkModelException;

  /**
   * convert
   * 
   * @param user
   * @param stableValue
   * @param contextValues
   * @return
   * @throws WdkModelException
   */
  Object toRawValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException;

  String toInternalValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException;

  String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException;

}
