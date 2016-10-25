package org.gusdb.wdk.model.answer.stream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.record.CsvResultList;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;

class FileBasedRecordIterator implements Iterator<RecordInstance> {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(FileBasedRecordIterator.class);

  /** Buffer size for the buffered writer use to write CSV files */
  private static final int BUFFER_SIZE = 32768;

  private boolean _isClosed = false;
  private final AnswerValue _answerValue;
  private Map<ResultList,List<ColumnAttributeField>> _attributeIteratorMap = new HashMap<>();
  private List<SingleTableRecordStream> _singleTableRecordInstanceStreams = new ArrayList<>();
  private Map<TableField, TwoTuple<Iterator<RecordInstance>, RecordInstance>> _tableIteratorMap = new HashMap<>();
  private SqlResultList _idResultList;

  /**
   * This constructor creates the SQL result list of requested ids along with setting up readers to read the attribute and table
   * data from the temporary CSV files set up initially.
   * @param answerValue
   * @param attributeFileMap
   * @param tableFileMap
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public FileBasedRecordIterator(AnswerValue answerValue, Map<Path, List<ColumnAttributeField>> attributeFileMap, Map<Path, TableField> tableFileMap) throws WdkModelException, WdkUserException {
    _answerValue = answerValue;

    // The temporary files containing table data necessarily start each row with primary key data since that is the only
    // way to distinguish between records.  We need to pass the primary key column names to the result list serving the
    // temporary file along with the table attribute names for each such file.
    List<String> primaryKeyColumnNames = new ArrayList<>();
    primaryKeyColumnNames.addAll(Arrays.asList(_answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField().getColumnRefs()));

    // Creating map keyed on the buffered reader for each file with a list of the associated attribute
    // fields as values.
    try {
      for(Path filePath : attributeFileMap.keySet()) {
        List<ColumnAttributeField> attributeFields = attributeFileMap.get(filePath);
        List<String> columnNames = new ArrayList<>();
        for(ColumnAttributeField attributeField : attributeFields) {
          columnNames.add(attributeField.getName());
        }
        ResultList attributeResultList = new CsvResultList(new BufferedReader(new FileReader(filePath.toString()), BUFFER_SIZE),columnNames, CsvResultList.TAB, CsvResultList.QUOTE, CsvResultList.ESCAPE);
        _attributeIteratorMap.put(attributeResultList,attributeFileMap.get(filePath));
      }

      // Creating a list of table iterators (one per requested table) that will provide a record instance from which the table
      // value may be extracted.
      for(Path filePath : tableFileMap.keySet()) {
        TableField tableField = tableFileMap.get(filePath);

        // Provide an ordered list of column names to pass to the CSV result list so that column data may be properly retrieved by name.
        List<String> columnNames = createTableColumnList(_answerValue, tableField);
        ResultList tableResultList = new CsvResultList(new BufferedReader(new FileReader(filePath.toString()), BUFFER_SIZE), columnNames, CsvResultList.TAB, CsvResultList.QUOTE, CsvResultList.ESCAPE);

        // Create a SingleTableRecordInstanceStream object, passing in the table field and the CSV result list created above.
        SingleTableRecordStream singleTableRecordInstanceStream = new SingleTableRecordStream(_answerValue, tableFileMap.get(filePath), tableResultList);

        // Need to keep the handle to the object so it can be closed later.
        _singleTableRecordInstanceStreams.add(singleTableRecordInstanceStream);
        Iterator<RecordInstance> tableIterator = singleTableRecordInstanceStream.iterator();
        _tableIteratorMap.put(tableField, new TwoTuple<Iterator<RecordInstance>,RecordInstance>(tableIterator,tableIterator.hasNext() ? tableIterator.next() : null));
      }

      // Getting the paged id SQL.  This same paged id SQL was used in the attribute SQL.  So
      // the primary keys should correspond 1:1 with the rows in the attribute CSV files.
      String sql = _answerValue.getPagedIdSql();

      // Obtain the result list of primary keys
      DataSource dataSource = _answerValue.getQuestion().getWdkModel().getAppDb().getDataSource();
      _idResultList = new SqlResultList(SqlUtils.executeQuery(dataSource, sql, "id__attr-full"));
    }
    catch(IOException | SQLException e) {
      throw new WdkModelException(e.getMessage());
    }
  }

  /**
   * Culls all the column attribute fields from the table field and returns a list of primary key column names
   * and column attribute column names.
   * 
   * @param answerValue
   * @param table
   *          - the given table field
   * @return ordered list of primary key column names and table column attribute column names.
   * @throws WdkModelException
   */
  private static List<String> createTableColumnList(AnswerValue answerValue, TableField table)
      throws WdkModelException {

    // Collect together all the columns representing the primary key attribute fields.
    PrimaryKeyAttributeField pkField = answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField();
    String[] pkColumns = pkField.getColumnRefs();

    // Collect together all the columns representing the column attribute fields to be included
    List<String> attributeColumns = new ArrayList<>();
    List<ColumnAttributeField> fields = FileBasedRecordStream.filterColumnAttributeFields(Arrays.asList(table.getAttributeFields()), true);
    for (ColumnAttributeField field : fields) {
      attributeColumns.add(field.getColumn().getName());
    }

    // Combine the primary key columns and column attribute columns into a single list
    List<String> columnNames = new ArrayList<>(Arrays.asList(pkColumns));
    columnNames.addAll(attributeColumns);

    return columnNames;
  }

