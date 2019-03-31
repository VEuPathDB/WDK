package org.gusdb.wdk.model.record.attribute;

import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.db.SqlColumnType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;

public abstract class ColumnAttributeField extends AttributeField {

  private Optional<SqlColumnType> _sqlType = Optional.empty();

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    return new MapBuilder<String, ColumnAttributeField>(_name, this).toMap();
  }

  public void setSqlColumnType(SqlColumnType type) throws WdkModelException {
    if (_sqlType.isPresent() && !_sqlType.get().equals(type)) {
      throw new WdkModelException("Each field can only be assigned a single " +
          "SQL column type.  This method was called with " + _sqlType.get() +
          " and called again with " + type);
    }
    _sqlType = Optional.of(type);
  }

  @Override
  @RngUndefined
  public Optional<SqlColumnType> getSqlColumnType() {
    return _sqlType;
  }
}
