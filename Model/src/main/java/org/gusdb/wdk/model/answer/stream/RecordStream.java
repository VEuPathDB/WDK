package org.gusdb.wdk.model.answer.stream;

import org.gusdb.wdk.model.record.RecordInstance;

public interface RecordStream extends Iterable<RecordInstance>, AutoCloseable {

  // no additional methods; just want to quantify the requirements for a RecordStream
  @Override
  public void close();

}
