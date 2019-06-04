package org.gusdb.wdk.model.bundle.config;

import org.gusdb.wdk.model.bundle.ColumnToolConfig;

import java.util.Collection;

/**
 * FilterConfigSetBuilder
 */
public interface FilterConfigSetBuilder {

  /**
   * @return mutable access to the internal backing collection for this builder
   */
  Collection<ColumnToolConfig> getAll();

  /**
   * Adds a new filter config to this builder.
   *
   * @param conf
   *   filter config to add to this builder
   *
   * @return the modified instance of FilterConfigSet.Builder (not guaranteed to
   * be the same instance)
   */
  FilterConfigSetBuilder add(ColumnToolConfig conf);

  /**
   * Constructs a new instance implementation of FilterConfigSet.
   *
   * @return the constructed FilterConfigSet instance.
   */
  FilterConfigSet build();
}
