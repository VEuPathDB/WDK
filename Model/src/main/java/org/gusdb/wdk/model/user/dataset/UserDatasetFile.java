package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * A handle on a file within a user dataset.
 *
 * @author steve
 */
public abstract class UserDatasetFile {

  private Path filePath;
  private Long userDatasetId;

  public UserDatasetFile(Path filePath, long userDatasetId) {
    this.filePath = filePath;
    this.userDatasetId = userDatasetId;
  }

  public Path getFilePath() {
    return filePath;
  }

  public String getFileName() {
    return getFilePath().getFileName().toString();
  }

  /**
   * Get the contents of the file as a stream
   */
  public abstract InputStream getFileContents(UserDatasetSession dsSession, Path path) throws WdkModelException;

  public abstract long getFileSize() throws WdkModelException;

  /**
   * Make a local copy of this user dataset file.  tmpWorkingDir is a temp dir
   * that is dedicated to the job that needs this local copy. Call
   * removeLocalCopy() when done.
   *
   * @return The full path as a String.
   */
  public Path getLocalCopy(UserDatasetSession dsSession, Path tmpWorkingDir) throws WdkModelException {
      Path localCopy = tmpWorkingDir.resolve(getFileName());
      createLocalCopy(dsSession, localCopy);
      return localCopy;
  }

  protected abstract void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException;

  /**
   * for use by implementers of getLocalCopy, to
   */
  protected Long getUserDatasetId() {
    return userDatasetId;
  }

  /**
   * Reads the byte range specified by {@code offset} and {@code len} from this
   * user dataset file into the provided {@link OutputStream} {@code into}.
   *
   * @param dsSess
   *   user dataset session service
   * @param offset
   *   byte range offset, this many bytes will be skipped from the beginning of
   *   the file
   * @param len
   *   number of bytes to read into the given output stream
   * @param into
   *   output stream into which {@code len} bytes will be read from this user
   *   dataset file
   *
   * @return the number of bytes read from this file into the given output
   *   stream.  This number may differ from {@code len} if the
   *   difference between {@code offset} and the total size of this file is less
   *   than {@code len}.
   *
   * @throws WdkRuntimeException
   *   may be thrown by the underlying implementation due to any error that
   *   occur while attempting to read this file or write to the given output
   *   stream.
   */
  public abstract long readRangeInto(UserDatasetSession dsSess, long offset,
    long len, OutputStream into) throws WdkRuntimeException;
}
