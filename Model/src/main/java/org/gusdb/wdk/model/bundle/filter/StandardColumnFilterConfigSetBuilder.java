package org.gusdb.wdk.model.bundle.filter;

import org.gusdb.wdk.model.bundle.ColumnToolConfig;
import org.gusdb.wdk.model.bundle.config.ColumnConfigBuilder;
import org.gusdb.wdk.model.bundle.config.ColumnFilterConfigSet;
import org.gusdb.wdk.model.bundle.config.ColumnConfig;
import org.gusdb.wdk.model.bundle.config.ColumnFilterConfigSetBuilder;
import org.gusdb.wdk.model.bundle.config.FilterConfigSet;
import org.gusdb.wdk.model.bundle.config.FilterConfigSetBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

  public ColumnFilterConfigSet build() {
    final var out = new HashMap<String, ColumnConfig>(builders.size());
    builders.forEach((k, v) -> out.put(k, v.build()));
    return new ColumnFilterConfigSetImpl(out);
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
}
