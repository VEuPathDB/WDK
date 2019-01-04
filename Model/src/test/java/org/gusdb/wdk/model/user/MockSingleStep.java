package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModel;

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
  public Step getPrimaryInputStep() {
    return null;
  }

  @Override
  public Step getSecondaryInputStep() {
    return null;
  }
}
