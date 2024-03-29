package org.gusdb.wdk.model.columntool;

import org.gusdb.wdk.model.report.PropertiesProvider;
import org.gusdb.wdk.model.report.Reporter;

import io.vulpine.lib.json.schema.SchemaBuilder;

/**
 * Interface with which column reporter implementations must comply
 *
 * @author rdoherty
 */
public interface ColumnReporter extends ColumnToolElement<ColumnReporter>, Reporter {

  /**
   * Returns the output schema for this element
   * (i.e. the structure of the JSON)
   *
   * @return schema for configuration of this element
   */
  SchemaBuilder getOutputSchema();

  /**
   * Another way to set properties on this reporter (added to
   * comply with the Reporter interface).  By default, immediately
   * gets the properties from the provider and sets them using
   * <code>setProperties(Map&lt;String,String>)</code>.
   */
  @Override
  default Reporter setProperties(PropertiesProvider props) {
    setModelProperties(props.getProperties());
    return this;
  }

}