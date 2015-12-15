package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.LinkAttributeValue;
import org.json.JSONArray;
import org.json.JSONObject;

/**
JSON output format:
{
  meta: {
    count: Number,
    class: String,
    attributes: [ {
      name: String,
      displayName: String,
      sortable: Boolean,
      removable: Boolean,
      category: String,
      type: string (comes from “type” property of attribute tag),
      className: String,
      properties: Object
    } ],

    tables: [ {
      name: String,
      displayName: String,
      attributes: [ {
        name: String,
        displayName: String,
        help: String,
        description: String,
        type: String
      } ]
    } ]
  },
  record:  {
    id: Any,
    attributes: { [name: String]: [value: Any] },
    tables: { [name: String]: [ { [name: String]: [value: Any] } ] }
  }
}
*/
public class RecordFormatter {

  private static final Logger LOG = Logger.getLogger(RecordFormatter.class);

  public static JSONObject formatRecord(RecordInstance recordInstance, List<String> attributeNames, List<String> tableNames) throws WdkModelException {
    try {
      JSONObject parent = new JSONObject();
      parent.put("meta", getMetaData(recordInstance, attributeNames, tableNames));
      parent.put("record", getRecordJson(recordInstance, attributeNames, tableNames));
      return parent;
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
  }

  public static JSONObject getMetaData(RecordInstance recordInstance, List<String> attributeNames, List<String> tableNames) {
    JSONObject meta = new JSONObject();
    RecordClass recordClass = recordInstance.getRecordClass();
    meta.put("class", recordClass.getFullName());
    
    JSONArray attributes = new JSONArray();
    for (AttributeField attrib : recordClass.getAttributeFields()) {
      if (!attributeNames.contains(attrib.getName())) continue;
      JSONObject attribJson = new JSONObject();
      attribJson.put("name", attrib.getName());
      attribJson.put("displayName", attrib.getDisplayName());
      attribJson.put("help", attrib.getHelp());
      attribJson.put("align", attrib.getAlign());
      attribJson.put("isSortable", attrib.isSortable());
      attribJson.put("isRemovable", attrib.isRemovable());
      attribJson.put("type", attrib.getType());
      attributes.put(attribJson);
    }
    meta.put("attributes", attributes);
    
    JSONArray tables = new JSONArray();
    for (TableField table : recordClass.getTableFields()) {
      if (!tableNames.contains(table.getName())) continue;
      JSONObject tableJson = new JSONObject();
      tableJson.put("name", table.getName());
      tableJson.put("displayName", table.getDisplayName());
      tableJson.put("help", table.getHelp());
      tableJson.put("description", table.getDescription());
      tableJson.put("type", table.getType());
      tables.put(tableJson);
    }
    meta.put("tables", tables);
   
    return meta;
  }

  public static JSONObject getRecordJson(RecordInstance record)
      throws WdkModelException, WdkUserException {
      return getRecordJson(record, null, null);
  }

  /**
   * 
   * @param record
   * @param attributeNames
   * @param tableNames
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   * @throws NullPointerException if any arguments are null
   */
  public static JSONObject getRecordJson(RecordInstance record, List<String> attributeNames, List<String> tableNames)
      throws WdkModelException, WdkUserException {
    JSONObject json = new JSONObject();
    json.put("id", record.getPrimaryKey().getValues());
    json.put("displayName",  record.getPrimaryKey().getDisplay());
    JSONObject attributes = new JSONObject();
    LOG.debug("Outputting record attributes: " + FormatUtil.arrayToString(attributeNames.toArray()));
    for (String attributeName : attributeNames) {
      attributes.put(attributeName, getAttributeJsonValue(record.getAttributeValue(attributeName)));
    }
    json.put("attributes", attributes);

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
          if (!entry.getValue().getAttributeField().isInternal()) {
            tableAttrsJson.put(entry.getKey(), getAttributeJsonValue(entry.getValue()));
          }
        }
        tableRowsJson.put(tableAttrsJson);
      }
      tables.put(tableName, tableRowsJson);
    }
    json.put("tables", tables);
    return json;
  }
  
  private static Object getAttributeJsonValue(AttributeValue attr) throws
    WdkModelException, WdkUserException {

    if (attr instanceof LinkAttributeValue) {
      LinkAttributeValue linkAttr = (LinkAttributeValue) attr;
      String displayText = linkAttr.getDisplayText();

      // Treat an empty displayText as null
      if (displayText == null || displayText.isEmpty()) {
        return JSONObject.NULL;
      }

      return new JSONObject()
      .put("url",  linkAttr.getUrl())
      .put("displayText", displayText);
    }

    else {
      Object value = attr.getValue();
      return value == null ? JSONObject.NULL : value;
    }
  }

}
