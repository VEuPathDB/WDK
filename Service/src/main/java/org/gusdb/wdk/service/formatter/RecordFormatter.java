package org.gusdb.wdk.service.formatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.LinkAttributeValue;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK RecordInstance objects.  RecordInstance JSON will have the following form:
 * 
 * {
 *   id: Object (map of PK attribute names -> PK attribute values),
 *   attributes: { [name: String]: [value: Any] },
 *   tables: { [name: String]: [ { [name: String]: [value: Any] } ] }
 * }
 * 
 * @author rdoherty
 */
public class RecordFormatter {

  private static final Logger LOG = Logger.getLogger(RecordFormatter.class);

  public static TwoTuple<JSONObject,List<Exception>> getRecordJson(RecordInstance record, Collection<String> attributeNames, Collection<String> tableNames)
      throws WdkModelException, WdkUserException {
    JSONObject recordJson = new JSONObject()
      .put(Keys.DISPLAY_NAME, record.getIdAttributeValue().getDisplay())
      .put(Keys.ID, getRecordPrimaryKeyJson(record))
      .put(Keys.RECORD_CLASS_NAME, record.getRecordClass().getFullName())
      .put(Keys.ATTRIBUTES, getRecordAttributesJson(record, attributeNames));
    TwoTuple<JSONObject,List<Exception>> tableResult = getRecordTablesJson(record, tableNames);
    recordJson.put(Keys.TABLES, tableResult.getFirst());
    return new TwoTuple<JSONObject,List<Exception>>(recordJson,tableResult.getSecond());
  }

  private static JSONArray getRecordPrimaryKeyJson(RecordInstance record) {
    JSONArray pkJson = new JSONArray();
    for (Map.Entry<String, String> entry : record.getPrimaryKey().getValues().entrySet()) {
      pkJson.put(new JSONObject().put(Keys.NAME, entry.getKey()).put(Keys.VALUE, entry.getValue()));
    }
    return pkJson;
  }

  private static JSONObject getRecordAttributesJson(RecordInstance record, Collection<String> attributeNames) throws WdkModelException, WdkUserException {
    JSONObject attributes = new JSONObject();
    LOG.debug("Outputting record attributes: " + FormatUtil.arrayToString(attributeNames.toArray()));
    for (String attributeName : attributeNames) {
      attributes.put(attributeName, getAttributeValueJson(record.getAttributeValue(attributeName)));
    }
    return attributes;
  }

  private static TwoTuple<JSONObject,List<Exception>> getRecordTablesJson(RecordInstance record, Collection<String> tableNames) {
    JSONObject tables = new JSONObject();
    JSONArray badTables = new JSONArray();
    List<Exception> errors = new ArrayList<>();
    // loop through tables
    for (String tableName : tableNames) {
      try {
        JSONArray tableRowsJson = new JSONArray();
        // loop through rows
        TableValue tableValue = record.getTableValue(tableName);
        for (Map<String, AttributeValue> row : tableValue) {
          JSONObject tableAttrsJson = new JSONObject();
          // loop through columns
          for (Entry<String, AttributeValue> entry : row.entrySet()) {
            tableAttrsJson.put(entry.getKey(), getAttributeValueJson(entry.getValue()));
          }
          tableRowsJson.put(tableAttrsJson);
        }
        tables.put(tableName, tableRowsJson);
      }
      /* Sometimes individual tables fail due to bad SQL or other reasons; in this event, we don't want the
       * whole request to fail since most of the data is probably fine.  Record the tables that fail and send
       * error email to inform the client and admins and then move on to the next table.
       */
      catch (Exception e) {
        String errorMsg = "Unable to dynamically load table '" + tableName + "' for record: " + record.getPrimaryKey().getValuesAsString();
        LOG.error(errorMsg, e);
        badTables.put(tableName);
        errors.add(new WdkModelException(errorMsg, e));
      }
    }
    if (badTables.length() != 0) {
      tables.put(Keys.ERRORS, badTables);
    }
    return new TwoTuple<JSONObject,List<Exception>>(tables, errors);
  }

  private static Object getAttributeValueJson(AttributeValue attr) throws WdkModelException, WdkUserException {

    if (attr instanceof LinkAttributeValue) {
      LinkAttributeValue linkAttr = (LinkAttributeValue) attr;
      String displayText = linkAttr.getDisplayText();

      // Treat an empty displayText as null
      if (displayText == null || displayText.isEmpty()) {
        return JSONObject.NULL;
      }

      return new JSONObject()
        .put(Keys.URL, linkAttr.getUrl())
        .put(Keys.DISPLAY_TEXT, displayText);
    }

    else {
      // TODO: figure out what kinds of values might be returned here and
      //     make sure they look pretty in JSON
      Object value = attr.getValue();
      return value == null ? JSONObject.NULL : value;
    }
  }
}
