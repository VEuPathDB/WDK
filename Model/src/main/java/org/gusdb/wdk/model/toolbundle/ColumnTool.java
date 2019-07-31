package org.gusdb.wdk.model.toolbundle;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

import java.io.IOException;
import java.util.Map;

import org.gusdb.fgputil.json.SchemaUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.json.JSONException;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.SchemaBuilder;

/**
 * Represents a tool or utility that operates on a single AttributeField value
 * at a time.
 */
public interface ColumnTool<T extends ColumnToolInstance> {

  /**
   * Utility function that takes a bare config object and populates its setters
   * using the JsonNode config passed in.
   * 
   * @param <T> type of Java configuration object
   * @param bareObject empty object with setters for configuration properties
   * @param config JSON configuration with properties matching T's setters
   * @return object passed in (not a copy) after population of properties
   */
  public static <T> T populateConfig(T bareObject, JsonNode config) {
    try {
      Jackson.readerForUpdating(bareObject).readValue(config);
      return bareObject;
    }
    catch (IOException e) {
      throw new JSONException(e);
    }
  }

  /**
   * Validates the configuration object against the given column.  The basic
   * validation only checks the JSON against the declared schema.  Subclasses
   * may wish to perform additional validation.
   * 
   * @param column
   * @param obj
   * @return
   * @throws WdkUserException
   */
  default ColumnToolConfig validateConfig(AttributeFieldDataType type, JsonNode obj)
  throws WdkUserException {
    var res = SchemaUtil.validate(getInputSpec(type).build(), obj);
    if (res.size() > 0)
      throw new WdkUserException(res.toString());
    return () -> obj;
  }

  ColumnTool<T> setKey(String key);

  String getKey();

  /**
   * Sets the XML defined properties from the WDK model.
   *
   * @param props
   *   properties for this tool defined in the WDK model XML.
   *
   * @return the current ColumnTool instance.
   */
  ColumnTool<T> setModelProperties(Map<String, String> props);

  /**
   * Return a serializable specification of what this tool expects as runtime
   * configuration input.
   *
   * @return input specification.
   */
  SchemaBuilder getInputSpec(AttributeFieldDataType type);

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
   * @param
   *   type of column for which this JSON should be checked
   * @param js
   *   JSON user configuration to check
   *
   * @return whether or not the given JSON config may be compatible with the
   * current ColumnTool implementation.
   */
  boolean isCompatibleWith(AttributeFieldDataType type, JsonNode js);

  /**
   * Creates an configured instance of this tool
   * 
   * @param answerValue answer value from which instance should be made
   * @param field column to which this tool has been assigned
   * @param config configuration of the instance
   * @return configured instance of this tool
   * @throws WdkModelException if unable to create an instance with this config
   */
  public T makeInstance(AnswerValue answerValue, AttributeField field, ColumnToolConfig config) throws WdkModelException;

}
