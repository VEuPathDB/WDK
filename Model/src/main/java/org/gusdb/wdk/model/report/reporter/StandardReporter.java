package org.gusdb.wdk.model.report.reporter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.FileBasedRecordStream;
import org.gusdb.wdk.model.answer.stream.PagedAnswerRecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStreamFactory;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.AbstractReporter;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.config.StandardConfig;
import org.gusdb.wdk.model.report.util.TableCache;
import org.json.JSONObject;

/**
 * A reporter whose configuration is standard, i.e. a list of attributes and tables
 * 
 * @author steve
 */
public abstract class StandardReporter extends AbstractReporter {

  private static final Logger LOG = Logger.getLogger(StandardReporter.class);

  private StandardConfig _standardConfig;
  private Set<AttributeField> _attributes;
  private Set<TableField> _tables;

  @Override
  public StandardReporter setAnswerValue(AnswerValue answerValue) {
    super.setAnswerValue(answerValue);
    standardizeAnswerValue(_baseAnswer);
    return this;
  }

  private static void standardizeAnswerValue(AnswerValue answerValue) {

    // always return all records; user cannot select a subset of records
    answerValue.setPageIndex(1, AnswerValue.UNBOUNDED_END_PAGE_INDEX);

    // disable custom sorting - (always sort by primary key)
    answerValue.setSortingMap(Collections.emptyMap());
  }

