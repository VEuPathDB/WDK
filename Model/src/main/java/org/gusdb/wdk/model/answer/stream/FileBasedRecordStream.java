package org.gusdb.wdk.model.answer.stream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Named;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.CsvResultList;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

import au.com.bytecode.opencsv.CSVWriter;

public class FileBasedRecordStream implements RecordStream {

  private static final Logger LOG = Logger.getLogger(FileBasedRecordStream.class);

  /** Buffer size for the buffered writer use to write CSV files */
  public static final int BUFFER_SIZE = 32768;

  /** Determines whether temporary directory and containing files are deleted on close */
  private static final boolean DELETE_TEMPORARY_FILES = true;

  /**
   * Suffix for temporary CSV files constructed from table queries. Done in part to avoid conflicts with
   * column attribute queries with the same name.
   */
  private static final String TABLE_DESIGNATION = "_table";

  /** Extension applied to temporary CSV file names */
  private static final String TEMP_FILE_EXT = ".txt";

  /** The prefix to be applied to a temporary directory */
  private static final String DIRECTORY_PREFIX = "wdk_";

  // final fields passed to constructor
  private final AnswerValue _answerValue;
  private final Collection<AttributeField> _attributes;
  private final Collection<TableField> _tables;

  // fields populated by populateFiles
  private Path _temporaryDirectory;
  private Map<Path, List<QueryColumnAttributeField>> _attributeFileMap = new HashMap<>();
  private Map<Path, TwoTuple<TableField,List<String>>> _tableFileMap = new HashMap<>();
  private boolean _filesPopulated = false;

  // collection of iterators that must be closed if this stream is closed (iterators are now invalid)
  private final List<FileBasedRecordIterator> _iterators = new ArrayList<>();

  /**
   * Creates a record stream that can provide all records without paging by caching attribute and table query
   * results in files and then reading from those file in parallel to construct RecordInstance objects one by
   * one as requested by the provided iterator.
   *
   * @param answerValue
   *          answer value defining the records to be returned
   * @param attributes
   *          collection of requested attribute fields
   * @param tables
   *          collection of requested table fields
   */
  public FileBasedRecordStream(AnswerValue answerValue, Collection<AttributeField> attributes, Collection<TableField> tables) {
    _answerValue = answerValue;
    _attributes = attributes;
    _tables = tables;
  }

  /**
   * Serially executes all attribute and table queries required to construct RecordInstance objects based on
   * the attribute and table sets requested in the constructor. Handles opening and closing DB connections,
   * serializing results to files, and closing files.
   *
   * @throws WdkModelException
   *           if unable to complete population
   */
  private synchronized void populateFiles() throws WdkModelException {

    // throw if files already populated
    if (_filesPopulated) {
      LOG.warn("populateFiles(): Multiple calls to populateFiles() on open stream.  This method need only be called once.  Ignoring...");
      return;
    }

    // create directory to store temporary files for this stream
    _temporaryDirectory = createTemporaryDirectory(_answerValue);

    // assemble files
    _attributeFileMap = assembleAttributeFiles(_answerValue, _temporaryDirectory, _attributes);
    _tableFileMap = assembleTableFiles(_answerValue, _temporaryDirectory, _tables);

    _filesPopulated = true;
    LOG.debug("populateFiles(): done with files");
  }

  /**
   * Create a temporary directory to house the temporary CSV files to be created.
   *
   * @throws WdkModelException
   */
  private static Path createTemporaryDirectory(AnswerValue answerValue) throws WdkModelException {
    try {
      Path wdkTempDir = answerValue.getWdkModel().getModelConfig().getWdkTempDir();
      return IoUtil.createOpenPermsTempDir(wdkTempDir, DIRECTORY_PREFIX);
    }
    catch (IOException ioe) {
      throw new WdkModelException(ioe);
    }
  }

  /**
   * Returns the set of column attribute fields required to produce the passed set of attributes.  Some
   * non-column attribute fields may depend on column attribute fields not provided in the given
   * attribute field list. Those need to be added.
   *
   * @param attributes list of attribute fields of any flavor
   * @param includeDependedColumns whether to find the needed columns of non-column attributes in the passed list
   * @return map containing all passed column attribute fields and any depended column attributes
   * @throws WdkModelException if unable to load depended attribute fields
   */
  public static Collection<ColumnAttributeField> getRequiredColumnAttributeFields(
      Collection<AttributeField> attributes, boolean includeDependedColumns) throws WdkModelException {
    // Using a set to collect column attribute fields because multiple non-column attributes may cite the same
    // column attributes as dependencies and we don't want them counted more than once.
    MapBuilder<String, ColumnAttributeField> columnAttributes = new MapBuilder<>(new LinkedHashMap<String, ColumnAttributeField>());

    for (AttributeField attribute : attributes) {
      if (attribute instanceof ColumnAttributeField) {
        columnAttributes.put(attribute.getName(), (ColumnAttributeField)attribute);
      }
      else if (includeDependedColumns){
        // addition of underlying but unspecified column attributes
        columnAttributes.putAll(attribute.getColumnAttributeFields());
      }
    }

    // get unique list and then filter out PK columns, which will always be fetched
    return columnAttributes.toMap().values();
  }

