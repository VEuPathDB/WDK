package org.gusdb.wdk.model.report;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

/**
 * @author Cary P.
 * 
 */
public class JSONReporter extends StandardReporter {

  private static Logger LOG = Logger.getLogger(JSONReporter.class);

  private TableCache _tableCache;

  public JSONReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public void setProperties(ReporterRef reporterRef) throws WdkModelException {
    super.setProperties(reporterRef);
    String cacheTableName = TableCache.getCacheTableName(_properties);
    if (cacheTableName != null) {
      _tableCache = new TableCache(getQuestion().getRecordClass(), _wdkModel.getAppDb(), cacheTableName);
    }
  }

  @Override
  public String getHttpContentType() {
    return "application/json";
  }

  @Override
  public String getDownloadFileName() {
    return getQuestion().getName() + "_detail.json";
  }

  @Override
  public void write(OutputStream out) throws WdkModelException {
    try (RecordStream records = getRecords()) {
      OutputStreamWriter streamWriter = new OutputStreamWriter(out);
      JSONWriter writer = new JSONWriter(streamWriter);

      AnswerValue av = _baseAnswer;
      writer.object().key("response").object().key("recordset").object().key("id").value(
          av.getChecksum()).key("count").value(this.getResultSize()).key("type").value(
              av.getQuestion().getRecordClass().getDisplayName()).key("records").array();

      if (_tableCache != null) {
        _tableCache.open();
      }

      // get page based answers with a maximum size (defined in PageAnswerIterator)
      int recordCount = 0;
      for (RecordInstance record : records) {
        writer.object().key("id").value(record.getPrimaryKey());

        // print out attributes of the record first
        formatAttributes(record, getSelectedAttributes(), writer);

        // print out tables
        formatTables(record, getSelectedTables(), writer, _tableCache);

        // count the records processed so far
        recordCount++;
        writer.endObject();
        streamWriter.flush();
      }

      writer.endArray() // records
          .endObject().endObject().endObject();
      streamWriter.flush();
      LOG.info("Totally " + recordCount + " records dumped");
    }
    catch (WdkUserException | JSONException | SQLException | IOException e) {
      throw new WdkModelException("Unable to write JSON report", e);
    }
    finally {
      if (_tableCache != null) {
        _tableCache.close();
      }
    }
  }

  private static void formatAttributes(RecordInstance record, Set<AttributeField> attributes, JSONWriter writer)
      throws WdkModelException, WdkUserException {
    if (attributes.size() > 0) {
      writer.key("fields").array();
      for (AttributeField field : attributes) {
        AttributeValue value = record.getAttributeValue(field.getName());
        writer.object().key("name").value(field.getName()).key("value").value(value.getValue()).endObject();
      }
      writer.endArray();
    }
  }

  /**
   * Add the following to the writer for the passed record:
   * 
   * { tables: [
   *   { name: String, rows: [
   *     { fields: [
   *       { name: String, value: String },
   *       ...
   *     ]}
   *   ]}
   * ]}
   */
  private static void formatTables(RecordInstance record, Set<TableField> tables, JSONWriter writer, TableCache tableCache)
      throws WdkModelException, SQLException, WdkUserException {

    JSONArray tablesArray = new JSONArray();

    // print out tables of the record
    for (TableField table : tables) {

      // tuple of #rows and formatted object
      TwoTuple<Integer,JSONObject> tableData = null;

      if (tableCache == null) {
        // if not caching then simply format and return
        tableData = getTableJson(record.getTableValue(table.getName()));
      }
      else {
        // check if the record has been cached
        TwoTuple<Integer,String> cachedData = tableCache.getCachedTableValue(record, table.getName());
        if (cachedData == null) {
          tableData = getTableJson(record.getTableValue(table.getName()));
          cachedData = new TwoTuple<Integer,String>(
              tableData.getFirst(), tableData.getSecond().toString());
          tableCache.insertTableValue(record, table.getName(), cachedData);
        }
      }

      tablesArray.put(tableData.getSecond());
    }

    // write to the stream
    writer.key("tables").value(tablesArray);

    if (tableCache != null) {
      // flush for each record
      tableCache.flushBatch();
    }
  }

  private static TwoTuple<Integer,JSONObject> getTableJson(TableValue tableValue) throws JSONException, WdkModelException, WdkUserException {
    TableField table = tableValue.getTableField();
    JSONObject tableObject = new JSONObject().put("name", table.getDisplayName());
    JSONArray rowsArray = new JSONArray();
    AttributeField[] fields = table.getAttributeFields(FieldScope.REPORT_MAKER);

    // output table header
    int tableSize = 0;
    for (Map<String, AttributeValue> row : tableValue) {
      tableSize++;
      JSONObject rowObject = new JSONObject();
      JSONArray fieldsArray = new JSONArray();
      for (AttributeField field : fields) {
        String fieldName = field.getName();
        AttributeValue value = row.get(fieldName);
        JSONObject fieldObject = new JSONObject().put("name", fieldName).put("value", value.getValue());
        fieldsArray.put(fieldObject);
      }
      rowObject.put("fields", fieldsArray);
      rowsArray.put(rowObject);
    }
    tableObject.put("rows", rowsArray);
    return new TwoTuple<Integer,JSONObject>(tableSize, tableObject);
  }
}
