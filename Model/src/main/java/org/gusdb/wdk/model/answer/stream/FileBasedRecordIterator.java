package org.gusdb.wdk.model.answer.stream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.fgputil.AutoCloseableList;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.Named;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.record.CsvResultList;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.StaticRecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

class FileBasedRecordIterator extends AbstractRecordIterator {

  /** Buffer size for the buffered writer use to write CSV files */
  private static final int BUFFER_SIZE = 32768;

  /**
   * Encapsulates a ResultList from an attributes query temp file and
   * the attributes that can be accessed from it
   */
  private static class AttributeRowStreamData implements AutoCloseable {

    public final String fileName;
    public final ResultList resultList;
    public final List<QueryColumnAttributeField> attributes;

    public AttributeRowStreamData(String filename, ResultList resultList, List<QueryColumnAttributeField> attributes) {
      this.fileName = filename;
      this.resultList = resultList;
      this.attributes = attributes;
    }

    @Override
    public void close() throws Exception {
      resultList.close();
    }
  }

  /**
   * Encapsulates objects needed to manage populating a "full"
   * record stream with tables served from a single table record stream
   */
  private static class TableRecordStreamData implements AutoCloseable{

    public final String fileName;
    public final TableField field;
    public final SingleTableRecordStream stream;
    public final Iterator<RecordInstance> iterator;
    public RecordInstance lastRecord;

    public TableRecordStreamData(String sourceFileName, TableField tableField, SingleTableRecordStream recordStream) {
      fileName = sourceFileName;
      field = tableField;
      stream = recordStream;
      iterator = stream.iterator();
      lastRecord = iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void close() throws Exception {
      stream.close();
    }
  }

  // list of data to populate attributes
  private final AutoCloseableList<AttributeRowStreamData> _attributeIteratorList;

  // list of data to populate tables
  private final AutoCloseableList<TableRecordStreamData> _tableIteratorList;

