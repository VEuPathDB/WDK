package org.gusdb.wdk.model.dataset;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

import java.io.*;

public class DatasetFileContents extends DatasetContents {
  private static final int BUF_SIZE = 8192;

  private final File contents;
  private String checksum;

  public DatasetFileContents(
    final String fileName,
    final File contents
  ) {
    super(fileName);
    this.contents = contents;
  }

  @Override
  public String getChecksum() {
    if (checksum != null)
      return checksum;

    return checksum = genChecksum(contents);
  }

  @Override
  public Reader getContentReader() throws WdkModelException {
    try {
      return new FileReader(contents);
    } catch (FileNotFoundException e) {
      throw new WdkModelException(e);
    }
  }

  private static String genChecksum(final File file) {
    try {
      var dig  = EncryptionUtil.newMd5Digester();
      var str  = new BufferedInputStream(new FileInputStream(file), BUF_SIZE);
      var buf  = new byte[BUF_SIZE];

      var read = 0;
      read = str.read(buf);
      while (read > -1) {
        dig.update(buf, 0, read);
        read = str.read(buf);
      }

      str.close();
      return EncryptionUtil.convertToHex(dig.digest(), true);
    } catch (IOException e) {
      throw new WdkRuntimeException(e);
    }
  }
}
