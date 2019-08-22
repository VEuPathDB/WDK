package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore;

/**
 * An implementation of JsonUserDatasetStore that uses the java nio Files
 * operations
 *
 * @author steve
 */
public class FilesysUserDatasetStore extends JsonUserDatasetStore {

  @Override
  public void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, Path wdkTempDir) throws WdkModelException {
    super.initialize(configuration, typeHandlers, wdkTempDir);
    try (FilesysUserDatasetSession session = getSession()) {
      session.checkRootDirExists();
      _id = session.initializeUserDatasetStoreId();
    }
    catch (WdkModelException e) {
      // if root dir does not exist, try to create with 777 access
      try {
        Set<PosixFilePermission> openAccess = new HashSet<>(Arrays.asList(PosixFilePermission.values()));
        Files.createDirectories(_usersRootDir, PosixFilePermissions.asFileAttribute(openAccess));
      }
      catch (IOException e2) {
        throw new WdkModelException("Users root dir [" + _usersRootDir + "] does not exist and cannot be created.", e2);
      }
    }
  }

  @Override
  public FilesysUserDatasetSession getSession() {
    return new FilesysUserDatasetSession(_usersRootDir);
  }

  @Override
  public FilesysUserDatasetSession getSession(Path usersRootDir) {
    return new FilesysUserDatasetSession(usersRootDir);
  }

}
