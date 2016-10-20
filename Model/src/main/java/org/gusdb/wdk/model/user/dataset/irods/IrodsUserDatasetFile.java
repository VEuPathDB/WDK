/**
 * 
 */
package org.gusdb.wdk.model.user.dataset.irods;

import java.io.InputStream;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

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
	IRODSFile file = IrodsUserDatasetStoreAdaptor.getFile(getFilePath().toString()); 
	IRODSFileFactory factory = IrodsUserDatasetStoreAdaptor.getFactory();
	try {
	  return factory.instanceIRODSFileInputStream(file);
	} 
	catch (JargonException je) {
	  throw new WdkModelException("Unable to create a file input stream for the file " + file.getName(), je);
	}
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileSize()
   */
  @Override
  public Long getFileSize() throws WdkModelException {
	IRODSFile file = IrodsUserDatasetStoreAdaptor.getFile(getFilePath().toString());
	return file.length();
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileName()
   */
  @Override
  public String getFileName() throws WdkModelException {
	IRODSFile file = IrodsUserDatasetStoreAdaptor.getFile(getFilePath().toString());
    return file.getName();
  }

  @Override
  protected void createLocalCopy(Path tmpFile) throws WdkModelException {
	IRODSFile irodsFile = IrodsUserDatasetStoreAdaptor.getFile(getFilePath().toString());
	try {
	  IrodsUserDatasetStoreAdaptor.getDataTransferOperations().getOperation(irodsFile, tmpFile.toFile(), null, null);
	}
	catch(JargonException je) {
	  throw new WdkModelException("Unable to copy " + getFilePath().toString() + " to " + tmpFile.toString() + ". - ", je);
	}
  }

}
