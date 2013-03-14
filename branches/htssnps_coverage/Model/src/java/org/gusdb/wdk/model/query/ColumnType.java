/**
 * 
 */
package org.gusdb.wdk.model.query;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A enum of types for columns supported by WDK. The type the will mapped to the
 * actual type of a field in the database.
 * 
 * @author xingao
 * 
 */
public enum ColumnType {

  STRING("string", 1999), NUMBER("number", 12), FLOAT("float", 12), BOOLEAN(
      "boolean", 1), CLOB("clob", 0), DATE("date", 0);

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

  private String type;
  private int defaultWidth;

  /**
     * 
     */
  private ColumnType(String type, int defaultWidth) {
    this.type = type;
    this.defaultWidth = defaultWidth;
  }

  public String getType() {
    return type;
  }

  /**
   * @return the defaultWidth
   */
  public int getDefaultWidth() {
    return defaultWidth;
  }

  public boolean isText() {
    return (this == CLOB || this == DATE || this == STRING);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return type;
  }

}
