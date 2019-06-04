package org.gusdb.wdk.model.bundle.impl;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.bundle.ColumnTool;
import org.gusdb.wdk.model.bundle.ColumnToolBuilder;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;

/**
 * Reference to the implementation of a ColumnTool
 *
 * @param <T>
 */
abstract class AbstractColumnToolBuilder<T extends ColumnTool>
implements ColumnToolBuilder<T> {

  private static final String ERR_INVALID_CLASS = "Invalid implementation "
    + "class path \"%s\"";

  private static final String ERR_NO_TYPE = "No target data type set on column "
    + "tool builder";

  private static final String ERR_NO_IMPL = "No implementation class set on "
    + "column tool builder";

  protected String name;

  /**
   * Implementation class name of a column tool.
   *
   * Must be the fully qualified class name.
   */
  protected String implementation;

  /**
   * XML defined properties for instances of this column tool.
   */
  private final Map<String, String> props;

  /**
   * Column data type that this column tool definition is intended to handle.
   */
  private AttributeFieldDataType type;

  AbstractColumnToolBuilder() {
    props = new HashMap<>();
  }

  @Override
  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(props);
  }

  @Override
  public AttributeFieldDataType getColumnType() {
    return type;
  }

  /**
   * Casts the object class to the expected type for the implementing tool
   * definition.
   *
   * @param input
   *   Object of unconfirmed type.
   *
   * @return Object cast to type {@link T}.
   *
   * @throws WdkModelException
   *   will be thrown if the input object cannot be assigned to type {@link T}.
   */
  protected abstract T cast(final Object input) throws WdkModelException;

  //
  // Model parse methods
  //

  @Override
  public ColumnToolBuilder<T> setImplementation(final String impl) {
    implementation = impl;
    return this;
  }

  public ColumnToolBuilder<T> addProperty(final WdkModelText prop) {
    props.put(prop.getName(), prop.getText());
    return this;
  }

  @Override
  public ColumnToolBuilder<T> setColumnType(final AttributeFieldDataType type) {
    this.type = type;
    return this;
  }

  @Override
  public ColumnToolBuilder<T> setKey(String key) {
    name = key;
    return this;
  }

  @Override
  public T build(WdkModel wdk) throws WdkModelException {
    if (isNull(type))
      throw new WdkModelException(ERR_NO_TYPE);
    if (isNull(implementation))
      throw new WdkModelException(ERR_NO_IMPL);

    try {
      final T tmp = cast(
        Class.forName(implementation)
          .getConstructor()
          .newInstance()
      );
      tmp.setModelProperties(props);
      tmp.setKey(name);
      return tmp;
    } catch (final Exception e) {
      throw new WdkModelException(format(ERR_INVALID_CLASS, implementation), e);
    }
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("properties", props)
      .put("implementation", implementation == null
        ? JSONObject.NULL
        : implementation)
      .put("columnType", type == null ? JSONObject.NULL : type)
      .put("name", name == null ? JSONObject.NULL : name)
      .toString();
  }
}
