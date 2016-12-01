package org.gusdb.wdk.model.record;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.AttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeValue;
import org.gusdb.wdk.model.user.User;

public class StaticRecordInstance extends AttributeValueContainer implements RecordInstance {

  private static final long serialVersionUID = 1L;

  protected RecordClass _recordClass;

  protected PrimaryKeyValue _primaryKey;

  protected Map<String, TableValue> _tableValueCache = new LinkedHashMap<String, TableValue>();

  protected boolean _isValidRecord;

  public StaticRecordInstance(User user, RecordClass recordClass, AttributeFieldContainer fieldContainer,
      Map<String, Object> pkValues, boolean translatePk) throws WdkModelException, WdkUserException {
    super(fieldContainer.getAttributeFieldMap());
    this._recordClass = recordClass;
    this._isValidRecord = true;

    if (translatePk) {
      List<Map<String, Object>> records = recordClass.lookupPrimaryKeys(user, pkValues);
      if (records.size() != 1) {
        throw new WdkUserException("The primary key doesn't map to singular record: " + pkValues);
      }
      pkValues = records.get(0);
    }

    // set primary key value and PK column attribute values
    _primaryKey = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), pkValues);
    for (String columnRef : recordClass.getPrimaryKeyDefinition().getColumnRefs()) {
      PkColumnAttributeField pkColumnField = (PkColumnAttributeField)_attributeFieldMap.get(columnRef);
      addAttributeValue(new PkColumnAttributeValue(pkColumnField, pkValues.get(columnRef)));
    }
  }

  @Override
  public boolean isValidRecord() {
    return _isValidRecord;
  }

  @Override
  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public AttributeField getAttributeField(String fieldName)
      throws WdkModelException {
    Map<String, AttributeField> attributeFields = getAttributeFieldMap();
    if (!attributeFields.containsKey(fieldName))
      throw new WdkModelException("The attribute field '" + fieldName
          + "' does not exist in record instance for class " + _recordClass.getFullName());
    return attributeFields.get(fieldName);
  }

  @Override
  public PrimaryKeyValue getPrimaryKey() {
    return _primaryKey;
  }

  /**
   * @return Map of attributeName -> AttributeFieldValue
   * @throws WdkUserException
   */
  @Override
  public Map<String, AttributeValue> getAttributeValueMap()
      throws WdkModelException, WdkUserException {
    Map<String, AttributeField> fields = getAttributeFieldMap();
    Map<String, AttributeValue> values = new LinkedHashMap<String, AttributeValue>();

    for (AttributeField field : fields.values()) {
      String name = field.getName();
      values.put(name, getAttributeValue(name));
    }
    return values;
  }

  @Override
  public String[] getSummaryAttributeNames() {
    Map<String, AttributeField> summaryFields = getAttributeFieldMap();
    String[] names = new String[summaryFields.size()];
    summaryFields.keySet().toArray(names);
    return names;
  }

  public Map<String, AttributeValue> getSummaryAttributeValueMap()
      throws WdkModelException, WdkUserException {
    return getAttributeValueMap();
  }

  @Override
  public Map<String, TableValue> getTableValueMap() throws WdkModelException, WdkUserException {
    Map<String, TableValue> values = new LinkedHashMap<String, TableValue>();
    for (TableField field : getAvailableTableFields()) {
      String name = field.getName();
      TableValue value = getTableValue(name);
      values.put(name, value);
    }
    return values;
  }

  protected Collection<TableField> getAvailableTableFields() {
    return Functions.mapToList(_tableValueCache.keySet(), new Function<String, TableField>() {
      @Override public TableField apply(String tableName) {
        try {
          return _recordClass.getTableField(tableName);
        }
        catch (WdkModelException e) {
          throw new WdkRuntimeException("Cannot find table field '" + tableName +
              "' in '" + _recordClass.getFullName() + "'.");
        }
      }
    });
  }

  @Override
  public TableValue getTableValue(String tableName) throws WdkModelException, WdkUserException {
    if (_tableValueCache.containsKey(tableName)) {
      return _tableValueCache.get(tableName);
    }
    throw new WdkModelException("Requested table attribute [" + tableName + "] is not loaded into container.");
  }

  @Override
  public ColumnAttributeValue getColumnAttributeValue(ColumnAttributeField field)
      throws WdkModelException, WdkUserException {
    if (containsKey(field.getName())) {
      throw new WdkModelException("Requested column attribute [" + field.getName() + "] is not loaded into container.");
    }
    return (ColumnAttributeValue)get(field.getName());
  }

  public void addTableValue(TableValue tableValue) {
    _tableValueCache.put(tableValue.getTableField().getName(), tableValue);
  }

  @Override
  public String print() throws WdkModelException, WdkUserException {

    String newline = System.getProperty("line.separator");
    StringBuilder buf = new StringBuilder();

    Map<String, AttributeValue> attributeValues = getAttributeValueMap();

    Map<String, AttributeValue> summaryAttributeValues = new LinkedHashMap<String, AttributeValue>();
    Map<String, AttributeValue> nonSummaryAttributeValues = new LinkedHashMap<String, AttributeValue>();

    splitSummaryAttributeValue(attributeValues, summaryAttributeValues,
        nonSummaryAttributeValues);

    printAtts_Aux(buf, summaryAttributeValues);
    printAtts_Aux(buf, nonSummaryAttributeValues);

    Map<String, TableValue> tableValues = getTableValueMap();
    for (TableValue tableValue : tableValues.values()) {
      String displayName = tableValue.getTableField().getDisplayName();
      buf.append(newline);
      buf.append("[Table]: " + displayName).append(newline);
      tableValue.write(buf);
    }

    buf.append(newline);
    return buf.toString();
  }
  
  @Override
  public String printSummary() throws WdkModelException, WdkUserException {

    StringBuilder buf = new StringBuilder();

    Map<String, AttributeValue> attributeValues = getAttributeValueMap();

    Map<String, AttributeValue> summaryAttributeValues = new LinkedHashMap<String, AttributeValue>();
    Map<String, AttributeValue> nonSummaryAttributeValues = new LinkedHashMap<String, AttributeValue>();

    splitSummaryAttributeValue(attributeValues, summaryAttributeValues,
        nonSummaryAttributeValues);

    printAtts_Aux(buf, summaryAttributeValues);
    return buf.toString();
  }

  public String toXML() throws WdkModelException, WdkUserException {
    return toXML("");
  }

  @Override
  public String toXML(String ident) throws WdkModelException, WdkUserException {
    String newline = System.getProperty("line.separator");
    StringBuilder buf = new StringBuilder();

    String rootStart = ident + "<" + getRecordClass().getFullName() + ">"
        + newline + ident + "<li>" + newline;
    String rootEnd = ident + "</li>" + newline + ident + "</"
        + getRecordClass().getFullName() + ">" + newline;
    ident = ident + "    ";
    buf.append(rootStart);

    Map<String, AttributeValue> attributeFields = getAttributeValueMap();
    for (String fieldName : attributeFields.keySet()) {
      AttributeValue value = attributeFields.get(fieldName);
      AttributeField field = value.getAttributeField();
      buf.append(ident + "<" + field.getName() + ">" + value.getValue() + "</"
          + field.getName() + ">" + newline);
    }

    Map<String, TableValue> tableFields = getTableValueMap();
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

  @Override
  public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException {
    return (IdAttributeValue) getAttributeValue(_recordClass.getIdAttributeField().getName());
  }

  @Override
  public IdAttributeValue getIdAttributeValue(IdAttributeField field) {
    return new IdAttributeValue(field, this);
  }
}
