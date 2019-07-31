package org.gusdb.wdk.model.toolbundle.config;

import java.util.function.BiConsumer;

import org.gusdb.fgputil.collection.ReadOnlyMap;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

public interface ColumnConfig extends ReadOnlyMap<String, ColumnToolConfig> {

  default void forEach(BiConsumer<String, ColumnToolConfig> fn) {
    entrySet().stream().forEach(entry -> fn.accept(entry.getKey(), entry.getValue()));
  }

}
