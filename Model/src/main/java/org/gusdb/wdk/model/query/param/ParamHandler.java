package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
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
   * get the stable value from the input request params; and if empty value is allowed, get the empty value if
   * no value is given by the user.
   *
   * @param user
   * @param requestParams
   * @return the raw value of a param
   * @throws WdkUserException
   * @throws WdkModelException
   */
  @Deprecated
  String getStableValue(User user, RequestParams requestParams) throws WdkUserException, WdkModelException;

  /**
   * Clean (eg, trim whitespace) and validate a stable value (from a user).  Should be called by getStableValue
   * @param user
   * @param inputStableValue
   * @return a cleaned stable value.
   * @throws WdkUserException
   * @throws WdkModelException
   */
   String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException, WdkModelException;

  /**
   * Prepare the display of the param in the question form.
   *
   * @param user
   * @param requestParams
   * @param contextValues
   * @throws WdkUserException
   * @throws WdkModelException
   */
   @Deprecated
   void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException;

  /**
   * convert raw value into stable value.
   *
   * @param user
   * @param rawValue
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   */
  String toStableValue(User user, Object rawValue)
      throws WdkUserException, WdkModelException;

  /**
   * convert
   *
   * @param user
   * @param stableValue
   * @return
   * @throws WdkModelException
   */
  Object toRawValue(User user, String stableValue) throws WdkModelException;

  String toInternalValue(RunnableObj<QueryInstanceSpec> contextParamValues) throws WdkModelException;

  String toSignature(RunnableObj<QueryInstanceSpec> contextParamValues) throws WdkModelException;

  String getDisplayValue(QueryInstanceSpec contextParamValues) throws WdkModelException;

  ParamHandler clone(Param param);

}