  /**
   * Bumps the id result list cursor to the next SQL record.
   */
  @Override
  //TODO investigate FgpUtil method that does the caching - implement Cursor
  public boolean hasNext() {
    checkClosed();
    try {
      return _idResultList.next();
    }
    catch(WdkModelException wme) {
      throw new RuntimeException(wme.getMessage());
    }
  }

  /**
   * Identifies the current primary key data and uses that along with attribute and table temporary files to
   * construct and return a record instance containing the attribute and table information requested.
   */
  @Override
  public RecordInstance next() {
    checkClosed();
    try {
      // Construct the primary key values
      PrimaryKeyAttributeField pkField = _answerValue.getQuestion().getRecordClass().getPrimaryKeyAttributeField();
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      for (String column : pkField.getColumnRefs()) {
        Object value = _idResultList.get(column);
        pkValues.put(column, value);
      }
      
      // Create and populate a temporary aggregate record instance
      return createRecordInstance(_attributeIteratorMap, _tableIteratorMap, pkValues);
    }
    catch(WdkUserException | WdkModelException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Closing the attribute CSV result lists, the table CSV result list, the id SQL result list and
   * the single table record instance streams used to populate the record instance tables.
   */
  void close() {
    _isClosed = true;
    for(SingleTableRecordStream singleTableRecordInstanceStream : _singleTableRecordInstanceStreams) {
      singleTableRecordInstanceStream.close();
    }
    try {
      if(_idResultList != null) {
        _idResultList.close();
      }
      for(ResultList resultList : _attributeIteratorMap.keySet()) {
        resultList.close();
      }
    }
    catch(WdkModelException wme) {
      throw new RuntimeException(wme.getMessage());
    }
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

  /**
   * Creates a new record instance for the given primary key and populates it from the input files represented
   * here by the map of buffered readers.
   * @param attributeIteratorMap - maps result list for each attribute temporary file to the list of attribute fields it contains.
   * @param tableIteratorMap - maps table name for each table requested to a tuple containing a single table record instance
   * stream iterator and the last record instance returned by that iterator.
   * @param pkValues - The values for the primary key for each a record instance is to be created.
   * @return - a record instance that aggregates all the requested attributes and tables.
   * @throws WdkModelException
   * @throws WdkUserException 
   */
  protected RecordInstance createRecordInstance(Map<ResultList,List<ColumnAttributeField>> attributeIteratorMap,
      Map<TableField,TwoTuple<Iterator<RecordInstance>,RecordInstance>> tableIteratorMap,
      Map<String,Object> pkValues) throws WdkModelException, WdkUserException {

    // Create a new record instance that will contain all the requested column attributes and tables for the provided primary key values  
    DynamicRecordInstance aggregateRecordInstance = new DynamicRecordInstance(_answerValue, pkValues); 

    // Iterate over all the CSV file result lists.  Note that the next item in the result list should correspond
    // to the primary key values provided.  There is no primary key information in the attribute CSV files themselves.
    for(ResultList resultList : attributeIteratorMap.keySet()) {
      // There should always be a row in the result list for the primary key given.  The conditional should always
      // be satisfied.  Since primary keys are not included in the attribute CSV files, no direct comparison can be made.
      if(resultList.next()) {
        for(AttributeField field : attributeIteratorMap.get(resultList)) {
          if(field instanceof ColumnAttributeField) {
            AttributeValue attributeValue = new ColumnAttributeValue((ColumnAttributeField) field, resultList.get(field.getName()));
            aggregateRecordInstance.addAttributeValue(attributeValue);
          }
        }
      }
      else {
        throw new WdkModelException("Mismatch between number of primary keys and a CSV file length.");
      }
    }

    // Iterate over all the tables for which iterators exist and pull out the iterator and the next record instance generated from
    // that iterator.
    for(TableField tableField : tableIteratorMap.keySet()) {
      attachTable(tableField, tableIteratorMap, aggregateRecordInstance);
    }
    return aggregateRecordInstance;
  }

  /**
   * For the provided table, determine whether the next record instance returned by the SingleTableRecordInstanceStream object is the same
   * as the record instance being assembled (by comparing pk values).  If they are the same, copy to values for the subject
   * table into the record instance being assembled, advance the SingleTableRecordInstanceStream iterator and save it and the resulting new
   * next record instance into the tuple kept in the tableIteratorMap.  Otherwise, just attach an empty version of the provided table
   * to the record instance being assembled.
   * @param tableName - name of the table to be attached to the record class under assembly
   * @param tableIteratorMap - map of the name of each table to be included in the record instance under assembly to a tuple containing the current state of
   * the SignleTableRecordInstanceStream iterator and the next record instance to be evaluated.
   * @param aggregateRecordInstance - the record instance under assembly
   * @throws WdkModelException
   * @throws WdkUserException
   */
  protected void attachTable(TableField tableField,
      Map<TableField,TwoTuple<Iterator<RecordInstance>,RecordInstance>> tableIteratorMap,
      DynamicRecordInstance aggregateRecordInstance) throws WdkModelException, WdkUserException {
    TwoTuple<Iterator<RecordInstance>,RecordInstance> twoTuple = tableIteratorMap.get(tableField);
    Iterator<RecordInstance> iterator = twoTuple.getFirst();
    RecordInstance nextRecordInstance = twoTuple.getSecond();
    TableValue tableValue = null;
    // If the next record instance for a given table is null, no current or subsequent record instances will have this table, in
    // which case, we need to create an empty one (to avoid any lazy db calls)
    if(nextRecordInstance != null) {
      // If the primary keys of next record instance for a given table do not match the keys of the aggregate record instance,
      // the next record instance applies to an aggregate record instance that is as yet downstream, in which case, create an
      // empty table value.
      Map<String,Object> aggregatePkValues = aggregateRecordInstance.getPrimaryKey().getRawValues();
      Map<String,Object> nextPkValues = nextRecordInstance.getPrimaryKey().getRawValues();
      if(!PrimaryKeyAttributeValue.rawValuesDiffer(aggregatePkValues, nextPkValues)) {
        // Collect the table value from the next record instance to be applied to the aggregate record instance.
        tableValue = nextRecordInstance.getTableValue(tableField.getName());
        nextRecordInstance = iterator.hasNext() ? iterator.next() : null;
        twoTuple.set(iterator,nextRecordInstance);
      }
      else {
        tableValue = createEmptyTableValue(tableField,aggregateRecordInstance.getPrimaryKey());
      }
    }
    else {
      tableValue = createEmptyTableValue(tableField,aggregateRecordInstance.getPrimaryKey());
    }
    aggregateRecordInstance.addTableValue(tableValue);
  }

  /**
   * Returns a empty table value for the provided table field and primary key
   * @param primaryKey
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  protected TableValue createEmptyTableValue(TableField tableField, PrimaryKeyAttributeValue primaryKey)
      throws WdkModelException, WdkUserException {
    return new TableValue(null, primaryKey, tableField, true);
  }
}
