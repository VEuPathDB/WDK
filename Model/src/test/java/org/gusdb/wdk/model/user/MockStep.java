package org.gusdb.wdk.model.user;

import java.util.Random;

import org.gusdb.wdk.model.WdkModelException;

public abstract class MockStep extends Step {

  public static final String TYPE_A = "a";
  public static final String TYPE_B = "b";
  
  private static final Random random = new Random();

  private final String projectId;
  private final String type;

  public MockStep(StepFactory stepFactory, User user, String type) {
    super(stepFactory, user, random.nextInt(Integer.MAX_VALUE) + 1);
    this.projectId = stepFactory.getWdkModel().getProjectId();
    this.type = type;
  }

  @Override
  public String getType() throws WdkModelException {
    return type;
  }

  @Override
  public String getProjectId() {
    return projectId;
  }
}
