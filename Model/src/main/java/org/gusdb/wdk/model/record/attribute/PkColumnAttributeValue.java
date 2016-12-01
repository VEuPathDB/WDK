package org.gusdb.wdk.model.record.attribute;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;

public class PkColumnAttributeValue extends AttributeValue {

  private final Object _value;

  public PkColumnAttributeValue(AttributeField field, Object value) {
    super(field);
    _value = value;
  }

  @Override
  public String getValue() throws WdkModelException {
    return Utilities.parseValue(_value);
  }
}
