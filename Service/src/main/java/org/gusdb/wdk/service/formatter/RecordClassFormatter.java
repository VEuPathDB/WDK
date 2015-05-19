package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.json.JSONArray;
import org.json.JSONObject;

public class RecordClassFormatter {

  public static JSONArray getRecordClassesJson(RecordClassSet[] recordClassSets,
      boolean expandRecordClasses, boolean expandAttributes,
      boolean expandTables, boolean expandTableAttributes) {
    JSONArray json = new JSONArray();
    for (RecordClassSet rcSet : recordClassSets) {
      for (RecordClass rc : rcSet.getRecordClasses()) {
        json.put(expandRecordClasses ? getRecordClassJson(rc,
            expandAttributes, expandTables, expandTableAttributes) :
              rc.getFullName());
      }
    }
    return json;
  }

  public static JSONObject getRecordClassJson(RecordClass recordClass,
      boolean expandAttributes, boolean expandTables, boolean expandTableAttributes) {
    JSONObject json = new JSONObject();
    json.put("fullName", recordClass.getFullName());
    json.put("displayName", recordClass.getDisplayName());
    json.put("displayNamePlural", recordClass.getDisplayNamePlural());
    json.put("description", recordClass.getDescription());
    json.put("attributes", getAttributesJson(recordClass, expandAttributes));
    json.put("tables", getTablesJson(recordClass, expandTables, expandTableAttributes));
    return json;
  }

  public static JSONArray getAttributesJson(AttributeFieldContainer container, boolean expandAttributes) {
    JSONArray array = new JSONArray();
    for (AttributeField attrib : container.getAttributeFields()) {
      array.put(expandAttributes ? attrib.getName() : getAttributeJson(attrib));
    }
    return array;
  }

  public static JSONObject getAttributeJson(AttributeField attrib) {
    JSONObject json = new JSONObject();
    json.put("name", attrib.getName());
    json.put("type", attrib.getType());
    json.put("category", attrib.getAttributeCategory());
    json.put("displayName", attrib.getDisplayName());
    json.put("help", attrib.getHelp());
    json.put("align", attrib.getAlign());
    return json;
  }

  public static JSONArray getTablesJson(RecordClass recordClass,
      boolean expandTables, boolean expandAttributes) {
    JSONArray array = new JSONArray();
    for (TableField table : recordClass.getTableFields()) {
      array.put(expandTables ? table.getName() : getTableJson(table, expandAttributes));
    }
    return array;
  }

  public static JSONObject getTableJson(TableField table, boolean expandAttributes) {
    JSONObject json = new JSONObject();
    json.put("name", table.getName());
    json.put("type", table.getType());
    json.put("displayName", table.getDisplayName());
    json.put("description", table.getDescription());
    json.put("help", table.getHelp());
    json.put("attributes", getAttributesJson(table, expandAttributes));
    return json;
  }
}
