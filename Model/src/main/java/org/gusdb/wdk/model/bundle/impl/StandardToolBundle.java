package org.gusdb.wdk.model.bundle.impl;

import org.gusdb.wdk.model.bundle.ColumnToolBundle;
import org.gusdb.wdk.model.bundle.ColumnToolSet;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Standard implementation of an {@link ColumnToolBundle}.
 */
public class StandardToolBundle
implements ColumnToolBundle {

  /**
   * Unique name of this tool bundle
   */
  private final String name;

  /**
   * Map of tools by name
   */
  private final Map<String, ColumnToolSet> toolSets;

  public StandardToolBundle(
    final String name,
    final Map<String, ColumnToolSet> toolSets
  ) {
    this.name = name;
    this.toolSets = Collections.unmodifiableMap(toolSets);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<ColumnToolSet> getTool(String toolName) {
    return Optional.ofNullable(toolSets.get(toolName));
  }

  @Override
  public Map<String, ColumnToolSet> getTools() {
    return Collections.unmodifiableMap(toolSets);
  }
}
