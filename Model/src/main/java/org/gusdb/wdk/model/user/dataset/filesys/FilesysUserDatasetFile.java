package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

public class FilesysUserDatasetFile implements UserDatasetFile {
  
  private Path filePath;
  
  public FilesysUserDatasetFile(Path filePath) {
    this.filePath = filePath;
  }

  @Override
  public InputStream getFileContents() throws WdkModelException {
    try {
      return Files.newInputStream(filePath);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public Long getFileSize() throws WdkModelException {
    try {
      return Files.size(filePath);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  public String getFileName() {
    return filePath.getFileName().toString();
  }

}
