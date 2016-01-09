package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.LinkAttributeValue;
import org.gusdb.wdk.service.formatter.Keys;
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

  public static JSONObject getRecordJson(RecordInstance record, Collection<String> attributeNames, Collection<String> tableNames)
      throws WdkModelException, WdkUserException {
    return new JSONObject()
      .put(Keys.DISPLAY_NAME, record.getPrimaryKey().getDisplay())
      .put(Keys.OVERVIEW, record.getOverview())
      .put(Keys.ID, record.getPrimaryKey().getValues())
      .put(Keys.ATTRIBUTES, getRecordAttributesJson(record, attributeNames))
      .put(Keys.TABLES, getRecordTablesJson(record, tableNames));
  }

  private static JSONObject getRecordAttributesJson(RecordInstance record, Collection<String> attributeNames) throws WdkModelException, WdkUserException {
    JSONObject attributes = new JSONObject();
    LOG.debug("Outputting record attributes: " + FormatUtil.arrayToString(attributeNames.toArray()));
    for (String attributeName : attributeNames) {
      attributes.put(attributeName, getAttributeValueJson(record.getAttributeValue(attributeName)));
    }
    return attributes;
  }

  private static JSONObject getRecordTablesJson(RecordInstance record, Collection<String> tableNames)
      throws WdkModelException, WdkUserException {
    JSONObject tables = new JSONObject();
    // loop through tables
    for (String tableName : tableNames) {
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
    return tables;
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
