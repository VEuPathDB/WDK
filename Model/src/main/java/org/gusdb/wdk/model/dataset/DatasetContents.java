package org.gusdb.wdk.model.dataset;

import org.gusdb.wdk.model.WdkModelException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public abstract class DatasetContents {

  private static final int BUF_SIZE = 8192;

  // constants used to estimate number of records
  public static final int ESTIMATED_CHARS_PER_ID = 10;
  public static final int ESTIMATED_BYTES_PER_ID = 15;

  protected final String fileName;

  protected DatasetContents(String fileName) {
    this.fileName = fileName;
  }

  public String getUploadFileName() {
    return fileName;
  }

  public abstract long getEstimatedRowCount();

  @SuppressWarnings("ThrowFromFinallyBlock")
  public String truncate(final int len) throws WdkModelException {
    Reader reader = null;
    try {
      reader = getContentReader();

      var size   = Math.min(len, BUF_SIZE);
      var buf    = new char[size];
      var read   = reader.read(buf, 0, size);
      var out    = new StringBuilder();
      var total  = read;

      while (read > 0 && total < len) {
        out.append(buf, 0, read);
        size = Math.min(len - total, size);
        total += read = reader.read(buf, 0, size);
      }

      if (total >= len)
        out.append("...");

      return out.toString();
    } catch (IOException e) {
      throw new WdkModelException(e);
    } finally {
      if (reader != null) {
        try { reader.close(); }
        catch (IOException e) { throw new WdkModelException(e); }
      }
    }
  }

  public abstract String getChecksum();

  public abstract Reader getContentReader() throws WdkModelException;

  public InputStream getContentStream() throws WdkModelException {
    final var reader = getContentReader();
    final var buf    = new char[1];
    return new InputStream() {
      @Override
      public int read() throws IOException {
        return reader.read(buf, 0, 1) == -1 ? -1 : buf[0];
      }
    };
  }
}