  @Override
  public Reporter configure(Map<String, String> config) throws ReporterConfigException {
    LOG.info("StandardReporter: configure() with Map: " + getClass().getName() + " instantiated and configured with: " +
        FormatUtil.prettyPrint(config, Style.MULTI_LINE));
    _standardConfig = new StandardConfig(getQuestion()).configure(config);
    loadValidatedFields();
    return this;
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException {
    LOG.info("StandardReporter: configure() with json: " +getClass().getName() + " instantiated and configured with: " + config.toString(2));
    _standardConfig = new StandardConfig(getQuestion()).configure(config);
    loadValidatedFields();
    return this;
  }

  protected StandardConfig getStandardConfig() {
    return _standardConfig;
  }

  protected Set<AttributeField> getSelectedAttributes() {
    return _attributes;
  }

  protected Set<TableField> getSelectedTables() {
    return _tables;
  }

  private static String PROPERTY_PAGE_SIZE = "page_size";
  private static final int DEFAULT_PAGE_SIZE = 100;
  protected RecordStream getRecords() throws WdkModelException {
    switch(_standardConfig.getStreamStrategy()) {
      case PAGED_ANSWER:
        int pageSize = DEFAULT_PAGE_SIZE;
        if (_properties.containsKey(PROPERTY_PAGE_SIZE)) {
          try {
            pageSize = Integer.valueOf(_properties.get(PROPERTY_PAGE_SIZE));
          }          catch (NumberFormatException e) {
            throw new WdkModelException("Reporter property '" + PROPERTY_PAGE_SIZE + "' must be a positive integer.");
          }
        }
        return new PagedAnswerRecordStream(_baseAnswer, pageSize);
      case FILE_BASED:
        return new FileBasedRecordStream(_baseAnswer, getSelectedAttributes(), getSelectedTables());
      case SMART:
      default:
        // unspecified; let factory decide for us
        return RecordStreamFactory.getRecordStream(_baseAnswer, getSelectedAttributes(), getSelectedTables());
    }
  }

  private void loadValidatedFields() throws ReporterConfigException {

    // get the columns that will be in the report
    Set<Field> fields = validateColumns(getQuestion(), _standardConfig);

    // divide fields into attributes and tables
    _attributes = new LinkedHashSet<AttributeField>();
    _tables = new LinkedHashSet<TableField>();
    for (Field field : fields) {
      if (field instanceof AttributeField) {
        _attributes.add((AttributeField) field);
      }
      else if (field instanceof TableField) {
        _tables.add((TableField) field);
      }
    }
  }

  /**
   * @return set of validated fields
   * @throws WdkModelException if an error occurs while validating
   * @throws WdkUserException if column inputs are invalid
   */
  private static Set<Field> validateColumns(Question question, StandardConfig stdConfig) throws ReporterConfigException {
    // get a map of report maker fields
    Map<String, Field> fieldMap = question.getFields();
    // the config map contains a list of column names;
    Set<Field> columns = new LinkedHashSet<Field>();
    if (stdConfig.getIsAllFields()) {
      columns.addAll(fieldMap.values());
    }
    else {
      Set<AttributeField> attrColumns = validateAttributeColumns(question, stdConfig);
      columns.addAll(attrColumns);
      
      if (stdConfig.getIsAllTables()) {
        for (String k : fieldMap.keySet()) {
          Field f = fieldMap.get(k);
          // if this is a table field
          if (TableField.class.isAssignableFrom(f.getClass()))
            columns.add(f);
        }
      }
      else {
        for (String table : stdConfig.getTables()) {
          table = table.trim();
          if (!fieldMap.containsKey(table))
            throw new ReporterConfigException("The table '" + table + "' is requested for the report, but is not available for this type of record");
          columns.add(fieldMap.get(table));
        }
      }
    }
    return columns;
  }

  /**
   * Find the requested fields that are Attribute columns and add to provided set
   * 
   * @param fieldMap the requested fields
   * @param columns add the attribute columns here
   * @throws WdkUserException 
   */
  private static Set<AttributeField> validateAttributeColumns(Question question, StandardConfig stdConfig) throws ReporterConfigException {
    Map<String, AttributeField> fieldMap = question.getAttributeFieldMap();
    Set<AttributeField> columns = new LinkedHashSet<AttributeField>();
    
    if (stdConfig.getIsAllAttributes()) {
      for (String k : fieldMap.keySet()) {
        Field f = fieldMap.get(k);
        // if this is an attribute field
        if (AttributeField.class.isAssignableFrom(f.getClass()))
          columns.add((AttributeField)f);
      }
    }
    else {
      for (String column : stdConfig.getAttributes()) {
        column = column.trim();
        if (fieldMap.containsKey(column)) {
          columns.add(fieldMap.get(column));
        } else throw new ReporterConfigException("Column '" + column + "' is requested for the report, but is not available for this type of record");
      }
    }
    return columns;
  }

  /**
   * Formats and writes table values for a record
   *
   * @param record record whose tables will be written
   * @param tables table fields needing written
   * @param includeEmptyTables whether to include empty tables
   * @param writer where to write the formatted table data
   * @param tableCache cache of formatted table data (null if no cache configured/available)
   * @param tableWriter function that takes the table value to be formatted and written,
   * the writer to which the the formatted record should be written, and a boolean telling
   * whether to include empty tables, and returns the number of rows in the table
   * @throws WdkModelException
   * @throws SQLException
   * @throws WdkUserException
   */
  protected static void formatTables(RecordInstance record, Set<TableField> tables,
      boolean includeEmptyTables, PrintWriter writer, TableCache tableCache,
      FunctionWithException<ThreeTuple<TableValue, Writer, Boolean>, Integer> tableWriter)
          throws WdkModelException, SQLException, WdkUserException {
    try {
      //LOG.debug("StandardReporter: formatTables(): starting on a record");
      // print out tables of the record
      for (TableField table : tables) {
        //LOG.debug("StandardReporter: formatTables(): looping through tables..");

        // if not caching then simply write record
        if (tableCache == null) {
          tableWriter.apply(new ThreeTuple<>(record.getTableValue(table.getName()), writer, includeEmptyTables));
        }
        else {
          LOG.debug("StandardReporter: formatTables(): caching table data");
          // check if the record has been cached
          TwoTuple<Integer,String> tableData = tableCache.getCachedTableValue(record, table.getName());
          if (tableData == null) {
            // NOTE: this code branch does NOT take advantage of the streaming
            //    optimization to preserve memory like the non-cache branch above
            // TODO: change insertTableValue to take the function and write into cache as a stream
            //    --OR-- remove the cache entirely(?)- only used for GFF

            // write the data into the buffer
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Writer tmpWriter = new OutputStreamWriter(bytes);
            int numRows = tableWriter.apply(new ThreeTuple<>(record.getTableValue(table.getName()), tmpWriter, includeEmptyTables));
            tmpWriter.flush();
            tableData = new TwoTuple<>(numRows, bytes.toString());
            tableCache.insertTableValue(record, table.getName(), tableData);

            // write to the stream
            if (includeEmptyTables || numRows > 0) {
              writer.print(tableData.getSecond());
              writer.flush();
            }
          }
        }
      }
      // commit batch of all tables for each record instance
      if (tableCache != null) {
        tableCache.flushBatch();
      }
    }
    catch (Exception e) {
      throw WdkModelException.translateFrom(e, "Could not write formatted table values");
    }
  }

  @Override
  public ContentDisposition getContentDisposition() {
    switch (_standardConfig.getAttachmentType()) {
      case "plain": // "Show in Browser"
        return ContentDisposition.INLINE;
      case "text": // "Text File"
      default:
        return ContentDisposition.ATTACHMENT;
    }
  }
}
