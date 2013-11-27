/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.sql.Types;
import org.gusdb.wdk.model.query.TypeConverters.TypeConverter;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A enum of types for columns supported by WDK. The type the will mapped to the
 * actual type of a field in the database.
 * 
 * @author xingao
 * 
 */
public enum ColumnType {

  STRING("string", 1999, Types.VARCHAR, TypeConverters.STRING_CONVERTER),
  NUMBER("number", 12, Types.INTEGER, TypeConverters.INTEGER_CONVERTER),
  FLOAT("float", 12, Types.FLOAT, TypeConverters.FLOAT_CONVERTER),
  BOOLEAN("boolean", 1, Types.BOOLEAN, TypeConverters.BOOLEAN_CONVERTER),
  CLOB("clob", 0, Types.CLOB, TypeConverters.STRING_CONVERTER),
  DATE("date", 0, Types.TIMESTAMP, TypeConverters.TIMESTAMP_CONVERTER);

  private String type;
  private int defaultWidth;
  private int sqlType;
  private TypeConverter converter;

  private ColumnType(String type, int defaultWidth, int sqlType, TypeConverter converter) {
    this.type = type;
    this.defaultWidth = defaultWidth;
    this.sqlType = sqlType;
    this.converter = converter;
  }

  public String getType() {
    return type;
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
    return type;
  }

  public static ColumnType parse(String name) throws WdkModelException {
    name = name.trim().toLowerCase();

    if (name.equals(STRING.type))
      return STRING;
    else if (name.equals(NUMBER.type))
      return NUMBER;
    else if (name.equals(FLOAT.type))
      return FLOAT;
    else if (name.equals(CLOB.type))
      return CLOB;
    else if (name.equals(BOOLEAN.type))
      return BOOLEAN;
    else if (name.equals(DATE.type))
      return DATE;
    else
      throw new WdkModelException("Invalid column type: [" + name + "]");
  }
}
