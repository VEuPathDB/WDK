package org.gusdb.wdk.model.user;

import java.util.Random;

import org.gusdb.wdk.model.WdkModel;

public class MockUser extends User {

  private static final Random random = new Random();

  public MockUser(WdkModel model) {
    super(model, random.nextInt(Integer.MAX_VALUE) + 1, "mock@email", "mock signatur");
  }

}
