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

  FilesysUserDatasetFile(Path filePath, long userDatasetId) {
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
  public long getFileSize() throws WdkModelException {
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
    final UserDatasetSession dsSess,
    final long offset,
    final long len,
    final OutputStream into
  ) throws WdkRuntimeException {
    if (len == 0)
      return 0;

    long wrote = 0;

    try (RandomAccessFile raf = new RandomAccessFile(getFilePath().toFile(), "r")) {
      final byte[] buffer = new byte[(int) min(32768L, len)];
      raf.seek(offset);

      for (int i = (int) ceil((double) len / buffer.length); i > 0; i--) {
        final int d = raf.read(buffer);
        into.write(buffer, 0, d);
        wrote += d;
      }
    } catch (IOException e) {
      throw new WdkRuntimeException(e);
    }

    return wrote;
  }
}
