package org.gusdb.wdk.model.bundle;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.json.SchemaUtil;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.util.Map;

/**
 * Represents a tool or utility that operates on a single AttributeField value
 * at a time.
 */
public interface ColumnTool {

  default ColumnToolConfig validateConfig(final JsonNode obj)
  throws WdkUserException {
    var res = SchemaUtil.validate(inputSpec().build(), obj);
    if (res.size() > 0)
      throw new WdkUserException(res.toString());
    return () -> obj;
  }

  ColumnTool setKey(String key);

  String getKey();

  /**
   * Set the answer value that the tool will operate on.
   *
   * @param val
   *   AnswerValue
   *
   * @return the current ColumnTool instance.
   */
  ColumnTool setAnswerValue(AnswerValue val);

  /**
   * Sets the attribute field that this tool will apply to
   *
   * @param field
   *   the attribute field that this tool will apply to
   *
   * @return the current ColumnTool instance.
   */
  ColumnTool setAttributeField(AttributeField field);

  /**
   * Sets the XML defined properties from the WDK model.
   *
   * @param props
   *   properties for this tool defined in the WDK model XML.
   *
   * @return the current ColumnTool instance.
   */
  ColumnTool setModelProperties(Map<String, String> props);

  /**
   * Return a serializable specification of what this tool expects as runtime
   * configuration input.
   *
   * @return input specification.
   */
  SchemaBuilder inputSpec();

  /**
   * Creates a copy of the current ColumnTool with it's configuration.
   *
   * @return a copy of the current ColumnTool instance.
   */
  ColumnTool copy();

  ColumnTool setConfiguration(ColumnToolConfig config);

  /**
   * Returns whether or not the implementing ColumnTool is capable of handling
   * data of the given type.
   *
   * @param type
   *   data type to check
   *
   * @return whether or not this ColumnTool can handle data of that type.
   */
  boolean isCompatibleWith(AttributeFieldDataType type);

  /**
   * Provides a quick check as to whether or not the input could be valid
   * without performing a full schema validation.
   * <p>
   * Implementations should do the minimum work to decide whether or not a given
   * JSON config may be compatible, such as a type check on one or more
   * properties.
   *
   * @param js
   *   JSON user configuration to check
   *
   * @return whether or not the given JSON config may be compatible with the
   * current ColumnTool implementation.
   */
  boolean isCompatibleWith(JsonNode js);
}
