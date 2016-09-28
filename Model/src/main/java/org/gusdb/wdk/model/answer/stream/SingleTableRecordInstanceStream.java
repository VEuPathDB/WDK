package org.gusdb.wdk.model.answer.stream;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.iterator.ReadOnlyIterator;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;

public class SingleTableRecordInstanceStream implements RecordStream {

  private AnswerValue _answerValue;
  private TableField _tableField;
  private PrimaryKeyAttributeField _pkField;
  private Wrapper<Map<String,Object>> _lastPkValues;
  private ResultList _resultList;
  private boolean _iteratorCalled;

  public SingleTableRecordInstanceStream(AnswerValue answerValue, TableField tableField) throws WdkModelException {
    try {
      init(answerValue, tableField, answerValue.getTableFieldResultList(tableField));
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Could not initialize single-table record stream", e);
    }
  }

  public SingleTableRecordInstanceStream(AnswerValue answerValue, TableField tableField, ResultList resultList) {
    init(answerValue, tableField, resultList);
  }

  public void init(AnswerValue answerValue, TableField tableField, ResultList resultList) {
    _answerValue = answerValue;
    _tableField = tableField;
    _pkField = _answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField();
    _lastPkValues = new Wrapper<>();
    _resultList = resultList;
    _iteratorCalled = false;
  }

  @Override
  public Iterator<RecordInstance> iterator() {
    if (_iteratorCalled) {
      throw new IllegalStateException("The iterator() method can be called only once on each instance.");
    }
    _iteratorCalled = true;
    try {
      if (_resultList.next()) {
        _lastPkValues.set(AnswerValue.getPrimaryKeyFromResultList(_resultList, _pkField).getRawValues());
      }
      return new ReadOnlyIterator<RecordInstance>() {
        @Override
        public boolean hasNext() {
          return _lastPkValues.get() != null;
        }
        @Override
        public RecordInstance next() {
          if (!hasNext()) {
            throw new NoSuchElementException("No more records exist in this stream.");
          }

          try {
            // start new record instance
            Map<String, Object> recordPrimaryKeyValues = _lastPkValues.get();
            RecordInstance record = new RecordInstance(_answerValue, recordPrimaryKeyValues);
            record.getPrimaryKey().setValueContainer(record); // TODO: explore why we have to do this
            TableValue tableValue = new TableValue(_answerValue.getUser(), record.getPrimaryKey(), _tableField, true);
            record.addTableValue(tableValue);
  
            // add the row from the last call to next and clear lastPkValues
            tableValue.initializeRow(_resultList);
            _lastPkValues.set(null); // will reset if there is another record after this one
            
            // loop through the ResultList's rows and add to table until PK values differ
            while (_resultList.next()) {
              PrimaryKeyAttributeValue rowPrimaryKey = AnswerValue.getPrimaryKeyFromResultList(_resultList, _pkField);
              Map<String,Object> newPkValues = rowPrimaryKey.getRawValues();
              if (pksDiffer(recordPrimaryKeyValues, newPkValues)) {
                // save off next record's primary keys and return this record
                _lastPkValues.set(newPkValues);
                return record;
              }
              // otherwise add row to table value
              tableValue.initializeRow(_resultList);
            }
  
            // if didn't already return, then this was the last record
            return record;
          }
          catch (WdkModelException | WdkUserException e) {
            throw new WdkRuntimeException("Unable to produce next single-table record", e);
          }
        }
      };
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to initialize single-table record iterator", e);
    }
  }

  private static boolean pksDiffer(Map<String, Object> map1, Map<String, Object> map2) {
    if (map1.size() != map2.size()) return false;
    for (String key : map1.keySet()) {
      if (!map2.containsKey(key)) return false;
      if (!map2.get(key).equals(map1.get(key))) return false;
    }
    return true;
  }

  @Override
  public void close() {
    try {
      _resultList.close();
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to close ResultList for single-table result stream", e);
    }
  }
}
