package org.gusdb.wdk.model.answer.stream;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.iterator.ReadOnlyIterator;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public abstract class AbstractRecordIterator extends ReadOnlyIterator<RecordInstance> implements AutoCloseable {

  private static final Logger LOG = Logger.getLogger(AbstractRecordIterator.class);

  /**
   * This method should call next() on the passed result list.  If next() returns
   * false, then the method should return null after performing any additional
   * desired logic.  If next() returns true, then the method should create a
   * RecordInstance using the current result row and return it.
   * 
   * @param resultList result list from which to create the next record
   * @return generated RecordInstance from this row, or null if no more rows
   * @throws WdkModelException 
   * @throws WdkUserException 
   */
  protected abstract RecordInstance createNextRecordInstance(ResultList resultList) throws WdkModelException, WdkUserException;

  // answer value source of these records
  private final AnswerValue _answerValue;

  // cache values needed to create each record
  private final RecordClass _recordClass;
  private final Map<String, AttributeField> _attributeFieldMap;

  // id query result list
  private final SqlResultList _resultList;

  // last record loaded (will be held in "cache" until next() delivers it)
  private RecordInstance _lastRecord = null;

  // whether this iterator is closed; if closed, next and hasNext will fail
  private boolean _isClosed = false;

  protected AbstractRecordIterator(AnswerValue answerValue, SqlResultList resultList) {
    _answerValue = answerValue;
    _resultList = resultList;
    // cache values looked up for each row
    Question question = _answerValue.getQuestion();
    _recordClass = question.getRecordClass();
    _attributeFieldMap = question.getAttributeFieldMap();
  }

  protected void performAdditionalClosingOps() {
    // by default, no addition closing operations
  }

  protected StaticRecordInstance createInstanceTemplate(ResultList resultList) throws WdkModelException {
    // Create a new instance containing the current row's PK values
    PrimaryKeyValue pkValue = new PrimaryKeyValue(_recordClass.getPrimaryKeyDefinition(), resultList);
    return new StaticRecordInstance(_recordClass, pkValue, _attributeFieldMap);
  }

  /**
   * Bumps the id result list cursor to the next SQL record.
   */
  @Override
  public boolean hasNext() {
    checkClosed();
    try {
      if (_lastRecord != null) return true;
      _lastRecord = createNextRecordInstance(_resultList);
      return (_lastRecord != null);
    }
    catch(WdkModelException | WdkUserException e) {
      LOG.error("Failed to load next record", e);
      throw new WdkRuntimeException("Failed to load next record", e);
    }
  }

  /**
   * Identifies the current primary key data and uses that along with attribute and table temporary files to
   * construct and return a record instance containing the attribute and table information requested.
   */
  @Override
  public RecordInstance next() {
    checkClosed();
    if (!hasNext()) {
      throw new NoSuchElementException("No more record instances in this iterator.");
    }
    RecordInstance nextRecord = _lastRecord;
    _lastRecord = null;
    return nextRecord;
  }

  /**
   * This method should close "quietly" (i.e. not throw exception); only a
   * best-effort attempt to close resources is required.
   */
  @Override
  public void close() {
    if (_isClosed) return;
    _isClosed = true;
    try {
      performAdditionalClosingOps();
    }
    catch(Exception e) {
      LOG.error("Unable to close additional resources of class " + getClass().getName(), e);
    }
    try {
      _resultList.close();
    }
    catch(WdkModelException e) {
      LOG.error("Unable to close SqlResultList.  This may be a connection leak.", e);
    }
  }

  private void checkClosed() {
    if (_isClosed) {
      throw new ConcurrentModificationException("This iterator has been closed.");
    }
  }
}
