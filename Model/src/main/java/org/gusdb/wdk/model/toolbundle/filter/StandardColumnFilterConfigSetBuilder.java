package org.gusdb.wdk.model.toolbundle.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfig;
import org.gusdb.wdk.model.toolbundle.config.ColumnConfigBuilder;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.toolbundle.config.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.toolbundle.config.FilterConfigSet;
import org.gusdb.wdk.model.toolbundle.config.FilterConfigSetBuilder;

public class StandardColumnFilterConfigSetBuilder
implements ColumnFilterConfigSetBuilder {

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
  public ColumnFilterConfigSetBuilder append(
    final String column,
    final String filter,
    final ColumnToolConfig config
  ) {
    builders.computeIfAbsent(column, x -> new ColumnConfigBuilderImpl())
      .append(filter, config);
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
    columnFilters.removeAll(filterName);
    return true;
  }
}


class ColumnConfigBuilderImpl
implements ColumnConfigBuilder {

  private Map<String, FilterConfigSetBuilder> builders = new HashMap<>();

  public ColumnConfigBuilderImpl() {}

  public ColumnConfigBuilderImpl(ColumnConfig old) {
    old.getFilterConfigSets()
      .forEach((a, b) -> builders.put(a, new FilterConfigSetBuilderImpl(b)));
  }

  @Override
  public Map<String, FilterConfigSetBuilder> getAll() {
    return builders;
  }

  @Override
  public ColumnConfigBuilder put(
    final String name,
    final FilterConfigSetBuilder builder
  ) {
    builders.put(name, builder);
    return this;
  }

  @Override
  public ColumnConfigBuilder append(
    final String name,
    final ColumnToolConfig config
  ) {
    builders.computeIfAbsent(name, x -> new FilterConfigSetBuilderImpl())
      .add(config);
    return this;
  }

  @Override
  public ColumnConfig build() {
    final var out = new HashMap<String, FilterConfigSet>();
    builders.forEach((k, v) -> out.put(k, v.build()));
    return new ColumnConfigImpl(out);
  }

  @Override
  public ColumnToolConfig get(String name) {
    return builders.get(name).getFirst();
  }

  @Override
  public void removeAll(String name) {
    builders.remove(name);
  }
}


class FilterConfigSetBuilderImpl
implements FilterConfigSetBuilder {
  private final Collection<ColumnToolConfig> configs = new ArrayList<>();

  public FilterConfigSetBuilderImpl() {}

  public FilterConfigSetBuilderImpl(FilterConfigSet old) {
    configs.addAll(old.getConfigs());
  }

  @Override
  public Collection<ColumnToolConfig> getAll() {
    return configs;
  }

  @Override
  public FilterConfigSetBuilder add(
    ColumnToolConfig conf) {
    configs.add(conf);
    return this;
  }

  @Override
  public FilterConfigSet build() {
    return new FilterConfigSetImpl(configs);
  }

  @Override
  public ColumnToolConfig getFirst() {
    if (configs.isEmpty()) return null;
    return configs.iterator().next();
  }
}
