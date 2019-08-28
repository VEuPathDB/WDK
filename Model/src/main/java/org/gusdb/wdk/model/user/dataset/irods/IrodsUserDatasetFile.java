package org.gusdb.wdk.model.user.dataset.irods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
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

  private static final TraceLog TRACE = new TraceLog(IrodsUserDatasetFile.class);

  private final ICatDataObject iCatData;

  IrodsUserDatasetFile(Path filePath, ICatDataObject data, long userDatasetId) {
    super(filePath, userDatasetId);
    iCatData = data;
  }

  @Override
  public InputStream getFileContents(UserDatasetSession dsSession, Path temporaryDirPath) throws WdkModelException {
    TRACE.start(dsSession, temporaryDirPath);
    long start = System.currentTimeMillis();
    try {
      Path localPath = getLocalCopy(dsSession, temporaryDirPath);
      return TRACE.end(Files.newInputStream(localPath));
    } catch(IOException e) {
      throw new WdkModelException(e);
    } finally {
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileContents-irods: " + getFilePath().toString(),start);
    }
  }

  public IRODSRandomAccessFile getRandomAccessFile(UserDatasetSession dsSession, long offset) throws WdkModelException {
    TRACE.start(dsSession, offset);
    long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
    IRODSRandomAccessFile irodsRandomAccessFile;
    try {
      IRODSFileFactory fileFactory = adaptor.getIrodsFileFactory();
      irodsRandomAccessFile = adaptor.getIrodsRandomAccessFile(fileFactory, getFilePath().toString());
      irodsRandomAccessFile.seek(offset,SeekWhenceType.SEEK_START);
      return TRACE.end(irodsRandomAccessFile);
    } catch (IOException ioe) {
      throw new WdkModelException("Unable to access the file " + getFilePath().toString(), ioe);
    } finally {
      QueryLogger.logEndStatementExecution("SUMMARY OF IRODS CALL","getFileContents (RA)-irods: " + getFilePath().toString(), start);
    }
  }

  @Override
  public long getFileSize() throws WdkModelException {
    return TRACE.getter(iCatData == null ? 0 : iCatData.getSize());
  }

  @Override
  protected void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException {
    TRACE.start(dsSession, tmpFile);
    long start = System.currentTimeMillis();
    IrodsUserDatasetStoreAdaptor adaptor = (IrodsUserDatasetStoreAdaptor) dsSession.getUserDatasetStoreAdaptor();
    TransferControlBlock tcb = adaptor.getTransferControlBlock();
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
    TRACE.end();
  }

  @Override
  public long readRangeInto(
    final UserDatasetSession dsSess,
    final long offset,
    final long len,
    final OutputStream into
  ) throws WdkRuntimeException {
    TRACE.start(dsSess, offset, len, into);
    if (len == 0)
      return TRACE.end(0);

    final IrodsUserDatasetStoreAdaptor adaptor;
    long wrote = 0;

    adaptor = (IrodsUserDatasetStoreAdaptor) dsSess.getUserDatasetStoreAdaptor();
    try {
      IRODSFileFactory fileFac = adaptor.getIrodsFileFactory();
      IRODSFile file = fileFac.instanceIRODSFile(getFilePath().toString());
      try (IRODSFileInputStream is = fileFac.instanceIRODSFileInputStream(file)) {
        if (file.length() <= offset)
          return TRACE.end(0);

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

    return TRACE.end(wrote);
  }
}
