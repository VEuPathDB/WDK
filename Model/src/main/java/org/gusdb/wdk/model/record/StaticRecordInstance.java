package org.gusdb.wdk.model.record;

import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.record.attribute.AttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;
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
    _recordClass = recordClass;
    _isValidRecord = true;

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
    return mapToList(_tableValueCache.keySet(), tableName -> {
      try {
        return _recordClass.getTableField(tableName);
      }
      catch (WdkModelException e) {
        throw new WdkRuntimeException("Cannot find table field '" + tableName +
            "' in '" + _recordClass.getFullName() + "'.");
      }
    });
  }

  @Override
  public TableValue getTableValue(String tableName) throws WdkModelException, WdkUserException {
    if (!_tableValueCache.containsKey(tableName)) {
      throw new WdkModelException("Requested table attribute [" + tableName + "] is not loaded into container.");
    }
    return _tableValueCache.get(tableName);
  }

  @Override
  public QueryColumnAttributeValue getQueryColumnAttributeValue(QueryColumnAttributeField field)
      throws WdkModelException, WdkUserException {
    if (!containsKey(field.getName())) {
      throw new WdkModelException("Requested column attribute [" + field.getName() + "] is not loaded into container.");
    }
    return (QueryColumnAttributeValue)get(field.getName());
  }

  public void addTableValue(TableValue tableValue) {
    _tableValueCache.put(tableValue.getTableField().getName(), tableValue);
  }

  @Override
  public IdAttributeValue getIdAttributeValue() throws WdkModelException, WdkUserException {
    return (IdAttributeValue) getAttributeValue(_recordClass.getIdAttributeField().getName());
  }

  @Override
  public IdAttributeValue getIdAttributeValue(IdAttributeField field) {
    return new IdAttributeValue(field, this);
  }

  @Override
  public void removeTableValue(String tableName) {
    _tableValueCache.remove(tableName);
  }
}
