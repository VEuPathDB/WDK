package org.gusdb.wdk.model.user.dataset;

import java.io.OutputStream;

/**
 * A handle on a file within a user dataset.
 * @author steve
 *
 */
public interface UserDatasetFile {
  /**
   * Get the contents of the file as a stream
   * @return
   */
  OutputStream getFileContents();
  
  /**
   * Get the size of the file
   * @return
   */
  Integer getFileSize();
  
  /**
   * Get the file's name.  There is no path, just a base name, because within
   * a dataset the files are flat
   * @return
   */
  String getFileName();
}
