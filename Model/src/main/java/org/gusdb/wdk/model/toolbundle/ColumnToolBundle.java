package org.gusdb.wdk.model.toolbundle;

import java.util.Map;
import java.util.Optional;

/**
 * A collection of tools that can be applied to an attribute field of a
 * {@link org.gusdb.wdk.model.record.RecordClass}.
 */
public interface ColumnToolBundle {
  /**
   * @return this tool bundle's unique name.
   */
  String getName();

  /**
   * Returns an optional tool retrieved by name.
   *
   * @param name
   *   Name of the tool to retrieve
   *
   * @return an option of {@link ColumnToolSet} which will be empty if no tool was
   * found with the given name.
   */
  Optional<ColumnToolSet> getTool(final String name);

  /**
   * Returns a read only map of the available tools, each mapped to its unique
   * name.
   *
   * @return read only map of available tools.
   */
  Map<String, ColumnToolSet> getTools();

}
