package org.gusdb.wdk.model.record.attribute;

import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModelException;

public abstract class ColumnAttributeField extends AttributeField {

  private Optional<AttributeFieldDataType> _dataType = Optional.empty();

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    return new MapBuilder<String, ColumnAttributeField>(_name, this).toMap();
  }

  @RngUndefined
  public void setDataType(AttributeFieldDataType type) throws WdkModelException {
    if (_dataType.isPresent() && !_dataType.get().equals(type)) {
      throw new WdkModelException("Each field can only be assigned a single " +
          "SQL column type.  This method was called with " + _dataType.get() +
          " and called again with " + type);
    }
    _dataType = Optional.of(type);
  }

  @Override
  public Optional<AttributeFieldDataType> getDataType() {
    return _dataType;
  }
}
