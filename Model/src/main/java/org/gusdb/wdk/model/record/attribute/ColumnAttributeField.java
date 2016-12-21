package org.gusdb.wdk.model.record.attribute;

import java.util.Map;

import org.gusdb.fgputil.MapBuilder;

public abstract class ColumnAttributeField extends AttributeField {

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    return new MapBuilder<String, ColumnAttributeField>(_name, this).toMap();
  }

}