  /**
   * Convenience method to detect whether a set of requested attribute fields
   * requires exactly one attribute query to fulfill.
   * 
   * @param attributes set of requested attribute fields
   * @return true if fields can be returned by executing only a single attribute query, else false
   * @throws WdkModelException if error occurs while calculating attribute query needs
   */
  public static boolean requiresExactlyOneAttributeQuery(Collection<AttributeField> attributes) throws WdkModelException {
    Collection<TwoTuple<Query, List<QueryColumnAttributeField>>> attributeQueries =
        getAttributeQueryMap(getRequiredColumnAttributeFields(attributes, true));
    if (LOG.isInfoEnabled()) {
      LOG.info("Required attribute fields: ");
      for (TwoTuple<Query, List<QueryColumnAttributeField>> query : attributeQueries) {
        LOG.info("  " + query.getFirst().getName() + " will provide [ " +
            query.getSecond().stream().map(NamedObject::getName).collect(Collectors.joining(", ")) + " ]");
      }
    }
    return attributeQueries.size() == 1;
  }

  /**
   * Collect all the queries needed to accommodate the requested attributes and assigns attributes to queries
   *
   * @param columnAttributes required column attributes
   * @return a collection of pairs: queries and attribute values available from them
   * @throws WdkModelException
   */
  private static Collection<TwoTuple<Query, List<QueryColumnAttributeField>>> getAttributeQueryMap(
      Collection<ColumnAttributeField> columnAttributes) throws WdkModelException {
    Map<String, TwoTuple<Query, List<QueryColumnAttributeField>>> queryMap = new HashMap<>();
    for (ColumnAttributeField attribute : columnAttributes) {
      // only need to add query if QueryColumnAttributeField (not PK)
      if (attribute instanceof QueryColumnAttributeField) {
        QueryColumnAttributeField queryAttr = (QueryColumnAttributeField)attribute;
        Query query = queryAttr.getColumn().getQuery();
        if (!queryMap.containsKey(query.getFullName())) {
          queryMap.put(query.getFullName(), new TwoTuple<Query, List<QueryColumnAttributeField>>(query, new ArrayList<QueryColumnAttributeField>()));
        }
        queryMap.get(query.getFullName()).getSecond().add(queryAttr);
      }
    }
    return queryMap.values();
  }

