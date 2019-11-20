package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import io.vulpine.lib.json.schema.SchemaBuilder;

/**
 * A reporter that operates on a set of data from a single result column.
 *
 * @param <T>
 *   the type of data this column reporter operates on.
 */
public interface ColumnReporter extends ColumnTool<ColumnReporterInstance> {

  /**
   * Returns a JSON Schema specification for the expected output format of this
   * reporter.
   *
   * @return JSON Schema for this reporters output.
   */
  SchemaBuilder outputSpec(AttributeFieldDataType type);

}
