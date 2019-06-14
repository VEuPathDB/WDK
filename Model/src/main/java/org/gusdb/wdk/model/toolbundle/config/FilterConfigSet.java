package org.gusdb.wdk.model.toolbundle.config;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Collection
 */
public interface FilterConfigSet {

  /**
   * @return read only access to the internal collection of FilterConfig
   * instances
   */
  Collection<ColumnToolConfig> getConfigs();

  /**
   * Applies the given function to each FilterConfig present in this
   * FilterConfigSet
   *
   * @param fn
   *   function to apply to each FilterConfig
   */
  default void forEach(Consumer<ColumnToolConfig> fn) {
    getConfigs().forEach(fn);
  }
}
