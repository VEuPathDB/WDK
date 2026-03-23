package org.gusdb.wdk.model.answer.stream;

import static org.gusdb.wdk.model.record.PrimaryKeyValue.rawValuesDiffer;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.iterator.ReadOnlyIterator;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;

public class SingleTableRecordStream implements RecordStream {

  private static final Logger LOG = Logger.getLogger(SingleTableRecordStream.class);

  private AnswerValue _answerValue;
  private TableField _tableField;
  private PrimaryKeyDefinition _pkDef;
  private Wrapper<Map<String,Object>> _lastPkValues;
  private ResultList _resultList;
  private boolean _iteratorCalled;

  public SingleTableRecordStream(AnswerValue answerValue, TableField tableField) throws WdkModelException {
    init(answerValue, tableField, answerValue.getTableFieldResultList(tableField));
  }

  public SingleTableRecordStream(AnswerValue answerValue, TableField tableField, String customTableSql) throws WdkModelException {
    init(answerValue, tableField, answerValue.getTableFieldResultList(tableField, customTableSql));
  }

  public SingleTableRecordStream(AnswerValue answerValue, TableField tableField, ResultList resultList) {
    init(answerValue, tableField, resultList);
  }

  private void init(AnswerValue answerValue, TableField tableField, ResultList resultList) {
    _answerValue = answerValue;
    _tableField = tableField;
    _pkDef = _answerValue.getQuestion().getRecordClass().getPrimaryKeyDefinition();
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
        Map<String, Object> firstPk = new PrimaryKeyValue(_pkDef, _resultList).getRawValues();
        //LOG.debug("RecordInstance Iterator(): First row PK = " + FormatUtil.prettyPrint(firstPk));
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
            Question question = _answerValue.getQuestion();
            StaticRecordInstance record = new StaticRecordInstance(_answerValue.getRequestingUser(),
                question.getRecordClass(), question, currentRecordPkValues, false);
            TableValue tableValue = new TableValue(_tableField);
            record.addTableValue(tableValue);
  
            // add the row from the last call to next and clear lastPkValues
            tableValue.initializeRow(_resultList);
            _lastPkValues.set(null); // will reset if there is another record after this one

            // loop through the ResultList's rows and add to table until PK values differ
            int rowCount = 0;
            Timer t = new Timer();
            Integer maxRows = _answerValue.getWdkModel().getModelConfig().getMaxTableValueRows();
            while (_resultList.next()) {
              Map<String,Object> rowPkValues = new PrimaryKeyValue(_pkDef, _resultList).getRawValues();
              if (rawValuesDiffer(currentRecordPkValues, rowPkValues)) {
                // save off next record's primary keys and return this record
                _lastPkValues.set(rowPkValues);
                return record;
              }
              // otherwise add row to table value
              rowCount++;
              LOG.trace("Row " + rowCount + ": fetched in " + t.getElapsedStringAndRestart());
              if (rowCount > maxRows)
                throw new WdkRuntimeException("Table query " + _tableField.getQueryRef() +
                    " returned too many (>" + maxRows + ") rows.");
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
