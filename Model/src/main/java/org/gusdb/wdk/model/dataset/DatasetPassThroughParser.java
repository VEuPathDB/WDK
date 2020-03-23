package org.gusdb.wdk.model.dataset;

import java.util.Iterator;

public class DatasetPassThroughParser extends AbstractDatasetParser {

  private final Iterable<String> passthrough;

  public DatasetPassThroughParser(final Iterable<String> passthrough) {
    this.passthrough = passthrough;
    setName("anonymous");
  }

  @Override
  public DatasetIterator iterator(DatasetContents contents) {
    return new DatasetIterator() {
      private Iterator<String> it = passthrough.iterator();

      @Override public String[] next()    { return new String[] { it.next() }; }
      @Override public boolean  hasNext() { return it.hasNext(); }
    };
  }
}
