package org.gusdb.wdk.model.user;

import java.util.Random;

import org.gusdb.wdk.model.WdkModel;

public abstract class MockStep extends Step {

  public static final String TYPE_A = "a";
  public static final String TYPE_B = "b";
  
  private static final Random random = new Random();

  private final String projectId;
  private final String type;

  public MockStep(WdkModel wdkModel, User user, String type) {
    super(wdkModel, user, random.nextInt(Integer.MAX_VALUE) + 1);
    this.projectId = wdkModel.getProjectId();
    this.type = type;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getProjectId() {
    return projectId;
  }
}
