package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

public interface DependentParamInstance {

  //TODO - CWL Verify
  String getValidStableValue(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException, WdkUserException;

}
