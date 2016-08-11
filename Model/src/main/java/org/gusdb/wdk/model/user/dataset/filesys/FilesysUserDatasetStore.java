package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;

/**
 * An implementation of JsonUserDatasetStore that uses the java nio Files operations
 * @author steve
 *
 */

public class FilesysUserDatasetStore extends org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore {

  public FilesysUserDatasetStore() {
    super(new FilesysUserDatasetStoreAdaptor());
  }

  @Override
  public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    super.initialize(configuration, typeHandlers);
    try {
      checkRootDirExists();
    }
    catch (WdkModelException e) {
      // if root dir does not exist, try to create with 777 access
      try {
        Set<PosixFilePermission> openAccess = new HashSet<>(Arrays.asList(PosixFilePermission.values()));
        Files.createDirectories(usersRootDir, PosixFilePermissions.asFileAttribute(openAccess));
      }
      catch (IOException e2) {
        throw new WdkModelException("Users root dir [" + usersRootDir + "] does not exist and cannot be created.", e2);
      }
    }
  }
}
