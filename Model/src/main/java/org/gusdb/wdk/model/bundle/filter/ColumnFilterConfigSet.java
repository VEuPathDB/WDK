package org.gusdb.wdk.model.bundle.filter;

import org.gusdb.wdk.model.bundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.bundle.config.ColumnConfig;
import org.gusdb.wdk.model.bundle.config.FilterConfigSet;
import org.gusdb.wdk.model.bundle.ColumnToolConfig;

import java.util.*;

/**
 * Base implementation of the {@link ColumnFilterConfigSet} interface.
 * <p>
 * Constructed via {@link org.gusdb.wdk.model.bundle.config.ColumnFilterConfigSetBuilder}.
 */
class ColumnFilterConfigSetImpl implements ColumnFilterConfigSet {
  private final Map<String, ColumnConfig> configs;

  ColumnFilterConfigSetImpl(final Map<String, ColumnConfig> configs) {
    this.configs = Map.copyOf(configs);
  }

  @Override
  public Map<String, ColumnConfig> getColumnConfigs() {
    return configs;
  }
}


/**
 * Base implementation of the {@link ColumnConfig} interface.
 * <p>
 * Constructed via {@link StandardColumnFilterConfigSetBuilder}.
 */
class ColumnConfigImpl implements ColumnConfig {
  private final Map<String, FilterConfigSet> filters;

  ColumnConfigImpl(Map<String, FilterConfigSet> filters) {
    this.filters = Map.copyOf(filters);
  }

  @Override
  public Map<String, FilterConfigSet> getFilterConfigSets() {
    return filters;
  }
}


/**
 * Base implementation of the {@link FilterConfigSet} interface.
 *
 * Constructed via {@link StandardColumnFilterConfigSetBuilder}.
 */
class FilterConfigSetImpl implements FilterConfigSet {
  private final Collection<ColumnToolConfig> configs;

  FilterConfigSetImpl(Collection<ColumnToolConfig> configs) {
    this.configs = List.copyOf(new ArrayList<>(configs));
  }

  @Override
  public Collection<ColumnToolConfig> getConfigs() {
    return configs;
  }
}
