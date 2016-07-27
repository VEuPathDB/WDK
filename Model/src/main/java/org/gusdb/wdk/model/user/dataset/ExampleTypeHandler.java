package org.gusdb.wdk.model.user.dataset;

public class ExampleTypeHandler implements UserDatasetTypeHandler {

  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset) {
    return new UserDatasetCompatibility(true, "");
  }

  @Override
  public UserDatasetType getUserDatasetType() {
    return UserDatasetTypeFactory.getUserDatasetType("example", "1.0");
  }
}