  /**
   * This constructor creates the SQL result list of requested ids along with setting up readers to read the attribute and table
   * data from the temporary CSV files set up initially.
   * @param answerValue
   * @param attributeFileMap
   * @param tableFileMap
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public FileBasedRecordIterator(AnswerValue answerValue,
      Map<Path, List<QueryColumnAttributeField>> attributeFileMap,
      Map<Path, TwoTuple<TableField,List<String>>> tableFileMap)
          throws WdkModelException, WdkUserException {
    // Obtain the result list of primary keys from the raw paged id SQL.  This same paged id SQL was used in
    // the attribute SQL.  So the primary keys should correspond 1:1 with the rows in the attribute CSV files.
    super(answerValue, makeIdResultList(answerValue));
    try {
      _attributeIteratorList = initializeAttributeProviders(answerValue, attributeFileMap);
      _tableIteratorList = initializeTableProviders(answerValue, tableFileMap);
    }
    catch (IOException e) {
      close();
      throw new WdkModelException("Unable to create FileBasedRecordIterator", e);
    }
  }

  private static SqlResultList makeIdResultList(AnswerValue answerValue) throws WdkModelException {
    try {
      return new SqlResultList(SqlUtils.executeQuery(
          answerValue.getQuestion().getWdkModel().getAppDb().getDataSource(),
          answerValue.getPagedIdSql(), "id__attr-full"));
    }
    catch (WdkUserException | SQLException e) {
      throw new WdkModelException("Unable to run ID SQL", e);
    }
  }

  private static AutoCloseableList<AttributeRowStreamData> initializeAttributeProviders(AnswerValue answerValue,
      Map<Path, List<QueryColumnAttributeField>> attributeFileMap) throws WdkModelException, FileNotFoundException {
    AutoCloseableList<AttributeRowStreamData> attributeIteratorList = new AutoCloseableList<>();
    for (Entry<Path,List<QueryColumnAttributeField>> entry : attributeFileMap.entrySet()) {

      // Generate full list of columns to fetch, including both PK columns and requested columns
      List<String> columnNames = new ListBuilder<String>(answerValue.getQuestion()
          .getRecordClass().getPrimaryKeyDefinition().getColumnRefs())
          .addAll(Functions.mapToList(entry.getValue(), Named.TO_NAME))
          .toList();

      ResultList csvResultList = new CsvResultList(
          new BufferedReader(new FileReader(entry.getKey().toString()), BUFFER_SIZE), columnNames,
          CsvResultList.TAB, CsvResultList.QUOTE, CsvResultList.ESCAPE);

      attributeIteratorList.add(new AttributeRowStreamData(
          entry.getKey().getFileName().toString(), csvResultList, entry.getValue()));
    }
    return attributeIteratorList;
  }

  // Creating a list of table iterators (one per requested table) that will provide a record
  // instance from which the table value may be extracted.
  private static AutoCloseableList<TableRecordStreamData> initializeTableProviders(AnswerValue answerValue,
      Map<Path,TwoTuple<TableField, List<String>> > tableFileMap) throws WdkModelException, FileNotFoundException {
    AutoCloseableList<TableRecordStreamData> tableIteratorList = new AutoCloseableList<>();
    for (Entry<Path,TwoTuple<TableField, List<String>>> entry : tableFileMap.entrySet()) {

      // Provide an ordered list of column names to pass to the CSV result list so that column data may be properly retrieved by name.
      ResultList tableResultList = new CsvResultList(
          new BufferedReader(new FileReader(entry.getKey().toString()), BUFFER_SIZE),
          entry.getValue().getSecond(), CsvResultList.TAB, CsvResultList.QUOTE, CsvResultList.ESCAPE);

      // Create a SingleTableRecordInstanceStream object, passing in the table field and the CSV result list created above.
      SingleTableRecordStream singleTableRecordInstanceStream = new SingleTableRecordStream(
          answerValue, entry.getValue().getFirst(), tableResultList);

      tableIteratorList.add(new TableRecordStreamData(
          entry.getKey().getFileName().toString(), entry.getValue().getFirst(), singleTableRecordInstanceStream));
    }
    return tableIteratorList;
  }



  /**
   * Creates a new record instance for the given primary key and populates it from the input files represented
   * here by the map of buffered readers.
   * @throws WdkModelException 
   * @throws WdkUserException 
   */
  @Override
  protected RecordInstance createNextRecordInstance(ResultList idResultList) throws WdkModelException, WdkUserException {

    if (!idResultList.next()) {
      // no more records. make sure our CSV files don't still have data; if so, it's an error
      checkForExtraData(_attributeIteratorList, _tableIteratorList);
      return null;
    }

    // Create a new record instance that will contain all the requested column attributes
    //   and tables for the provided primary key values
    StaticRecordInstance aggregateRecordInstance = createInstanceTemplate(idResultList);
    PrimaryKeyDefinition pkDef = aggregateRecordInstance.getRecordClass().getPrimaryKeyDefinition();
    Map<String,Object> pkValues = aggregateRecordInstance.getPrimaryKey().getRawValues();

    // Iterate over all the CSV file result lists.  Note that the next item in the
    //   result list should correspond to the primary key values provided.  There is
    //   no primary key information in the attribute CSV files themselves.
    for (AttributeRowStreamData attributeData : _attributeIteratorList) {
      attachAttributes(aggregateRecordInstance, attributeData, pkDef, pkValues);
    }

    // Iterate over all the tables for which iterators exist and pull out the iterator and the next record
    // instance generated from that iterator.
    for (TableRecordStreamData tableData : _tableIteratorList) {
      attachTable(aggregateRecordInstance, tableData, pkValues);
    }

    return aggregateRecordInstance;
  }

  private void checkForExtraData(AutoCloseableList<AttributeRowStreamData> attributeIteratorList,
      AutoCloseableList<TableRecordStreamData> tableIteratorList) throws WdkModelException {
    for (AttributeRowStreamData attrData : attributeIteratorList) {
      if (attrData.resultList.next()) {
        throw new WdkModelException("Attribute query data [" + attrData.fileName + "] contains more records than ID query.");
      }
    }
    for (TableRecordStreamData tableData : tableIteratorList) {
      if (tableData.lastRecord != null) {
        throw new WdkModelException("Table query data [" + tableData.fileName + "] contains more records than ID query.");
      }
    }
  }

