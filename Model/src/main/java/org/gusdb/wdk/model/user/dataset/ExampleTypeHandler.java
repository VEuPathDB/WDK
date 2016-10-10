package org.gusdb.wdk.model.user.dataset;

import javax.sql.DataSource;

public class ExampleTypeHandler implements UserDatasetTypeHandler {

  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset, DataSource appDbDataSource) {
    return new UserDatasetCompatibility(true, "");
  }

  @Override
  public UserDatasetType getUserDatasetType() {
    return UserDatasetTypeFactory.getUserDatasetType("example", "1.0");
  }

  @Override
  public void installInAppDb(UserDataset userDataset, DataSource appDbDataSource,
      String userDatasetSchemaName) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void uninstallInAppDb(Integer userDatasetId, DataSource appDbDataSource,
      String userDatasetSchemaName) {
    // TODO Auto-generated method stub
    
  }


}
