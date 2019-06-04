package org.gusdb.wdk.model.bundle.config;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;

public interface ColumnConfig {
  Map<String, FilterConfigSet> getFilterConfigSets();

  default void forEach(BiConsumer<String, FilterConfigSet> fn) {
    getFilterConfigSets().forEach(fn);
  }

  default FilterConfigSet getFilterConfigs(String name) {
    return getFilterConfigSets().get(name);
  }

  default boolean hasFilters(String name) {
    return isNull(getFilterConfigs(name));
  }

  default Optional<FilterConfigSet> filterConfigs(String name) {
    return Optional.ofNullable(getFilterConfigs(name));
  }

}
