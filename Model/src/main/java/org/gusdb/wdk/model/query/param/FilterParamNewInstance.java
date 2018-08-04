package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.ValidStableValues;
import org.gusdb.wdk.model.user.User;

/**
 * this is a class that likely needs to be retired.  the current implementation is almost a no-op.  
 * we still have it because FilterParamNew, perhaps wrongly, is an AbstractDependentParam.  ADP requires
 * it to provide a DependentParamInstance.  The world of ADP needs to be re-thought.  Meantime, we have a no-op here.
 */
public class FilterParamNewInstance implements DependentParamInstance {

  // param this cache was created by
  private FilterParamNew _param;

  public FilterParamNewInstance(FilterParamNew param) {
    _param = param;
  }

  /**
   * only needed by actions, which FilterParamNew does not support.   
   */
  @Override
  public String getValidStableValue(User user, ValidStableValues contextParamValues)
      throws WdkModelException, WdkUserException {
    String stableValue = contextParamValues.get(_param.getName());
    return _param.getValidStableValue(user, stableValue, contextParamValues);
  }
}
