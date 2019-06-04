package org.gusdb.wdk.model.bundle.config;

import org.gusdb.wdk.model.bundle.ColumnToolConfig;

import java.util.Map;

public interface ColumnFilterConfigSetBuilder {
  Map<String, ColumnConfigBuilder> getAll();

  ColumnFilterConfigSetBuilder put(String name, ColumnConfigBuilder builder);

  ColumnFilterConfigSetBuilder append(String column, String filter,
    ColumnToolConfig config);

  ColumnFilterConfigSet build();
}
