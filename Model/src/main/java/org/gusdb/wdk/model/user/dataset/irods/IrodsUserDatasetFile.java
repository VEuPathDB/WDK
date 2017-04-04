/**
 * 
 */
package org.gusdb.wdk.model.user.dataset.irods;

import java.io.InputStream;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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
	IRODSAccessObjectFactory accessObjectFactory = IrodsUserDatasetStoreAdaptor.getIrodsAccessObjectFactory();
	IRODSFileFactory fileFactory = IrodsUserDatasetStoreAdaptor.getIrodsFileFactory(accessObjectFactory);
	IRODSFile irodsFile = null;
	try {
	  irodsFile = IrodsUserDatasetStoreAdaptor.getIrodsFile(accessObjectFactory, getFilePath().toString()); 
	  return fileFactory.instanceIRODSFileInputStream(irodsFile);
	}
	catch (JargonException je) {
	  throw new WdkModelException("Unable to create a file input stream for the file " + irodsFile.getName(), je);
	}
	finally {
	  IrodsUserDatasetStoreAdaptor.closeFile(irodsFile);
	  IrodsUserDatasetStoreAdaptor.closeSession(accessObjectFactory);
	}
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileSize()
   */
  @Override
  public Long getFileSize() throws WdkModelException {
    IRODSAccessObjectFactory accessObjectFactory = IrodsUserDatasetStoreAdaptor.getIrodsAccessObjectFactory();
	IRODSFile irodsFile = null;
	try {
	  irodsFile = IrodsUserDatasetStoreAdaptor.getIrodsFile(accessObjectFactory,getFilePath().toString()); 
	  return irodsFile.length();
	}
	finally {
	  IrodsUserDatasetStoreAdaptor.closeFile(irodsFile);
	  IrodsUserDatasetStoreAdaptor.closeSession(accessObjectFactory);
	}
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileName()
   */
  @Override
  public String getFileName() throws WdkModelException {
	IRODSAccessObjectFactory accessObjectFactory = IrodsUserDatasetStoreAdaptor.getIrodsAccessObjectFactory();
	IRODSFile irodsFile = null;
	try {
	  irodsFile = IrodsUserDatasetStoreAdaptor.getIrodsFile(accessObjectFactory,getFilePath().toString());
      return irodsFile.getName();
	}
	finally {
	  IrodsUserDatasetStoreAdaptor.closeFile(irodsFile);
	  IrodsUserDatasetStoreAdaptor.closeSession(accessObjectFactory);
	}
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.UserDatasetFile#createLocalCopy(Path)
   */
  @Override
  protected void createLocalCopy(Path tmpFile) throws WdkModelException {
    IRODSAccessObjectFactory accessObjectFactory = IrodsUserDatasetStoreAdaptor.getIrodsAccessObjectFactory();
	IRODSFile irodsFile = null;
	try {
	  irodsFile = IrodsUserDatasetStoreAdaptor.getIrodsFile(accessObjectFactory,getFilePath().toString());
	  IrodsUserDatasetStoreAdaptor.getDataTransferOperations(accessObjectFactory).getOperation(irodsFile, tmpFile.toFile(), null, null);
	}
	catch(JargonException je) {
	  throw new WdkModelException("Unable to copy " + getFilePath().toString() + " to " + tmpFile.toString() + ". - ", je);
	}
	finally {
	  IrodsUserDatasetStoreAdaptor.closeFile(irodsFile);
	  IrodsUserDatasetStoreAdaptor.closeSession(accessObjectFactory);
	}
  }

}
