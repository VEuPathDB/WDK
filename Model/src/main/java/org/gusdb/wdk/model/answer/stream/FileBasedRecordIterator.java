package org.gusdb.wdk.model.answer.stream;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.gusdb.wdk.model.record.RecordInstance;

class FileBasedRecordIterator implements Iterator<RecordInstance> {

  private boolean _isClosed = false;

  @Override
  public boolean hasNext() {
    checkClosed();
    // TODO implement
    return false;
  }

  @Override
  public RecordInstance next() {
    checkClosed();
    // TODO implement
    return null;
  }

  void close() {
    _isClosed = true;
    // TODO: wait for current calls to next() and hasNext() to complete, then close any open files
  }

  private void checkClosed() {
    if (_isClosed) {
      throw new ConcurrentModificationException("This iterator has been closed.");
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Unable to remove RecordInstances using this iterator.");
  }
}
