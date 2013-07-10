/**
 * Created on: Mar 18, 2005
 */
package org.gusdb.wdk.model.record.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.record.RecordInstance;

/**
 * A PrimaryKeyAttributeValue contains the column values that can identify a
 * {@link RecordInstance} uniquely. The number values stored here should match
 * the number of columns defined in the {@link PrimaryKeyAttributeField}.
 * 
 * @author Jerric
 */
public class PrimaryKeyAttributeValue extends AttributeValue {

  private PrimaryKeyAttributeField field;
  private Map<String, Object> pkValues;

  public PrimaryKeyAttributeValue(PrimaryKeyAttributeField field,
      Map<String, Object> pkValues) {
    super(field);
    this.field = field;
    this.pkValues = new LinkedHashMap<String, Object>(pkValues);
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
  public Object getValue() throws WdkModelException {
    if (value == null)
      value = Utilities.replaceMacros(field.getText(), pkValues);
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
    } else
      return false;
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    try {
      return getValue().hashCode();
    } catch (WdkModelException e) {
      throw new WdkRuntimeException(e);
    }
  }

  /**
   * @return
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      return (String) getValue();
    } catch (WdkModelException ex) {
      throw new WdkRuntimeException(ex);
    }
  }
}
