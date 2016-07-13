package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.OutputStream;
import java.nio.file.Path;

import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

public class FilesysUserDatasetFile implements UserDatasetFile {
  
  private Path filePath;
  
  public FilesysUserDatasetFile(Path filePath) {
    this.filePath = filePath;
  }

  @Override
  public OutputStream getFileContents() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getFileSize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getFileName() {
    // TODO Auto-generated method stub
    return null;
  }

}
