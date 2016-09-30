package org.gusdb.wdk.model.answer.stream;

import static org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue.rawValuesDiffer;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
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

public class SingleTableRecordInstanceStream implements RecordStream {

  private static final Logger LOG = Logger.getLogger(SingleTableRecordInstanceStream.class);

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

  private void init(AnswerValue answerValue, TableField tableField, ResultList resultList) {
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
        Map<String, Object> firstPk = AnswerValue.getPrimaryKeyFromResultList(_resultList, _pkField).getRawValues();
        LOG.debug("First row PK = " + FormatUtil.prettyPrint(firstPk));
        _lastPkValues.set(firstPk);
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
            Map<String, Object> currentRecordPkValues = _lastPkValues.get();
            RecordInstance record = new RecordInstance(_answerValue, currentRecordPkValues);
            TableValue tableValue = new TableValue(_answerValue.getUser(), record.getPrimaryKey(), _tableField, true);
            record.addTableValue(tableValue);
  
            // add the row from the last call to next and clear lastPkValues
            tableValue.initializeRow(_resultList);
            int rows = 1;
            _lastPkValues.set(null); // will reset if there is another record after this one

            // loop through the ResultList's rows and add to table until PK values differ
            while (_resultList.next()) {
              Map<String,Object> rowPkValues = AnswerValue.getPrimaryKeyFromResultList(_resultList, _pkField).getRawValues();
              LOG.debug("Will compare previous with " + FormatUtil.prettyPrint(rowPkValues));
              if (rawValuesDiffer(currentRecordPkValues, rowPkValues)) {
                // save off next record's primary keys and return this record
                LOG.debug("New record's table; save off new record PKs and return record having table of size " + rows);
                _lastPkValues.set(rowPkValues);
                return record;
              }
              // otherwise add row to table value
              tableValue.initializeRow(_resultList);
              rows++;
              LOG.debug("Added row to current record's table.  Now " + rows + " rows.");
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
