package org.gusdb.wdk.model.toolbundle.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;

/**
 * An accessor fo retrieving {@link ColumnConfig} implementations for a given
 * column name.
 */
public interface ColumnFilterConfigSet {

  /**
   * Returns a read-only map representation of the internal data.
   *
   * @return map of column name to {@link ColumnConfig} instance.
   */
  Map<String, ColumnConfig> getColumnConfigs();

  /**
   * @return whether or not this config set is empty.
   */
  default boolean isEmpty() {
    return getColumnConfigs().isEmpty();
  }

  /**
   * Retrieves a {@code ColumnConfig} for the given column name
   *
   * @param name
   *   name of the column for which to retrieve a {@code ColumnConfig}
   *
   * @return the {@code ColumnConfig} for the given column name, or null if no
   * such config was present.
   */
  default ColumnConfig getColumnConfig(String name) {
    return getColumnConfigs().get(name);
  }

  /**
   * Applies the given function to each {@code name: ColumnConfig} pair
   *
   * @param fn
   *   function to apply to each {@code name: ColumnConfig} pair
   */
  default void forEach(BiConsumer<String, ColumnConfig> fn) {
    getColumnConfigs().forEach(fn);
  }

  /**
   * Returns whether or not this {@link ColumnFilterConfigSet} contains an entry
   * for the given column name.
   *
   * @param name
   *   name of the column to check for
   *
   * @return whether or not this config set has an entry for the given column.
   */
  default boolean hasColumn(String name) {
    return isNull(getColumnConfig(name));
  }

  default Optional<ColumnConfig> columnConfig(String name) {
    return Optional.ofNullable(getColumnConfig(name));
  }
}
