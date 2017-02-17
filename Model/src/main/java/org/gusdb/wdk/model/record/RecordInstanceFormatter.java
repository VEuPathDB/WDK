package org.gusdb.wdk.model.record;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

public class RecordInstanceFormatter {

  public static String print(RecordInstance record) throws WdkModelException, WdkUserException {

    String newline = System.getProperty("line.separator");
    StringBuilder buf = new StringBuilder();

    Map<String, AttributeValue> attributeValues = getAttributeValueMap(record);

    Map<String, AttributeValue> summaryAttributeValues = new LinkedHashMap<String, AttributeValue>();
    Map<String, AttributeValue> nonSummaryAttributeValues = new LinkedHashMap<String, AttributeValue>();

    splitSummaryAttributeValue(attributeValues, summaryAttributeValues,
        nonSummaryAttributeValues);

    printAtts_Aux(buf, summaryAttributeValues);
    printAtts_Aux(buf, nonSummaryAttributeValues);

    Map<String, TableValue> tableValues = record.getTableValueMap();
    for (TableValue tableValue : tableValues.values()) {
      String displayName = tableValue.getTableField().getDisplayName();
      buf.append(newline);
      buf.append("[Table]: " + displayName).append(newline);
      tableValue.write(buf);
    }

    buf.append(newline);
    return buf.toString();
  }

  public static String printSummary(RecordInstance record) throws WdkModelException, WdkUserException {

    StringBuilder buf = new StringBuilder();

    Map<String, AttributeValue> attributeValues = getAttributeValueMap(record);

    Map<String, AttributeValue> summaryAttributeValues = new LinkedHashMap<String, AttributeValue>();
    Map<String, AttributeValue> nonSummaryAttributeValues = new LinkedHashMap<String, AttributeValue>();

    splitSummaryAttributeValue(attributeValues, summaryAttributeValues,
        nonSummaryAttributeValues);

    printAtts_Aux(buf, summaryAttributeValues);
    return buf.toString();
  }

  public static String toXML(RecordInstance record) throws WdkModelException, WdkUserException {
    return toXML(record, "");
  }

  public static String toXML(RecordInstance record, String ident) throws WdkModelException, WdkUserException {
    String newline = System.getProperty("line.separator");
    StringBuilder buf = new StringBuilder();

    String rootStart = ident + "<" + record.getRecordClass().getFullName() + ">"
        + newline + ident + "<li>" + newline;
    String rootEnd = ident + "</li>" + newline + ident + "</"
        + record.getRecordClass().getFullName() + ">" + newline;
    ident = ident + "    ";
    buf.append(rootStart);

    Map<String, AttributeValue> attributeFields = getAttributeValueMap(record);
    for (String fieldName : attributeFields.keySet()) {
      AttributeValue value = attributeFields.get(fieldName);
      AttributeField field = value.getAttributeField();
      buf.append(ident + "<" + field.getName() + ">" + value.getValue() + "</"
          + field.getName() + ">" + newline);
    }

    Map<String, TableValue> tableFields = record.getTableValueMap();
    for (String fieldName : tableFields.keySet()) {
      buf.append(ident + "<" + fieldName + ">" + newline);

      TableValue tableValue = tableFields.get(fieldName);
      tableValue.toXML(buf, "li", ident);
      buf.append(ident + "</" + fieldName + ">" + newline);
    }

    buf.append(rootEnd);

    return buf.toString();
  }

  /**
   * @return Map of attributeName -> AttributeFieldValue
   * @throws WdkUserException
   */
  private static Map<String, AttributeValue> getAttributeValueMap(RecordInstance record)
      throws WdkModelException, WdkUserException {
    Map<String, AttributeField> fields = record.getAttributeFieldMap();
    Map<String, AttributeValue> values = new LinkedHashMap<String, AttributeValue>();

    for (AttributeField field : fields.values()) {
      String name = field.getName();
      values.put(name, record.getAttributeValue(name));
    }
    return values;
  }

  /**
   * Given a map of all attributes in this recordInstance, separate them into
   * those that are summary attributes and those that are not summary
   * attributes. Place results into summaryAttributes and nonSummaryAttributes.
   */
  private static void splitSummaryAttributeValue(
      Map<String, AttributeValue> attributes,
      Map<String, AttributeValue> summaryAttributes,
      Map<String, AttributeValue> nonSummaryAttributes) {
    for (String fieldName : attributes.keySet()) {
      AttributeValue attribute = attributes.get(fieldName);
      if (attribute.getAttributeField().isInternal()) {
        summaryAttributes.put(fieldName, attribute);
      } else {
        nonSummaryAttributes.put(fieldName, attribute);
      }
    }
  }

  private static void printAtts_Aux(StringBuilder buf,
      Map<String, AttributeValue> attributes) throws WdkModelException, WdkUserException {
    String newline = System.getProperty("line.separator");
    for (String attributeName : attributes.keySet()) {
      AttributeValue attribute = attributes.get(attributeName);
      buf.append(attribute.getAttributeField().getDisplayName());
      buf.append(":   " + attribute.getBriefDisplay());
      buf.append(newline);
    }
  }
}
