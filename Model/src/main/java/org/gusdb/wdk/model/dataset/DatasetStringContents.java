package org.gusdb.wdk.model.dataset;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.wdk.model.WdkModelException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class DatasetStringContents extends DatasetContents {
  private final String contents;

  public DatasetStringContents(final String fileName, final String contents) {
    super(fileName);
    this.contents = contents;
  }

  @Override
  public String getChecksum() {
    return EncryptionUtil.encrypt(contents);
  }

  @Override
  public Reader getContentReader() {
    return new StringReader(contents);
  }
}
