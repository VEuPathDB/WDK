package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
  public static JSONObject formatRecord(RecordInstance recordInstance, List<String> attributeNames, List<String> tableNames) throws WdkModelException {
    try {
      JSONObject parent = new JSONObject();
      parent.put("meta", getMetaData(recordInstance, attributeNames, tableNames));
      parent.put("record", getRecordJson(recordInstance));
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
    JSONObject json = new JSONObject();
    json.put("id", record.getPrimaryKey().getValues());
    JSONObject attributes = new JSONObject();
    for (Entry<String,AttributeValue> attrib : record.getAttributeValueMap().entrySet()) {
      attributes.put(attrib.getKey(), getAttributeJsonValue(attrib.getValue()));
    }
    json.put("attributes", attributes);

    // FIXME: This can probably be cleaned up / refactored
    JSONObject tables = new JSONObject();
    for (Entry<String, TableValue> table : record.getTables().entrySet()) {
      JSONArray tableRowsJSON = new JSONArray();
      for(Map<String, AttributeValue> row : table.getValue()) {
        JSONObject tableAttrsJSON = new JSONObject();
        for (Entry<String, AttributeValue> entry : row.entrySet()) {
          if (!entry.getValue().getAttributeField().isInternal()) {
             tableAttrsJSON.put(entry.getKey(), getAttributeJsonValue(entry.getValue()));
          }
        }
        tableRowsJSON.put(tableAttrsJSON);
      }
      tables.put(table.getKey(), tableRowsJSON);
    }
    json.put("tables", tables);
    return json;
  }
  
  private static Object getAttributeJsonValue(AttributeValue attr) throws
    WdkModelException, WdkUserException {
    if (attr instanceof LinkAttributeValue) {
      LinkAttributeValue linkAttr = (LinkAttributeValue) attr;
      JSONObject value = new JSONObject();
      value.put("url",  linkAttr.getUrl());
      value.put("displayText", linkAttr.getDisplayText());
      return value;
    }
    else {
      return attr.getValue();
    }
  }

}
