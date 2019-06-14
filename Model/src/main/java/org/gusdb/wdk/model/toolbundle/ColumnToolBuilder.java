package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.util.Map;

public interface ColumnToolBuilder<T extends ColumnTool> {

  ColumnToolBuilder<T> setKey(String key);

  /**
   * @return the AttributeFieldDataType supported by this column tool.
   */
  AttributeFieldDataType getColumnType();

  /**
   * @return an immutable reference to this tool definition's defined
   * properties.
   */
  Map<String, String> getProperties();

  /**
   * Set the column data type to be handled by this column tool.
   *
   * @param type
   *   string name of an {@link AttributeFieldDataType} from the model XML.
   *
   * @return the updated ColumnToolBuilder
   */
  ColumnToolBuilder<T> setColumnType(AttributeFieldDataType type);

  /**
   * Appends an XML property to this column tool builder's
   * internal property map.
   *
   * @param prop
   *   named XML property
   *
   * @return the updated ColumnToolBuilder
   */
  @SuppressWarnings("unused") // Referenced by ModelXmlParser
  ColumnToolBuilder<T> addProperty(WdkModelText prop);

  ColumnToolBuilder<T> setImplementation(String path);

  T build(WdkModel wdk) throws WdkModelException;
}
