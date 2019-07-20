package org.gusdb.wdk.model.toolbundle.config;

import java.util.Map;

import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

public interface ColumnConfigBuilder extends Map<String, ColumnToolConfig> {

  ColumnConfigBuilder addEntry(String name, ColumnToolConfig builder);

  ColumnConfig build();

}
