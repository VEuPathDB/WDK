package org.gusdb.wdk.service.formatter;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.xml.XmlAnswerValue;
import org.gusdb.wdk.model.xml.XmlAttributeValue;
import org.gusdb.wdk.model.xml.XmlRecordInstance;
import org.gusdb.wdk.model.xml.XmlRowValue;
import org.gusdb.wdk.model.xml.XmlTableValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Collectors;

public class XmlAnswerFormatter {

  public static JSONObject formatXmlAsnwerValue(XmlAnswerValue xmlAnswerValue) {
    return new JSONObject()
        .put(JsonKeys.RECORDS, formatRecords(xmlAnswerValue.getRecordInstances()));
  }

  private static JSONArray formatRecords(XmlRecordInstance[] records) {
    return new JSONArray(Arrays.stream(records)
        .map(record -> new JSONObject()
            .put(JsonKeys.ID, record.getId())
            .put(JsonKeys.RECORD_CLASS_NAME, record.getRecordClass().getFullName())
            .put(JsonKeys.ATTRIBUTES, formatAttributes(record.getAttributes()))
            .put(JsonKeys.TABLES, formatTables(record.getTables())))
        .toArray());
  }

  private static JSONObject formatAttributes(XmlAttributeValue[] attributeValues) {
    return new JSONObject(Arrays.stream(attributeValues)
        .collect(Collectors.toMap(XmlAttributeValue::getName, XmlAttributeValue::getValue)));
  }

  private static JSONObject formatTables(XmlTableValue[] tableValues) {
    return new JSONObject(Arrays.stream(tableValues)
        .collect(Collectors.toMap(XmlTableValue::getName, XmlAnswerFormatter::formatTableAttributes)));
  }

  private static JSONArray formatTableAttributes(XmlTableValue tableValue) {
    return new JSONArray(Arrays.stream(tableValue.getRows())
        .map(XmlRowValue::getColumns)
        .map(XmlAnswerFormatter::formatAttributes)
        .toArray());
  }

}
