package org.gusdb.wdk.model.user.dataset.irods;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.irods.jargon.core.transfer.TransferControlBlock;

/**
 * @author steve
 */
public class IrodsUserDatasetFile extends UserDatasetFile {

  public IrodsUserDatasetFile(Path filePath, Long userDatasetId) {
    super(filePath, userDatasetId);
  }

  @Override
  public InputStream getFileContents(UserDatasetSession dsSession, Path temporaryDirPath) throws WdkModelException {
    long start = System.currentTimeMillis();
    //IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
    try {
      Path localPath = getLocalCopy(dsSession, temporaryDirPath);
      return Files.newInputStream(localPath);
    }
    catch(IOException e) {
      throw new WdkModelException(e);
    }
    finally {
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileContents-irods: " + getFilePath().toString(),start);
    }
  }

  public IRODSRandomAccessFile getRandomAccessFile(UserDatasetSession dsSession, long offset) throws WdkModelException {
    long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
    IRODSRandomAccessFile irodsRandomAccessFile = null;
    try {
      IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
      irodsRandomAccessFile = adaptor.getIrodsRandomAccessFile(fileFactory, getFilePath().toString());
      irodsRandomAccessFile.seek(offset,SeekWhenceType.SEEK_START);
      return irodsRandomAccessFile;
    }
    catch (IOException ioe) {
      throw new WdkModelException("Unable to access the file " + getFilePath().toString(), ioe);
    }
    finally {
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileContents (RA)-irods: " + getFilePath().toString(), start);
    }
  }

  @Override
  protected Long readFileSize(UserDatasetSession dsSession) throws WdkModelException {
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

  @Override
  protected void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException {
    long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
    TransferControlBlock tcb = adaptor.getTranferControlBlock();
    IRODSFile irodsFile = null;
    try {
      IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
      irodsFile = adaptor.getIrodsFile(fileFactory,getFilePath().toString());
      tcb.getTransferOptions().setMaxThreads(1);
      adaptor.getDataTransferOperations().getOperation(irodsFile, tmpFile.toFile(), null, tcb);
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
