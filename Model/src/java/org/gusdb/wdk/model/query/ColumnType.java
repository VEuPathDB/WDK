package org.gusdb.wdk.model.query;

import java.sql.Types;
import org.gusdb.wdk.model.query.TypeConverters.TypeConverter;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A enum of types for columns supported by WDK. The type the will mapped to the
 * actual type of a field in the database.
 * 
 * @author xingao
 */
public enum ColumnType {

  STRING(1999, Types.VARCHAR, TypeConverters.STRING_CONVERTER),
  NUMBER(12, Types.INTEGER, TypeConverters.INTEGER_CONVERTER),
  FLOAT(12, Types.FLOAT, TypeConverters.FLOAT_CONVERTER),
  BOOLEAN(1, Types.BOOLEAN, TypeConverters.BOOLEAN_CONVERTER),
  CLOB(0, Types.CLOB, TypeConverters.STRING_CONVERTER),
  DATE(0, Types.TIMESTAMP, TypeConverters.TIMESTAMP_CONVERTER);

  private int defaultWidth;
  private int sqlType;
  private TypeConverter converter;

  private ColumnType(int defaultWidth, int sqlType, TypeConverter converter) {
    this.defaultWidth = defaultWidth;
    this.sqlType = sqlType;
    this.converter = converter;
  }

  public String getType() {
    return name().toLowerCase();
  }

  /**
   * @return type of this column as java.sql.Types value
   */
  public int getSqlType() {
    return sqlType;
  }
  
  /**
   * @return the defaultWidth
   */
  public int getDefaultWidth() {
    return defaultWidth;
  }

  public Object convertStringToTypedValue(String value) {
    return converter.convert(value);
  }
  
  public boolean isText() {
    return (this == CLOB || this == DATE || this == STRING);
  }

  @Override
  public String toString() {
    return getType();
  }

  public static ColumnType parse(String name) throws WdkModelException {
    try {
      return valueOf(name.trim().toUpperCase());
    }
    catch (IllegalArgumentException | NullPointerException e) {
      throw new WdkModelException("Invalid column type: [" + name + "]", e);
    }
  }
}
