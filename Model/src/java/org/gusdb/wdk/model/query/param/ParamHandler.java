package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * A param handler can be used to process the internal param value before it is
 * consumed by the query instance.
 * 
 * @author jerric
 * 
 */
public interface ParamHandler {

  void setParam(Param param);

  void setWdkModel(WdkModel wdkModel);

  void setProperties(Map<String, String> properties) throws WdkModelException;

  String toStableValue(User user, String rawValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException;

  String toRawValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException;

  String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException;

  String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException;

}
