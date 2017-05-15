package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;

public class FilesysUserDatasetFile extends UserDatasetFile {
  
 
  public FilesysUserDatasetFile(Path filePath, Long userDatasetId) {
    super(filePath, userDatasetId);
  }

  @Override
  public InputStream getFileContents(UserDatasetSession dsSession) throws WdkModelException {
    try {
      return Files.newInputStream(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public Long getFileSize(UserDatasetSession dsSession) throws WdkModelException {
    try {
      return Files.size(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public String getFileName(UserDatasetSession dsSession) {
    return getFilePath().getFileName().toString();
  }

  @Override
  protected void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException {
    try {
      Files.copy(getFilePath(), tmpFile);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
}
