package org.gusdb.wdk.model.user.dataset;

public class ExampleTypeHandler implements UserDatasetTypeHandler {
  private String type;
  private String version;

  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset) {
    return new UserDatasetCompatibility(true, "");
  }

  @Override
  public UserDatasetType getUserDatasetType() {
    return UserDatasetTypeFactory.getUserDatasetType(type, version);
  }
}
