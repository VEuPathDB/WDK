/**
 * 
 */
package org.gusdb.wdk.model.user.dataset.irods;

import java.io.InputStream;
import java.nio.file.Path;

import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

/**
 * @author steve
 *
 */
public class IrodsUserDatasetFile extends UserDatasetFile {

  public IrodsUserDatasetFile(Path filePath, Long userDatasetId) {
	super(filePath, userDatasetId);
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileContents()
   */
  @Override
  public InputStream getFileContents(UserDatasetSession dsSession) throws WdkModelException {
	long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
	IRODSFile irodsFile = null;
	try {
	  IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
	  irodsFile = adaptor.getIrodsFile(fileFactory, getFilePath().toString());
	  return fileFactory.instanceIRODSFileInputStream(irodsFile);
	}
	catch (JargonException je) {
	  throw new WdkModelException("Unable to create a file input stream for the file " + irodsFile.getName(), je);
	}
	finally {
	  adaptor.closeFile(irodsFile);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileContents-irods: " + getFilePath().toString(), start);
	}
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileSize()
   */
  @Override
  public Long getFileSize(UserDatasetSession dsSession) throws WdkModelException {
	long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
	IRODSFile irodsFile = null;
	try {
	  IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
	  irodsFile = adaptor.getIrodsFile(fileFactory, getFilePath().toString());
	  return irodsFile.length();
	}
	finally {
	  adaptor.closeFile(irodsFile);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileSize-irods: " + getFilePath().toString(),start);
	}
  }

  /* (non-Javadoc)
   * @see org.gusdb.wdk.model.user.dataset.UserDatasetFile#getFileName()
   */
  @Override
  public String getFileName(UserDatasetSession dsSession) throws WdkModelException {
	long start = System.currentTimeMillis();
	IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
	IRODSFile irodsFile = null;
	try {
	  IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
	  irodsFile = adaptor.getIrodsFile(fileFactory,getFilePath().toString());
      return irodsFile.getName();
	}
	finally {
	  adaptor.closeFile(irodsFile);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileName-irods: " + getFilePath().toString(),start);
	}
  }

  /**
   * @see
   * org.gusdb.wdk.model.user.dataset.UserDatasetFile#createLocalCopy(Path)
   */
  @Override
  protected void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException {
	long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
	IRODSFile irodsFile = null;
	try {
	  IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
	  irodsFile = adaptor.getIrodsFile(fileFactory,getFilePath().toString());
	  adaptor.getDataTransferOperations().getOperation(irodsFile, tmpFile.toFile(), null, null);
	}
	catch(JargonException je) {
	  throw new WdkModelException("Unable to copy " + getFilePath().toString() + " to " + tmpFile.toString() + ". - ", je);
	}
	finally {
	  adaptor.closeFile(irodsFile);
	  QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","createLocalCopy-irods:" + getFilePath().toString(),start);
	}
  }

}
