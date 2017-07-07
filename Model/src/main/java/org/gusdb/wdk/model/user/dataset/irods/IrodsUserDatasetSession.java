package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;

import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;

public class IrodsUserDatasetSession extends JsonUserDatasetSession {

  public IrodsUserDatasetSession(Path usersRootDir) {
    super(new IrodsUserDatasetStoreAdaptor(), usersRootDir);
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
