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

  private Object _value;

  /**
   * @param instance
   * @param field
   */
  public ColumnAttributeValue(ColumnAttributeField field, Object value) {
    super(field);
    _value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeValue#getValue()
   */
  @Override
  public String getValue() throws WdkModelException {
    return Utilities.parseValue(_value);
  }

}
