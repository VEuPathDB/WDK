package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.ImmutableEntry;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * <p>
 * This container will cache the {@link AttributeValue}s once they are retrieved
 * for future reuse.  A primary key attribute is required.  How column attribute
 * values are retrieved is decided by the subclass.
 * </p>
 * <p>
 * It creates the actual {@link AttributeValue} objects, and when an
 * {@link AttributeField} has embedded other {@link AttributeField}s, the
 * corresponding {@link AttributeValue} will use the container to resolve the
 * values of those embedded {@link AttributeField}s.
 * </p>
 *
 * @author xingao
 * @author rdoherty
 *
 */
public abstract class AttributeValueContainer extends LinkedHashMap<String, AttributeValue> implements AttributeValueMap {

  private static final long serialVersionUID = 1L;

  protected final Map<String,AttributeField> _attributeFieldMap;

  public abstract IdAttributeValue getIdAttributeValue(IdAttributeField field);

  public abstract QueryColumnAttributeValue getQueryColumnAttributeValue(QueryColumnAttributeField field)
      throws WdkModelException, WdkUserException;

  public AttributeValueContainer(Map<String,AttributeField> attributeFieldMap) {
    _attributeFieldMap = attributeFieldMap;
  }

  public Map<String, AttributeField> getAttributeFieldMap() {
    return _attributeFieldMap;
  }

  @Override
  public void addAttributeValue(AttributeValue value) {
    put(value.getName(), value);
  }

  /**
   * Get existing attribute value from cache, or create one if the value doesn't exist.
   */
  @Override
  public AttributeValue getAttributeValue(String fieldName) throws WdkModelException, WdkUserException {
    // get the field from the map
    AttributeField field = _attributeFieldMap.get(fieldName);
    if (field == null) {
      throw new WdkModelException("The attribute field [" + fieldName + "] cannot be found in this container.");
    }

    // if value already exists in this container
    if (super.containsKey(fieldName)) {
      return super.get(fieldName);
    }

    // value not present; try to generate it
    if (field instanceof PkColumnAttributeField) {
      // throw exception; PK column values should always be present in a container
      throw new WdkModelException("Value for PK column '" + field.getName() + "' not present in attribute value container.");
    }
    if (field instanceof IdAttributeField) {
      return addValue(getIdAttributeValue((IdAttributeField) field));
    }
    if (field instanceof LinkAttributeField) {
      return addValue(new LinkAttributeValue((LinkAttributeField) field, this));
    }
    if (field instanceof TextAttributeField) {
      return addValue(new TextAttributeValue((TextAttributeField) field, this));
    }
    if (field instanceof QueryColumnAttributeField) {
      return addValue(getQueryColumnAttributeValue((QueryColumnAttributeField) field));
    }

    // not a supported attribute field type
    throw new WdkModelException("Unsupported attribute field type for : " + fieldName);
  }

  private AttributeValue addValue(AttributeValue value) {
    addAttributeValue(value);
    return value;
  }

  @Override
  public boolean containsKey(Object key) {
    return _attributeFieldMap.containsKey(key);
  }

  @Override
  public AttributeValue get(Object key) {
    try {
      return getAttributeValue((String) key);
    }
    catch (WdkModelException | WdkUserException ex) {
      throw new WdkRuntimeException(ex);
    }
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
