package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

public class FilesysUserDatasetFile extends UserDatasetFile {
  
 
  public FilesysUserDatasetFile(Path filePath, Integer userDatasetId) {
    super(filePath, userDatasetId);
  }

  @Override
  public InputStream getFileContents() throws WdkModelException {
    try {
      return Files.newInputStream(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public Long getFileSize() throws WdkModelException {
    try {
      return Files.size(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public String getFileName() {
    return getFilePath().getFileName().toString();
  }

  @Override
  protected void createLocalCopy(Path tmpFile) throws WdkModelException {
    try {
      Files.copy(getFilePath(), tmpFile);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
}
