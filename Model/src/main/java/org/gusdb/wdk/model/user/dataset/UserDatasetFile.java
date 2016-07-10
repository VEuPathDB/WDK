package org.gusdb.wdk.model.user.dataset;

import java.io.OutputStream;

import org.gusdb.wdk.model.WdkModelException;

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
  OutputStream getFileContents() throws WdkModelException;
  
  /**
   * Get the size of the file
   * @return
   */
  Integer getFileSize() throws WdkModelException;
  
  /**
   * Get the file's name.  There is no path, just a base name, because within
   * a dataset the files are flat
   * @return
   */
  String getFileName() throws WdkModelException;
}
