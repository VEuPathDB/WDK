package org.gusdb.wdk.model.user.dataset;

import java.io.InputStream;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A handle on a file within a user dataset.
 * @author steve
 *
 */
public abstract class UserDatasetFile {
  private Path filePath;
  private Long userDatasetId;
  
  public UserDatasetFile(Path filePath, Long userDatasetId) {
    this.filePath = filePath;
    this.userDatasetId = userDatasetId;
  }
  
  public Path getFilePath() {
    return filePath;
  }

  /**
   * Get the contents of the file as a stream
   * @return
   */
  public abstract InputStream getFileContents(UserDatasetSession dsSession, Path path) throws WdkModelException;
  
  /**
   * Get the size of the file
   * @return
   */
  public abstract Long getFileSize(UserDatasetSession dsSession) throws WdkModelException;
  
  /**
   * Get the file's name.  There is no path, just a base name, because within
   * a dataset the files are flat
   * @return
   */
  public abstract String getFileName(UserDatasetSession dsSession) throws WdkModelException;
  
  /**
   * Make a local copy of this user dataset file.  tmpWorkingDir is a temp dir that is dedicated
   * to the job that needs this local copy.
   * Call removeLocalCopy() when done.
   * @return The full path as a String.
   * @throws WdkModelException
   */
  public Path getLocalCopy(UserDatasetSession dsSession, Path tmpWorkingDir) throws WdkModelException {
      Path localCopy = tmpWorkingDir.resolve(getFileName(dsSession));
      createLocalCopy(dsSession, localCopy);
      return localCopy;
  }
  
  protected abstract void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException;
  
  /**
   * for use by implementers of getLocalCopy, to 
   * @return
   */
  protected Long getUserDatasetId() {
    return userDatasetId;
  }
}
