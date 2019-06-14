package org.gusdb.wdk.model.toolbundle.config;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

import java.util.Map;

public interface ColumnFilterConfigSetBuilder {
  Map<String, ColumnConfigBuilder> getAll();

  ColumnFilterConfigSetBuilder put(String name, ColumnConfigBuilder builder);

  ColumnFilterConfigSetBuilder append(String column, String filter,
    ColumnToolConfig config);

  ColumnFilterConfigSet build();
}
