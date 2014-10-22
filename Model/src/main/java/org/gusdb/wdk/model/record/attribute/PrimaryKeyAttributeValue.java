/**
 * Created on: Mar 18, 2005
 */
package org.gusdb.wdk.model.record.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * A PrimaryKeyAttributeValue contains the column values that can identify a {@link RecordInstance} uniquely.
 * The number values stored here should match the number of columns defined in the
 * {@link PrimaryKeyAttributeField}.
 * 
 * @author Jerric
 */
public class PrimaryKeyAttributeValue extends AttributeValue {

  private final Map<String, Object> pkValues;
  private AttributeValueContainer valueContainer;
  
  public PrimaryKeyAttributeValue(PrimaryKeyAttributeField field, Map<String, Object> pkValues) {
    this(field, pkValues, null);
  }

  public PrimaryKeyAttributeValue(PrimaryKeyAttributeField field, Map<String, Object> pkValues,
      AttributeValueContainer valueContainer) {
    super(field);
    this.pkValues = new LinkedHashMap<String, Object>(pkValues);
    this.valueContainer = valueContainer;
  }
  
  public void setValueContainer(AttributeValueContainer valueContainer) {
    this.valueContainer = valueContainer;
  }

  public Map<String, String> getValues() {
    Map<String, String> values = new LinkedHashMap<String, String>();
    for (String column : pkValues.keySet()) {
      String value = Utilities.parseValue(pkValues.get(column));
      values.put(column, value);
    }
    return values;
  }

  @Override
  public Object getValue() throws WdkModelException, WdkUserException {
    if (value == null) {
      Map<String, Object> values = new LinkedHashMap<String, Object>(pkValues);

      try {
      // parse the text and look up other fields, so that primaryKey fields can support macros of other column
      // attributes.
      Map<String, AttributeField> subFields = field.parseFields(((PrimaryKeyAttributeField)field).getText());
      for (String fieldName : subFields.keySet()) {
        if (!values.containsKey(fieldName)) {
          AttributeValue value = valueContainer.getAttributeValue(fieldName);
          Object object = value.getValue();
          values.put(fieldName, (object == null) ? "" : object.toString());
        }
      }

      value = Utilities.replaceMacros(((PrimaryKeyAttributeField)field).getText(), values);
      } catch (Exception ex) {
         logger.error("Failed to substitute sub-fields.", ex);
         throw new WdkModelException(ex);
      }
    }
    return value;
  }

  /**
   * @param obj
   * @return
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PrimaryKeyAttributeValue) {
      PrimaryKeyAttributeValue pk = (PrimaryKeyAttributeValue) obj;

      for (String columnName : pkValues.keySet()) {
        if (!pk.pkValues.containsKey(columnName))
          return false;
        Object value = pk.pkValues.get(columnName);
        if (!pkValues.get(columnName).equals(value))
          return false;
      }
      return true;
    }
    else
      return false;
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 0;
    for(Object value : pkValues.values()) {
      if (value != null)
      hashCode ^= value.toString().hashCode();
    }
    return hashCode;
  }

  /**
   * @return
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      return (String) getValue();
    }
    catch (WdkModelException | WdkUserException ex) {
      throw new WdkRuntimeException(ex);
    }
  }
}
