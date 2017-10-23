package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;

public class IrodsUserDatasetSession extends JsonUserDatasetSession {

  public IrodsUserDatasetSession(Path usersRootDir, String wdkTempDirName) throws WdkModelException {
    super(new IrodsUserDatasetStoreAdaptor(wdkTempDirName, usersRootDir), usersRootDir);
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, Long userDatasetId) {
    return new IrodsUserDatasetFile(path, userDatasetId);
  }

  @Override
  public void close() {
    ((IrodsUserDatasetStoreAdaptor) adaptor).closeSession();
  }  
}
