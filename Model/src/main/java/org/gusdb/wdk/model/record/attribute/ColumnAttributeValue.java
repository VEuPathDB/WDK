package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An wrapper to the actual value of a {@link org.gusdb.wdk.model.query.Column} retrieved from a
 * {@link org.gusdb.wdk.model.query.Query}.
 * 
 * @author Jerric Gao
 */
public abstract class ColumnAttributeValue extends AttributeValue {

  private final Object _value;

  public ColumnAttributeValue(ColumnAttributeField field, Object value) {
    super(field);
    _value = value;
  }

  @Override
  public String getValue() throws WdkModelException {
    return Utilities.parseValue(_value);
  }

}
