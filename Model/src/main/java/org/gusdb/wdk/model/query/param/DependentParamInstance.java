package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

public interface DependentParamInstance {

  String getValidStableValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException;

}
