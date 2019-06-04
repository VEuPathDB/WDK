package org.gusdb.wdk.model.record.attribute;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;

import java.util.Map;
import java.util.Objects;

public abstract class ColumnAttributeField extends AttributeField {

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    return new MapBuilder<>(_name, this).toMap();
  }

  @RngUndefined
  public void setDataType(AttributeFieldDataType type) {
    this._dataType = Objects.requireNonNull(type);
  }
}
