package org.gusdb.wdk.model;

import org.gusdb.wdk.model.user.MockStepFactory;
import org.gusdb.wdk.model.user.StepFactory;

public class MockWdkModel extends WdkModel {

  @Override
  public StepFactory getStepFactory() {
    return new MockStepFactory(this);
  }
  
  @Override
  public String getProjectId() {
    return "MockProject";
  }
}
