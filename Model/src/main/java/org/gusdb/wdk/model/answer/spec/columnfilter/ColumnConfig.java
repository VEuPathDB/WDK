package org.gusdb.wdk.model.answer.spec.columnfilter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.json.JsonUtil;
import org.json.JSONObject;

/**
 * Represents a map from filter name -> filter config for a single column
 */
public class ColumnConfig extends ReadOnlyHashMap<String, JSONObject> {

  public static class ColumnConfigBuilder extends HashMap<String, JSONObject> {

    public ColumnConfigBuilder() {}

    public ColumnConfigBuilder(ColumnConfig orig) {
      orig.forEach((toolName, config) -> put(toolName, JsonUtil.clone(config)));
    }

    public ColumnConfigBuilder addEntry(
      final String name,
      final JSONObject builder
    ) {
      put(name, builder);
      return this;
    }

    public ColumnConfig build() {
      return new ColumnConfig(this);
    }
  }

  public ColumnConfig(Map<String, JSONObject> filters) {
    super(filters);
  }

  public void forEach(BiConsumer<String, JSONObject> fn) {
    entrySet().stream().forEach(entry -> fn.accept(entry.getKey(), entry.getValue()));
  }

}
