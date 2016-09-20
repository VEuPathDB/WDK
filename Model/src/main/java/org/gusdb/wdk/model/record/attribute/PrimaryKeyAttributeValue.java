/**
 * Created on: Mar 18, 2005
 */
package org.gusdb.wdk.model.record.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
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
  
  /**
   * The display will be used in the summary and record page display. if a
   * display is not specified in the model, the text (in value) will be used as display.
   */
  private String display;


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
      String copy = Utilities.parseValue(pkValues.get(column));
      values.put(column, copy);
    }
    return values;
  }

  public String getValuesAsString() {
    StringBuilder s = new StringBuilder();
    for (String pkName : pkValues.keySet()) {
      s.append(pkName).append(" = ");
      s.append(pkValues.get(pkName)).append(", ");
    }
    return s.toString();
  }

  @Override
  public Object getValue() throws WdkModelException, WdkUserException {
    if (value == null) {
      try {
        String label = "attribute" + " [" + field.getName() + "] of ["
            + field.getRecordClass().getFullName() + "]";
				// NOTE: valueContainer is null when (.value) called in favorites.tag, changed to use (.display) anyway
        value = valueContainer.replaceMacrosWithAttributeValues(
          ((PrimaryKeyAttributeField)field).getText(),
          label
        );
      } catch (Exception ex) {
         throw new WdkModelException("Failed to substitute sub-fields.", ex);
      }
    }
    return value;
  }

@Override
  public String getDisplay() throws WdkModelException, WdkUserException {
    if (display == null) {
      if (valueContainer == null) {
        // may happen if PK attribute value is created independently without container
        // simply join PK values together
        display = FormatUtil.join(pkValues.values().toArray(), ", ");
      }
      else {
        try {
          // parse the text and look up other fields, so that primaryKey fields can support
          //   macros of other column attributes
          Map<String, Object> values = new LinkedHashMap<String, Object>(pkValues);
          Map<String, AttributeField> subFields = field.parseFields(((PrimaryKeyAttributeField)field).getDisplay());
          for (String fieldName : subFields.keySet()) {
            if (!values.containsKey(fieldName)) {
              AttributeValue fieldValue = valueContainer.getAttributeValue(fieldName);
              Object object = fieldValue.getValue();
              values.put(fieldName, (object == null) ? "" : object.toString());
            }
          }
          display = Utilities.replaceMacros(((PrimaryKeyAttributeField)field).getDisplay(), values);
        }
        catch (Exception ex) {
           logger.warn("Failed to substitute sub-fields.", ex);
           throw new WdkModelException(ex);
        }
      }
    }
    return display;
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
      if (pk.pkValues.size() != pkValues.size()) {
        return false;
      }
      for (String columnName : pkValues.keySet()) {
        if (!pk.pkValues.containsKey(columnName)) {
          return false;
        }
        Object otherValue = pk.pkValues.get(columnName);
        if (!pkValues.get(columnName).equals(otherValue)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 0;
    for (Object pkValue : pkValues.values()) {
      if (pkValue != null) {
        hashCode ^= pkValue.toString().hashCode();
      }
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
