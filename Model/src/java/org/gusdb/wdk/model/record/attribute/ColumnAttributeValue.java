/**
 * 
 */
package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An wrapper to the actual value of a {@link Column} retrieved from a
 * {@link Query}.
 * 
 * @author Jerric Gao
 * 
 */
public class ColumnAttributeValue extends AttributeValue {

  /**
   * @param instance
   * @param field
   */
  public ColumnAttributeValue(ColumnAttributeField field, Object value) {
    super(field);
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeValue#getValue()
   */
  @Override
  public Object getValue() throws WdkModelException {
    return Utilities.parseValue(value);
  }

}
