package org.gusdb.wdk.model.toolbundle.filter;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfigBuilder;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSetBuilder;

public class StandardColumnFilterConfigSetBuilder
implements ColumnFilterConfigSetBuilder {

  // map from column name to builder for a map of tool name -> filter config
  private final Map<String, ColumnConfigBuilder> builders = new HashMap<>();

  public StandardColumnFilterConfigSetBuilder() {}

  public StandardColumnFilterConfigSetBuilder(ColumnFilterConfigSet old) {
    old.getColumnConfigs()
      .forEach((k, v) -> builders.put(k, new ColumnConfigBuilderImpl(v)));
  }

  @Override
  public Map<String, ColumnConfigBuilder> getAll() {
    return builders;
  }

  @Override
  public ColumnFilterConfigSetBuilder put(
    final String name,
    final ColumnConfigBuilder builder) {
    builders.put(name, builder);
    return this;
  }

  @Override
  public ColumnFilterConfigSetBuilder setFilterConfig(
    final String column,
    final String filter,
    final ColumnToolConfig config
  ) {
    builders.computeIfAbsent(column, x -> new ColumnConfigBuilderImpl())
      .addEntry(filter, config);
    return this;
  }

  @Override
  public ColumnFilterConfigSet build() {
    final var out = new HashMap<String, ColumnConfig>(builders.size());
    builders.forEach((k, v) -> out.put(k, v.build()));
    return new ColumnFilterConfigSetImpl(out);
  }

  /**
   * Remove a filter config for the passed column name and filter name
   * 
   * @param columnName
   * @param filterName
   * @return true if filter removed, else false
   */
  public boolean remove(String columnName, String filterName) {
    ColumnConfigBuilder columnFilters = builders.get(columnName);
    if (columnFilters == null) return false;
    ColumnToolConfig config = columnFilters.get(filterName);
    if (config == null) return false;
    columnFilters.remove(filterName);
    return true;
  }
}


class ColumnConfigBuilderImpl extends HashMap<String, ColumnToolConfig> implements ColumnConfigBuilder {

  public ColumnConfigBuilderImpl() {}

  public ColumnConfigBuilderImpl(ColumnConfig orig) {
    orig.forEach((toolName, config) -> put(toolName, config.deepCopy()));
  }

  @Override
  public ColumnConfigBuilder addEntry(
    final String name,
    final ColumnToolConfig builder
  ) {
    put(name, builder);
    return this;
  }

  @Override
  public ColumnConfig build() {
    return new ColumnConfigImpl(this);
  }
}

