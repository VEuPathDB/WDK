package org.gusdb.wdk.model.report.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.TableValueRow;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.LinkAttributeValue;
import org.gusdb.wdk.model.record.attribute.TextAttributeValue;
import org.gusdb.wdk.model.report.config.AnswerDetails.AttributeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK RecordInstance objects.  RecordInstance JSON will have the following form:
 * 
 * {
 *   id: Array&lt;Object> (each has name (name of PK column) and value (PK column value) properties,
 *   attributes: { [name: String]: [value: Any] },
 *   tables: { [name: String]: [ { [name: String]: [value: Any] } ] }
 * }
 * 
 * @author rdoherty
 */
public class RecordFormatter {

  private static final Logger LOG = Logger.getLogger(RecordFormatter.class);

  public static TwoTuple<JSONObject,List<Exception>> getRecordJson(RecordInstance record, Collection<String> attributeNames, Collection<String> tableNames, AttributeFormat attributeFormat)
      throws WdkModelException, WdkUserException {
    JSONObject recordJson = new JSONObject()
      .put(JsonKeys.DISPLAY_NAME, record.getIdAttributeValue().getDisplay())
      .put(JsonKeys.ID, getRecordPrimaryKeyJson(record))
      .put(JsonKeys.RECORD_CLASS_NAME, record.getRecordClass().getFullName())
      .put(JsonKeys.ATTRIBUTES, getRecordAttributesJson(record, attributeNames, attributeFormat));
    ThreeTuple<JSONObject,JSONArray,List<Exception>> tableResult = getRecordTablesJson(record, tableNames, attributeFormat);
    recordJson.put(JsonKeys.TABLES, tableResult.getFirst());
    recordJson.put(JsonKeys.TABLE_ERRORS, tableResult.getSecond());
    return new TwoTuple<JSONObject,List<Exception>>(recordJson,tableResult.getThird());
  }

  public static JSONArray getRecordPrimaryKeyJson(RecordInstance record) {
    Map<String,String> pkValues = record.getPrimaryKey().getValues();
    JSONArray pkJson = new JSONArray();
    for (String column : record.getRecordClass().getPrimaryKeyDefinition().getColumnRefs()) {
      pkJson.put(new JSONObject().put(JsonKeys.NAME, column).put(JsonKeys.VALUE, pkValues.get(column)));
    }
    return pkJson;
  }

  private static JSONObject getRecordAttributesJson(RecordInstance record, Collection<String> attributeNames, AttributeFormat attributeFormat) throws WdkModelException, WdkUserException {
    JSONObject attributes = new JSONObject();
    LOG.debug("Outputting record attributes: " + FormatUtil.arrayToString(attributeNames.toArray()));
    for (String attributeName : attributeNames) {
      attributes.put(attributeName, getAttributeValueJson(record.getAttributeValue(attributeName), attributeFormat));
    }
    return attributes;
  }

  private static ThreeTuple<JSONObject,JSONArray,List<Exception>> getRecordTablesJson(RecordInstance record, Collection<String> tableNames, AttributeFormat attributeFormat) {
    JSONObject tables = new JSONObject();
    JSONArray badTables = new JSONArray();
    List<Exception> exceptionList = new ArrayList<>();
    // loop through tables
    for (String tableName : tableNames) {
      try {
        tables.put(tableName, getTableRowsJson(record, tableName, attributeFormat));
      }
      /* Sometimes individual tables fail due to bad SQL or other reasons; in this event, we don't want the
       * whole request to fail since most of the data is probably fine.  Record the tables that fail and send
       * error email to inform the client and admins and then move on to the next table.
       */
      catch (Exception e) {
        String errorMsg = "Unable to dynamically load table '" + tableName + "' for record: " + record.getPrimaryKey().getValuesAsString();
        LOG.error(errorMsg, e);
        badTables.put(tableName);
        exceptionList.add(new WdkModelException(errorMsg, e));
      }
    }
    return new ThreeTuple<JSONObject,JSONArray,List<Exception>>(tables, badTables, exceptionList);
  }

  public static JSONArray getTableRowsJson(RecordInstance record, String tableName, AttributeFormat attributeFormat) throws WdkModelException, WdkUserException {
    JSONArray tableRowsJson = new JSONArray();
    // loop through rows
    TableValue tableValue = record.getTableValue(tableName);
    for (TableValueRow row : tableValue) {
      JSONObject tableAttrsJson = new JSONObject();
      // loop through columns
      for (Entry<String, AttributeValue> entry : row.entrySet()) {
        tableAttrsJson.put(entry.getKey(), getAttributeValueJson(entry.getValue(), attributeFormat));
      }
      tableRowsJson.put(tableAttrsJson);
    }
    return tableRowsJson;
  }

  private static Object getAttributeValueJson(AttributeValue attr, AttributeFormat attributeFormat) throws WdkModelException, WdkUserException {

    if (attributeFormat == AttributeFormat.DISPLAY) {
      // for display format, show 
      if (attr instanceof LinkAttributeValue) {
        LinkAttributeValue linkAttr = (LinkAttributeValue)attr;
        String displayText = linkAttr.getDisplayText();

        // Treat an empty displayText as null
        if (displayText == null || displayText.isEmpty()) {
          return JSONObject.NULL;
        }

        return new JSONObject()
          .put(JsonKeys.URL, linkAttr.getUrl())
          .put(JsonKeys.DISPLAY_TEXT, displayText);
      }
  
      if (attr instanceof TextAttributeValue){
        return attr.getDisplay();
      }
    }
    else {
      if (attr instanceof LinkAttributeValue) {
        return ((LinkAttributeValue)attr).getUrl();
      }
    }
    // outside of above cases, return value of attribute or JSON null if empty
    String value = attr.getValue();
    return value == null ? JSONObject.NULL : value;
  }
}
