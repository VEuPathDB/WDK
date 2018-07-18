package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class MockSingleStep extends MockStep {

  public MockSingleStep(WdkModel wdkModel, User user, String type) {
    super(wdkModel, user, type);
  }

  @Override
  public boolean isFirstStep() {
    return true;
  }
  
  @Override
  public boolean isCombined() {
    return false;
  }
  
  @Override
  public boolean isTransform() {
    return false;
  }

  @Override
  public Step getPreviousStep() throws WdkModelException {
    return null;
  }

  @Override
  public Step getChildStep() throws WdkModelException {
    return null;
  }
}
