package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    id: String,
    attributes: [
     { name: String, value: Any }
    ],
    tables: [
      { 
        name: String,
        value: [ { name: String, value: Any } ]
      }
    ]
  } 
}
*/
public class RecordFormatter {
  public static JSONObject formatAnswer(RecordInstance recordInstance, Set<String> attributeNames, Set<String> tableNames) throws WdkModelException {
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

  public static JSONObject getMetaData(RecordInstance recordInstance, Set<String> attributeNames, Set<String> tableNames) {
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
      attributes.put(tableJson);
    }
    meta.put("tables", tables);
   
    return meta;
  }

  public static JSONObject getRecordJson(RecordInstance record)
      throws WdkModelException, WdkUserException {
    JSONObject json = new JSONObject();
    json.put("id", record.getPrimaryKey().getValue());
    JSONArray attributes = new JSONArray();
    for (Entry<String,AttributeValue> attrib : record.getAttributeValueMap().entrySet()) {
      JSONObject attribJson = new JSONObject();
      attribJson.put("name", attrib.getKey());
      attribJson.put("value", getAttributeJsonValue(attrib.getValue()));
      attributes.put(attribJson);
    }
    json.put("attributes", attributes);

    // FIXME: This can probably be cleaned up / refactored
    JSONArray tables = new JSONArray();
    for (Entry<String, TableValue> table : record.getTables().entrySet()) {
      JSONArray tableRowsJSON = new JSONArray();
      for(Map<String, AttributeValue> row : table.getValue()) {
        JSONArray tableAttrsJSON = new JSONArray();
        for (Entry<String, AttributeValue> entry : row.entrySet()) {
          if (!entry.getValue().getAttributeField().isInternal()) {
            JSONObject tableAttrJSON = new JSONObject();
             tableAttrJSON.put("name", entry.getKey());
             tableAttrJSON.put("value", getAttributeJsonValue(entry.getValue()));
             tableAttrsJSON.put(tableAttrJSON);
          }
        }
        tableRowsJSON.put(tableAttrsJSON);
      }
      JSONObject tableJson = new JSONObject();
      tableJson.put("name", table.getKey());
      tableJson.put("rows", tableRowsJSON);
      tables.put(tableJson);
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
