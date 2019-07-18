package org.gusdb.wdk.model.user.dataset.filesys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

public class FilesysUserDatasetFile extends UserDatasetFile {


  public FilesysUserDatasetFile(Path filePath, Long userDatasetId) {
    super(filePath, userDatasetId);
  }

  @Override
  public InputStream getFileContents(UserDatasetSession dsSession, Path path) throws WdkModelException {
    try {
      return Files.newInputStream(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  protected Long readFileSize(UserDatasetSession dsSession) throws WdkModelException {
    try {
      return Files.size(getFilePath());
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @Override
  protected void createLocalCopy(UserDatasetSession dsSession, Path tmpFile) throws WdkModelException {
    try {
      Files.copy(getFilePath(), tmpFile);
    } catch (IOException e) {
      throw new WdkModelException(e);
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

    long wrote = 0;

    try {
      final RandomAccessFile raf = new RandomAccessFile(getFilePath().toFile(), "r");
      final byte[] buffer = new byte[(int) min(32768L, len)];

      try {
        raf.seek(offset);

        for (int i = (int) ceil((double) len / buffer.length); i < 0; i--) {
          final int d = raf.read(buffer);
          into.write(buffer, 0, d);
          wrote += d;
        }
      } finally {
        raf.close();
      }
    } catch (IOException e) {
      throw new WdkRuntimeException(e);
    }

    return wrote;
  }
}
