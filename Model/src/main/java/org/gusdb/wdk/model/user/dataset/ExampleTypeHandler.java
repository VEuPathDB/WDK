package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;

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

  @Override
  public void setType(String type) throws WdkModelException {
   if (!type.equals("example")) throw new WdkModelException("ExampleTypeHandler only handles type 'example'");
    this.type = type;
  }

  @Override
  public void setVersion(String version) throws WdkModelException {
    if (!version.equals("1.0")) throw new WdkModelException("ExampleTypeHandler only handles version '1.0'");
    this.version = version;
  }

}
