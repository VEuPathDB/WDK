package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;

public class ExampleTypeHandler implements UserDatasetTypeHandler {

  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UserDatasetType getUserDatasetType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setType(String type) throws WdkModelException {
   if (!type.equals("example")) throw new WdkModelException("ExampleTypeHandler only handles type 'example'");
    
  }

  @Override
  public void setVersion(String version) throws WdkModelException {
    if (!version.equals("1.0")) throw new WdkModelException("ExampleTypeHandler only handles version '1.0'");
  }

}
