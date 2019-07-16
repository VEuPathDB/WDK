package org.gusdb.wdk.model.user.dataset.irods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.*;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.transfer.TransferControlBlock;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

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

  @Override
  public long readRangeInto(
    UserDatasetSession dsSess,
    long offset,
    long len,
    OutputStream into
  ) throws WdkRuntimeException {
    if (len == 0)
      return 0;

    final IrodsUserDatasetStoreAdaptor adaptor;
    long wrote = 0;

    adaptor = (IrodsUserDatasetStoreAdaptor) dsSess.getUserDatasetStoreAdaptor();
    try {
      IRODSFileFactory fileFac = adaptor.getIrodsFileFactory();
      IRODSFile file = fileFac.instanceIRODSFile(getFilePath().toString());
      try (IRODSFileInputStream is = fileFac.instanceIRODSFileInputStream(file)) {
        if (file.length() <= offset)
          return 0;

        is.skip(offset);

        final byte[] buffer = new byte[(int) min(32768L, len)];

        for (int i = (int) ceil((double) len / buffer.length); i > 0; i--) {
          final int d = is.read(buffer);
          into.write(buffer, 0, d);
          wrote += d;
        }
      }
    } catch (Exception ioe) {
      throw new WdkRuntimeException("Unable to access the file " + getFilePath().toString(), ioe);
    }

    return wrote;
  }
}
