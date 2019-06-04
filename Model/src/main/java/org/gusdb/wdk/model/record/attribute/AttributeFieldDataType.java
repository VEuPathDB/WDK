package org.gusdb.wdk.model.record.attribute;

import java.util.Arrays;
import java.util.Optional;

import org.gusdb.fgputil.db.DbColumnType;
import org.gusdb.wdk.model.query.ColumnType;

public enum AttributeFieldDataType {
  STRING,
  NUMBER,
  DATE,
  OTHER;

  public static AttributeFieldDataType fromColumnType(final ColumnType type) {
    return fromSqlType(type.getSqlType());
  }

  public static Optional<AttributeFieldDataType> fromString(final String val) {
    final String test = val.toUpperCase();
    return Arrays.stream(values())
      .filter(e -> e.name().equals(test))
      .findFirst();
  }

  public static AttributeFieldDataType fromSqlType(int sqlType) {
    switch(DbColumnType.getFromSqlType(sqlType)) {
      case STRING:
        return STRING;
      case DATE_TIME:
        return DATE;
      case DOUBLE:
      case LONG_INT:
        return NUMBER;
      case CLOB:
      case BINARY_DATA:
      case BOOLEAN:
      case OTHER:
      default:
        return OTHER;
    }
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
