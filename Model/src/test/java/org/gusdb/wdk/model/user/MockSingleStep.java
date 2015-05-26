package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.WdkModelException;

public class MockSingleStep extends MockStep {

  public MockSingleStep(StepFactory stepFactory, User user, String type) throws WdkModelException {
    super(stepFactory, user, type);
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
