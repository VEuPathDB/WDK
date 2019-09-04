package org.gusdb.wdk.model.user.dataset.filesys;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;

/**
 * An implementation of JsonUserDatasetSession that uses the java nio Files
 * operations
 *
 * @author steve
 */
public class FilesysUserDatasetSession extends JsonUserDatasetSession {

  public FilesysUserDatasetSession(Path usersRootDir) {
    super(new FilesysUserDatasetStoreAdaptor(), usersRootDir);
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, long userDatasetId) {
    return new FilesysUserDatasetFile(path, userDatasetId);
  }

  @Override
  public List<Path> getRecentEvents(String eventDirectory, long lastHandledEventId) throws WdkModelException {
    return adaptor.getPathsInDir(Paths.get(eventDirectory));
  }
}
