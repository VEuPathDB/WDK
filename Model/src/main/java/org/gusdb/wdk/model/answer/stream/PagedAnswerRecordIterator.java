package org.gusdb.wdk.model.answer.stream;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;

public class PagedAnswerRecordIterator implements Iterator<RecordInstance> {

  private final Iterator<AnswerValue> _answerValueIterator;
  private Iterator<RecordInstance> _currentPage;

  PagedAnswerRecordIterator(Iterator<AnswerValue> answerValueIterator) {
    _answerValueIterator = answerValueIterator;
  }

  @Override
  public boolean hasNext() {
    if (_currentPage != null && _currentPage.hasNext()) {
      return true;
    }
    try {
      while (_answerValueIterator.hasNext()) {
        AnswerValue answerValue = _answerValueIterator.next();
        RecordInstance[] nextPageRecords = answerValue.getRecordInstances();
        if (nextPageRecords.length > 0) {
          _currentPage = Arrays.asList(nextPageRecords).iterator();
          return true;
        }
      }
      return false;
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to get next page of records from answer value", e);
    }
  }

  @Override
  public RecordInstance next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more record instances in this iterator.");
    }
    return _currentPage.next();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove RecordInstances using this iterator.");
  }
}
