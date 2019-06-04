package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

/**
 * A param handler can be used to process the internal param value before it is
 * consumed by the query instance.
 *
 * @author jerric
 */
public interface ParamHandler {

  void setParam(Param param);

  void setWdkModel(WdkModel wdkModel);

  void setProperties(Map<String, String> properties) throws WdkModelException;

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

  /**
   * Converts the param into an empty value which can be used to produce a
   * syntactically correct parametrized query.
   */
  String toEmptyInternalValue();

  String toSignature(RunnableObj<QueryInstanceSpec> contextParamValues) throws WdkModelException;

  String getDisplayValue(QueryInstanceSpec contextParamValues) throws WdkModelException;

  ParamHandler clone(Param param);

}
