package org.gusdb.wdk.model.toolbundle.config;

import java.util.Map;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

public interface ColumnConfigBuilder {
  Map<String, FilterConfigSetBuilder> getAll();

  ColumnConfigBuilder put(String name, FilterConfigSetBuilder builder);

  ColumnConfigBuilder append(String filter, ColumnToolConfig config);

  ColumnConfig build();

  ColumnToolConfig get(String name);

  void removeAll(String name);
}
