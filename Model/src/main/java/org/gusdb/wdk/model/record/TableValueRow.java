package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gusdb.fgputil.ImmutableEntry;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.AttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.IdAttributeField;
import org.gusdb.wdk.model.record.attribute.IdAttributeValue;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

public class TableValueRow extends AttributeValueContainer {

  private static final long serialVersionUID = 1L;

  private final TableField _tableField;

  public TableValueRow(TableField tableField, ResultList resultList) throws WdkModelException {
    super(tableField.getAttributeFieldMap());
    _tableField = tableField;
    initializeFromResultList(resultList);
  }

  private void initializeFromResultList(ResultList resultList) throws WdkModelException {
    for (AttributeField field : _tableField.getAttributeFields()) {
      if (!(field instanceof QueryColumnAttributeField)) {
        continue;
      }
      Object value = resultList.get(field.getName());
      QueryColumnAttributeValue attributeValue = new QueryColumnAttributeValue(
          (QueryColumnAttributeField) field, value);
      addAttributeValue(attributeValue);
    }
  }

  @Override
  public QueryColumnAttributeValue getQueryColumnAttributeValue(QueryColumnAttributeField field)
      throws WdkModelException, WdkUserException {
    if (!containsKey(field.getName())) {
      throw new WdkModelException("Requested column attribute [" + field.getName() + " not loaded into container.");
    }
    return (QueryColumnAttributeValue)get(field.getName());
  }

  @Override
  public IdAttributeValue getIdAttributeValue(IdAttributeField field) {
    throw new UnsupportedOperationException("Table rows cannot have ID attributes");
  }

  /***************************************************************
   * The methods below are overridden because any field in the field
   * map should be accessible once an instance of this class is populated.
   ***************************************************************/

  @Override
  public boolean containsKey(Object key) {
    return _attributeFieldMap.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return _attributeFieldMap.isEmpty();
  }

  @Override
  public int size() {
    return _attributeFieldMap.size();
  }

  @Override
  public Set<String> keySet() {
    return _attributeFieldMap.keySet();
  }

  @Override
  public Set<Entry<String,AttributeValue>> entrySet() {
    Set<Entry<String, AttributeValue>> entries = new HashSet<>();
    for (String name : _attributeFieldMap.keySet()) {
      entries.add(new ImmutableEntry<String, AttributeValue>(name, get(name)));
    }
    return entries;
  }

  @Override
  public Collection<AttributeValue> values() {
    List<AttributeValue> values = new ArrayList<AttributeValue>();
    for (String name : _attributeFieldMap.keySet()) {
      values.add(get(name));
    }
    return values;
  }
}
