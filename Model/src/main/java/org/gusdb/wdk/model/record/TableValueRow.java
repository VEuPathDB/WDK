package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.DynamicAttributeValueContainer;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;

public class TableValueRow extends DynamicAttributeValueContainer implements Map<String, AttributeValue> {

  private PrimaryKeyAttributeValue primaryKey;
  private Map<String, AttributeField> attributeFields;
  private TableField tableField;

  public TableValueRow(PrimaryKeyAttributeValue primaryKey, TableField tableField) {
    this.primaryKey = primaryKey;
    this.tableField = tableField;
    this.attributeFields = tableField.getAttributeFieldMap();
  }

  public void initializeFromResultList(ResultList resultList) throws WdkModelException {

    for (AttributeField field : tableField.getAttributeFields()) {
      if (!(field instanceof ColumnAttributeField)) continue;

      Object value = resultList.get(field.getName());
      ColumnAttributeValue attributeValue = new ColumnAttributeValue(
          (ColumnAttributeField) field, value);
      addAttributeValue(attributeValue);
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeValueContainer#getAttributeFieldMap()
   */
  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {
    return attributeFields;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#clear()
   */
  @Override
    public void clear() {
    throw new UnsupportedOperationException("Not supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  @Override
    public boolean containsKey(Object key) {
    return attributeFields.containsKey(key);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  @Override
    public boolean containsValue(Object value) {
    for (String name : attributeFields.keySet()) {

      AttributeValue attributeValue = get(name);
      if (attributeValue.equals(value)) return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#entrySet()
   */
  @Override
    public Set<Entry<String, AttributeValue>> entrySet() {
    Set<Entry<String, AttributeValue>> entries = new LinkedHashSet<Entry<String, AttributeValue>>();
    for (String name : attributeFields.keySet()) {
      AttributeValue value = get(name);
      entries.add(new TableValueRowEntry(name, value));
    }
    return entries;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  @Override
    public AttributeValue get(Object key) {
    try {
      return getAttributeValue((String) key);
    } catch (WdkModelException | WdkUserException ex) {
      throw new WdkRuntimeException(ex);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#isEmpty()
   */
  @Override
    public boolean isEmpty() {
    return attributeFields.isEmpty();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#keySet()
   */
  @Override
    public Set<String> keySet() {
    return attributeFields.keySet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  @Override
    public AttributeValue put(String key, AttributeValue value) {
    throw new UnsupportedOperationException("Not supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#putAll(java.util.Map)
   */
  @Override
    public void putAll(Map<? extends String, ? extends AttributeValue> values) {
    throw new UnsupportedOperationException("Not supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#remove(java.lang.Object)
   */
  @Override
    public AttributeValue remove(Object key) {
    throw new UnsupportedOperationException("Not supported");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#size()
   */
  @Override
    public int size() {
    return attributeFields.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#values()
   */
  @Override
    public Collection<AttributeValue> values() {
    List<AttributeValue> values = new ArrayList<AttributeValue>();
    for (String name : attributeFields.keySet()) {
      values.add(get(name));
    }
    return values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.AttributeValueContainer#fillColumnAttributeValues
   * (org.gusdb.wdk.model.query.Query)
   */
  @Override
    protected void fillColumnAttributeValues(Query attributeQuery)
    throws WdkModelException {
    // do nothing, since the data is filled by the parent TableValue
  }

  @Override
    protected PrimaryKeyAttributeValue getPrimaryKey() {
    return primaryKey;
  }

  private class TableValueRowEntry implements Entry<String, AttributeValue> {

    private String name;
    private AttributeValue value;

    public TableValueRowEntry(String name, AttributeValue value) {
      this.name = name;
      this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map.Entry#getKey()
     */
    @Override
      public String getKey() {
      return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map.Entry#getValue()
     */
    @Override
      public AttributeValue getValue() {
      return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    @Override
      public AttributeValue setValue(AttributeValue value) {
      this.value = value;
      return value;
    }

  }

}
