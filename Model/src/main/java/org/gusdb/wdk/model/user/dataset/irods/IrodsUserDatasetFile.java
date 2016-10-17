/**
 * 
 */
package org.gusdb.wdk.model.user.dataset.irods;

import java.io.InputStream;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;

/**
 * @author steve
 *
 */
public class IrodsUserDatasetFile extends UserDatasetFile {

  public IrodsUserDatasetFile(Path filePath, Integer userDatasetId) {
    super(filePath, userDatasetId);
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileContents()
   */
  @Override
  public InputStream getFileContents() throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileSize()
   */
  @Override
  public Long getFileSize() throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileName()
   */
  @Override
  public String getFileName() throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void createLocalCopy(Path tmpFile) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

}
