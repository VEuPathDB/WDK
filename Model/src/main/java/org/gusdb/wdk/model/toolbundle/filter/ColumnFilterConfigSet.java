package org.gusdb.wdk.model.toolbundle.filter;

import java.util.Map;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;

/**
 * Base implementation of the {@link ColumnFilterConfigSet} interface.
 * <p>
 * Constructed via {@link org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSetBuilder}.
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
class ColumnConfigImpl extends ReadOnlyHashMap<String, ColumnToolConfig> implements ColumnConfig {
  ColumnConfigImpl(Map<String, ColumnToolConfig> filters) {
    super(filters);
  }
}