  private static void attachAttributes(StaticRecordInstance recordInstance, AttributeRowStreamData attributeData,
      PrimaryKeyDefinition pkDef, Map<String, Object> expectedPkValues) throws WdkModelException {
    // There should always be a row in the result list for the primary key given.  The conditional should always
    // be satisfied.  Since primary keys are not included in the attribute CSV files, no direct comparison can be made.
    ResultList resultList = attributeData.resultList;
    if (resultList.next()) {
      PrimaryKeyValue attrListPk = pkDef.getPrimaryKeyFromResultList(resultList);
      if (PrimaryKeyValue.rawValuesDiffer(expectedPkValues, attrListPk.getRawValues())) {
        throw new WdkModelException("Record ordering in attribute query data [" + attributeData.fileName +
            "] does not match ID query.  Expected: " + FormatUtil.prettyPrint(expectedPkValues) +
            ", Found: " + FormatUtil.prettyPrint(attrListPk.getRawValues()));
      }
      for (QueryColumnAttributeField field : attributeData.attributes) {
        AttributeValue attributeValue = new QueryColumnAttributeValue(field, resultList.get(field.getName()));
        recordInstance.addAttributeValue(attributeValue);
      }
    }
    else {
      throw new WdkModelException("Mismatched record count from ID query and records in attribute query data [" +
          attributeData.fileName + "].  Too few records in CSV file.");
    }
  }

  /**
   * For the provided table, determine whether the next record instance returned by the SingleTableRecordInstanceStream object is the same
   * as the record instance being assembled (by comparing pk values).  If they are the same, copy to values for the subject
   * table into the record instance being assembled, advance the SingleTableRecordInstanceStream iterator and save it and the resulting new
   * next record instance into the tuple kept in the tableIteratorMap.  Otherwise, just attach an empty version of the provided table
   * to the record instance being assembled.
   * 
   * @param aggregateRecordInstance the record instance under assembly
   * @param tableData object containing table field and data stream for that table
   * @param pkValues primary key value of the current record instance
   * @throws WdkModelException if something goes wrong
   * @throws WdkUserException if something else goes wrong
   */
  private static void attachTable(StaticRecordInstance aggregateRecordInstance,
      TableRecordStreamData tableData, Map<String, Object> pkValues) throws WdkModelException, WdkUserException {
    // If the next record instance for a given table is null, no current or subsequent record instances will have this table, in
    // which case, we need to create an empty one (to avoid any lazy db calls)
    RecordInstance tableRecord = tableData.lastRecord;
    if (tableRecord != null) {
      // If the primary keys of next record instance for a given table do not match the keys of the aggregate record instance,
      // the next record instance applies to an aggregate record instance that is as yet downstream, in which case, create an
      // empty table value.
      Map<String,Object> nextPkValues = tableRecord.getPrimaryKey().getRawValues();
      if (!PrimaryKeyValue.rawValuesDiffer(pkValues, nextPkValues)) {
        // Collect the table value from the next record instance to be applied to the aggregate record instance.
        aggregateRecordInstance.addTableValue(tableRecord.getTableValue(tableData.field.getName()));
        tableData.lastRecord = tableData.iterator.hasNext() ? tableData.iterator.next() : null;
        return;
      }
    }
    // otherwise, add empty table
    aggregateRecordInstance.addTableValue(new TableValue(tableData.field));
  }

  /**
   * Closing the attribute CSV result lists, the table CSV result list, the id SQL result list and
   * the single table record instance streams used to populate the record instance tables.
   */
  @Override
  protected void performAdditionalClosingOps() {
    if (_attributeIteratorList != null) _attributeIteratorList.close();
    if (_tableIteratorList != null) _tableIteratorList.close();
  }
}
