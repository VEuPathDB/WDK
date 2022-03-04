package org.gusdb.wdk.model.columntool;

import java.util.Map;

import org.gusdb.wdk.model.record.attribute.AttributeField;

import io.vulpine.lib.json.schema.SchemaBuilder;

/**
 * Base interface for Column Tool implementations (i.e. a reporter/filter combination)
 *
 * @author rdoherty
 */
public interface ColumnToolElement<T extends ColumnToolElement<T>> {

  /**
   * Assigns the attribute field on which this tool will operate
   *
   * @param field field for this instance
   * @return this (builder pattern)
   */
  T setAttributeField(AttributeField field);

  /**
   * Sets properties on this instance fetched from the model XML
   *
   * @param properties properties set in the XML for this element
   * @return this (builder pattern)
   */
  T setModelProperties(Map<String,String> properties);

  /**
   * Returns the input (user configuration) schema for this element
   * (i.e. the structure of the JSON)
   *
   * @return schema for configuration of this element
   */
  SchemaBuilder getInputSchema();

}