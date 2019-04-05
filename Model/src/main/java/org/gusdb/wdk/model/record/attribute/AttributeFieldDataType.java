package org.gusdb.wdk.model.record.attribute;

import java.util.Arrays;
import java.util.Optional;

import org.gusdb.fgputil.db.DbColumnType;

public enum AttributeFieldDataType {
  STRING,
  NUMBER,
  DATE,
  OTHER;

  public static Optional<AttributeFieldDataType> fromString(final String val) {
    final String test = val.toUpperCase();
    return Arrays.stream(values())
      .filter(e -> e.name().equals(test))
      .findFirst();
  }

  public static AttributeFieldDataType getFromSqlType(int sqlType) {
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
}