  /**
   * Assembles the CSV file at the given file path for given resultList, copying out, in order, all the
   * columns provided in the columnNames list. The CSV file columns are tab delimited and any nulls are
   * provided with a unique representation so that they may be turned back into nulls when later creating ad
   * hoc record instances.
   *
   * @param filePath
   *          - the path to the temporary file that will house the CSV data
   * @param columnNames
   *          - an ordered list of the names to be extracted from the query results
   * @param resultList
   *          - the query results
   * @throws WdkModelException
   */
  private static void assembleCsvFile(Path filePath, List<String> columnNames, ResultList resultList)
      throws WdkModelException {

    try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(filePath.toString()), BUFFER_SIZE),
          CsvResultList.TAB, CsvResultList.QUOTE, CsvResultList.ESCAPE)) {

      // Create an array sized to handle the attribute columns as this will be used to write out a row
      // to the CSV file. Primary keys are not included since there should be exactly 1 row per primary key
      // and since restoration to record instances will employ the same sorted id SQL.
      String[] inputs = new String[columnNames.size()];

      // For each record in the result list
      while (resultList.next()) {
        for (int i = 0; i < columnNames.size(); i++) {
          Object result = resultList.get(columnNames.get(i));
          inputs[i] = (result == null) ? CsvResultList.NULL_REPRESENTATION : String.valueOf(result);
        }
        writer.writeNext(inputs);
      }
    }
    catch (IOException ioe) {
      throw new WdkModelException("Unable to write CSV file", ioe);
    }
  }

  /**
   * Writes out one file per executed query to the temporary directory. A map of attribute file paths to their
   * associated list of attribute fields is created to facilitate later population of a record instance.
   *
   * @param answerValue answer value to write files for
   * @param tempDir temporary directory in which to write the files
   * @param attributes collection of attributes that are required by the caller
   * @return map from temporary file path to the ordered list of columns that can be found in that file
   * @throws WdkModelException if something goes wrong
   */
  private static Map<Path, List<QueryColumnAttributeField>> assembleAttributeFiles(AnswerValue answerValue, Path tempDir,
      Collection<AttributeField> attributes) throws WdkModelException {
    Map<Path, List<QueryColumnAttributeField>> pathMap = new HashMap<>();
    if (attributes == null || attributes.isEmpty()) {
      return pathMap;
    }
    Timer t = new Timer();
    LOG.debug("assembleAttributeFiles(): Starting attribute file assembly...");
    Collection<TwoTuple<Query,List<QueryColumnAttributeField>>> requiredQueries =
        getAttributeQueryMap(getRequiredColumnAttributeFields(attributes, true));
    LOG.debug("assembleAttributeFiles(): Assembled required queries: " + t.getElapsedString());

    // Iterate over all the queries needed to return all the requested attributes
    for (TwoTuple<Query,List<QueryColumnAttributeField>> queryData : requiredQueries) {
      pathMap.put(writeAttributeFile(answerValue, queryData.getFirst(),
          queryData.getSecond(), tempDir), queryData.getSecond());
    }

    LOG.debug("assembleAttributeFiles(): Attribute file assembly complete: " + pathMap);
    return pathMap;
  }

  /**
   * Queries the database use the passed attribute query and answer value and writes a CSV file containing
   * only those columns requested to the temporary directory
   *
   * @param answerValue answer value to write file for
   * @param query attribute query to be executed
   * @param attributeFields column attribute fields to be fetched
   * @param tempDir temporary directory in which to write the file
   * @return path to the file containing the attribute query data
   * @throws WdkModelException if something goes wrong
   */
  private static Path writeAttributeFile(AnswerValue answerValue, Query query,
      List<QueryColumnAttributeField> attributeFields, Path tempDir) throws WdkModelException {

    // Obtain path to CSV file that will hold the results of the current query.
    Path filePath = tempDir.resolve(query.getFullName() + TEMP_FILE_EXT);

    SqlResultList resultList = null;
    try {
      Timer t = new Timer();
      LOG.debug("writeAttributeFile(): Starting query " + query.getName());

      // Getting the paged attribute SQL but in fact, getting a SQL statement requesting with all records.
      String sql = answerValue.getAnswerAttributeSql(query, true);
      LOG.debug("Merged attribute SQL for query '" + query.getFullName() + "': " + FormatUtil.NL + sql);

      // Get the result list for the current attribute query
      DataSource dataSource = answerValue.getWdkModel().getAppDb().getDataSource();
      resultList = new SqlResultList(SqlUtils.executeQuery(dataSource, sql, query.getFullName() + "__attr-full"));

      // Generate full list of columns to fetch, including both PK columns and requested columns
      List<String> columnsToTransfer = new ListBuilder<String>(answerValue.getAnswerSpec().getQuestion()
          .getRecordClass().getPrimaryKeyDefinition().getColumnRefs())
          .addAll(Functions.mapToList(attributeFields, Named.TO_NAME))
          .toList();

      // Transfer the result list content to the CSV file provided
      LOG.debug("writeAttributeFile(): Starting iteration over result list for query " + query.getName() + ": " + t.getElapsedString());
      assembleCsvFile(filePath, columnsToTransfer, resultList);
      LOG.debug("writeAttributeFile(): Finished iteration over result list for query " + query.getName() + ": " + t.getElapsedString());

      // open file permissions and return the path to the temporary CSV file
      filePath.toFile().setWritable(true, false);
      return filePath;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to transfer attribute query result to CSV file", e);
    }
    finally {
      // close the result list for the table query if one exists.
      if (resultList != null) {
        resultList.close();
      }
    }
  }

  /**
   * Constructs one temporary CSV file for each table requested. A map of table file paths to their associated
   * table field is created to facilitate later population of a record instance.
   *
   * @param answerValue answer value to write tables for
   * @param tempDir temporary directory in which to write the files
   * @param tables collection of tables for which files will be written
   * @return map of data file paths to the fields they contain data for
   * @throws WdkModelException if something goes wrong
   */
  private static Map<Path, TwoTuple<TableField,List<String>>> assembleTableFiles(AnswerValue answerValue,
      Path tempDir, Collection<TableField> tables) throws WdkModelException {
    Map<Path, TwoTuple<TableField,List<String>>> pathMap = new HashMap<>();
    if (tables == null || tables.isEmpty()) {
      return pathMap;
    }
    LOG.debug("assembleTableFiles(): Starting table file assembly...");

    // Iterate over all the tables requested
    for (TableField table : tables) {
      TwoTuple<Path,List<String>> fileInfo = writeTableFile(answerValue, tempDir, table);
      pathMap.put(fileInfo.getFirst(), new TwoTuple<TableField, List<String>>(table, fileInfo.getSecond()));
    }
    LOG.debug("assembleTableFiles(): Table file assembly complete:" + pathMap);
    return pathMap;
  }

  /**
   * Queries the database for the table field rows of the passed table for the passed answer value and writes
   * a CSV file containing those rows (PK and table columns included) to the temporary directory
   *
   * @param answerValue answer value to write table for
   * @param tempDir temporary directory in which to write the file
   * @param table table field for which data will be generated
   * @return path to the file containing the table field data
   * @throws WdkModelException if something goes wrong
   */
  private static TwoTuple<Path,List<String>> writeTableFile(AnswerValue answerValue, Path tempDir, TableField table) throws WdkModelException {
    Timer t = new Timer();
    LOG.debug("writeTableFile(): Starting table: " + table.getName() + "(query: " + table.getWrappedQuery().getName() + ")");

    // Appending table designation to query name for file to more easily distinguish
    // these files from those supporting attribute queries and to avoid name collisions.
    Path filePath = tempDir.resolve(table.getName() + TABLE_DESIGNATION + TEMP_FILE_EXT);
    List<String> columnNames = getTableColumnNames(getPkColumnNames(answerValue), table);
    ResultList resultList = null;
    try {

      // Get the result list for the current table query
      resultList = answerValue.getTableFieldResultList(table);

      // Transfer the result list content to the CSV file provided.
      LOG.debug("writeTableFile(): Starting iteration over result list for query " + table.getWrappedQuery().getName() + ": " + t.getElapsedString());
      assembleCsvFile(filePath, columnNames, resultList);
      LOG.debug("writeTableFile(): Finished iteration over result list for query " + table.getWrappedQuery().getName() + ": " + t.getElapsedString());

      // open file permissions and return the path to the temporary CSV file
      filePath.toFile().setWritable(true, false);
      return new TwoTuple<Path,List<String>>(filePath, columnNames);
    }
    finally {
      // Close the result list for the table query if one exists.
      if (resultList != null) {
        resultList.close();
      }
    }
  }

  /**
   * Collects together all the columns representing the primary key attribute fields.
   *
   * @param answerValue reference answer value
   * @return list of column names for primary key
   */
  private static List<String> getPkColumnNames(AnswerValue answerValue) {
    return Arrays.asList(answerValue.getAnswerSpec().getQuestion().getRecordClass().getPrimaryKeyDefinition().getColumnRefs());
  }

  /**
   * Returns a list of the columns that will be written to the table CSV file.  This list includes the primary
   * key values for the record class passed, and the column attributes for the table field.
   *
   * @param table table field
   * @return list of columns
   * @throws WdkModelException if error occurs querying model
   */
  private static List<String> getTableColumnNames(List<String> pkColumns, TableField table) throws WdkModelException {

    // Collect together all the columns representing the column attribute fields to be included
    // Not cherry-picking columns here so we can rely on the order of the column attribute fields in the table
    Collection<ColumnAttributeField> fields = getRequiredColumnAttributeFields(table.getAttributeFieldMap().values(), false);

    // Combine the primary key columns and column attribute columns into a single list
    List<String> columnNames = new ArrayList<>(pkColumns);
    columnNames.addAll(Functions.mapToList(fields, Named.TO_NAME));
    return columnNames;
  }

  /**
   * Creates an iterator which can construct RecordInstance records on the fly from the files produced by
   * populateFiles(). Please note that the RecordInstances returned by the iterator will ONLY have the fields
   * and attributes specified in this class's constructor; additional fields and attributes cannot be added
   * at-will.
   */
  @Override
  public Iterator<RecordInstance> iterator() {
    FileBasedRecordIterator iter;
    try {
      populateFiles(); // will do nothing if already called
      iter = new FileBasedRecordIterator(_answerValue, _attributeFileMap, _tableFileMap);
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException(e);
    }
    _iterators.add(iter);
    return iter;
  }

  /**
   * Closes and removes all files written by this object and alerts any returned iterators that files will no
   * longer be available. Files will be closed quietly
   */
  @Override
  public void close() {
    _filesPopulated = false;
    for (FileBasedRecordIterator iter : _iterators) {
      // close each iterator; will disallow further record reading
      iter.close();
    }
    // remove temporary dir and resident files created to populate on the fly record instances
    if (DELETE_TEMPORARY_FILES) {
      try {
        IoUtil.deleteDirectoryTree(_temporaryDirectory);
      }
      catch (IOException e) {
        LOG.error("Unable to completely remove temporary directory: " + _temporaryDirectory, e);
      }
    }
  }
}
