package org.gusdb.wdk.model.query.param;

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

  void setWdkModel(WdkModel wdkModel) throws WdkUserException,
      WdkModelException;

  String transform(User user, String internalValue) throws WdkModelException;
}
