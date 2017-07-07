package org.gusdb.wdk.model.user.dataset.filesys;

import java.nio.file.Path;

import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;

/**
 * An implementation of JsonUserDatasetSession that uses the java nio Files operations
 * @author steve
 *
 */

public class FilesysUserDatasetSession extends JsonUserDatasetSession {

  public FilesysUserDatasetSession(Path usersRootDir) {
    super(new FilesysUserDatasetStoreAdaptor(), usersRootDir);
  }

 
  @Override
  public UserDatasetFile getUserDatasetFile(Path path, Long userDatasetId) {
    return new FilesysUserDatasetFile(path, userDatasetId);
  }
}
