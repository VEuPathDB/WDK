package org.gusdb.wdk.model.dataset;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.wdk.model.WdkRuntimeException;

import java.io.*;
import java.util.List;

public class DatasetListContents extends DatasetContents {

  private final List<String> idList;
  private String checksum;

  public DatasetListContents(final List<String> idList) {
    super(null);
    this.idList = idList;
  }

  
  @Override
  public String getChecksum() {
    if (checksum != null)
      return checksum;

    return checksum = genChecksum();
  }

  @Override
  public Reader getContentReader() {
    return new ListReader();
  }

  private String genChecksum() {
    try {
      var dig  = EncryptionUtil.newMd5Digester();
      var str  = getContentReader();
      var buf  = new char[1024];

      var read = str.read(buf);
      while (read > -1) {
        dig.update(new String(buf, 0, read).getBytes());
        read = str.read(buf);
      }

      str.close();
      return EncryptionUtil.convertToHex(dig.digest(), true);
    } catch (IOException e) {
      throw new WdkRuntimeException(e);
    }
  }

  /**
   * Reader implementation for reading from the idList prop.
   */
  private class ListReader extends Reader {
    private int index;
    private int offset;
    private boolean done;
    private boolean sep;

    @Override
    @SuppressWarnings("NullableProblems")
    public int read(char[] cbuf, int off, int len) {
      //noinspection SynchronizeOnNonFinalField
      synchronized (lock) {
        if (len < 0 || off < 0 || off + len > cbuf.length)
          throw new IndexOutOfBoundsException();
        if (len == 0)
          return 0;

        if (done)
          return -1;

        var read = 0;
        var pos = off;

        while (!done && read < len) {
          cbuf[pos++] = next();
          read++;
        }

        return read;
      }
    }

    @Override
    public void close() {}

    private char next() {
      if (sep) {
        sep = false;
        return ' ';
      }

      var out = idList.get(index).charAt(offset);
      inc();
      return out;
    }

    private void inc() {
      // More characters in this list entry
      if (offset + 1 < idList.get(index).length()) {
        offset++;
        return;
      }

      // More list entries
      if (index + 1 < idList.size()) {
        index++;
        offset = 0;
        sep = true;
        return;
      }

      done = true;
    }
  }

  @Override
  public long getEstimatedRowCount() {
    return idList.size();
  }
}
