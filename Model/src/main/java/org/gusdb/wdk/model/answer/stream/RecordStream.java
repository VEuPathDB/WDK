package org.gusdb.wdk.model.answer.stream;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.gusdb.wdk.model.record.RecordInstance;

public interface RecordStream extends Iterable<RecordInstance>, AutoCloseable {

  // no additional methods; just want to quantify the requirements for a RecordStream
  @Override
  void close();

  default Stream<RecordInstance> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
