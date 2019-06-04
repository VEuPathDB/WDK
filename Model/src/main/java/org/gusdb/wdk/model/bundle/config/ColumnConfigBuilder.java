package org.gusdb.wdk.model.bundle.config;

import org.gusdb.wdk.model.bundle.ColumnToolConfig;

import java.util.Map;

public interface ColumnConfigBuilder {
  Map<String, FilterConfigSetBuilder> getAll();

  ColumnConfigBuilder put(String name, FilterConfigSetBuilder builder);

  ColumnConfigBuilder append(String filter, ColumnToolConfig config);

  ColumnConfig build();
}
