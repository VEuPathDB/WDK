package org.gusdb.wdk.model.record.attribute;

import java.util.Collections;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

public class PkColumnAttributeField extends AttributeField {

  public PkColumnAttributeField() {
    // by default internal = true; can be overridden by WDK model XML declaration
    _internal = true;
  }

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException {
    return Collections.EMPTY_MAP;
  }

}
