package org.gusdb.wdk.model.user.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A handle on a file within a user dataset.
 * @author steve
 *
 */
public abstract class UserDatasetFile {
  private Path filePath;
  private Integer userDatasetId;
  
  public UserDatasetFile(Path filePath, Integer userDatasetId) {
    this.filePath = filePath;
    this.userDatasetId = userDatasetId;
  }
  
  protected Path getFilePath() {
    return filePath;
  }

  /**
   * Get the contents of the file as a stream
   * @return
   */
  public abstract InputStream getFileContents() throws WdkModelException;
  
  /**
   * Get the size of the file
   * @return
   */
  public abstract Long getFileSize() throws WdkModelException;
  
  /**
   * Get the file's name.  There is no path, just a base name, because within
   * a dataset the files are flat
   * @return
   */
  public abstract String getFileName() throws WdkModelException;
  
  /**
   * Make a local copy of this user dataset file.  
   * Call removeLocalCopy() when done.
   * @return The full path as a String.
   * @throws WdkModelException
   */
  public Path getLocalCopy(Path tmpDir) throws WdkModelException {
    try {
      // use datasetid as prefix to be doubly sure of universal file name uniqueness
      Path tmpFile = Files.createTempFile(tmpDir, userDatasetId.toString() + "_", null);
      createLocalCopy(tmpFile);
      return tmpFile;
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }
  
  protected abstract void createLocalCopy(Path tmpFile) throws WdkModelException;
  
  /**
   * for use by implementers of getLocalCopy, to 
   * @return
   */
  protected Integer getUserDatasetId() {
    return userDatasetId;
  }
}
