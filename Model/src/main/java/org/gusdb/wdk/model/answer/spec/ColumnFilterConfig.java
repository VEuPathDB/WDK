package org.gusdb.wdk.model.answer.spec;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.json.JsonUtil;
import org.json.JSONObject;

/**
 * Represents a map from filter name -> filter config for a single column
 */
public class ColumnFilterConfig extends ReadOnlyHashMap<String, JSONObject> {

  public static class ColumnFilterConfigBuilder extends HashMap<String, JSONObject> {

    public ColumnFilterConfigBuilder() {}

    public ColumnFilterConfigBuilder(ColumnFilterConfig orig) {
      orig.forEach((toolName, config) -> put(toolName, JsonUtil.clone(config)));
    }

    public ColumnFilterConfigBuilder addEntry(
      final String name,
      final JSONObject builder
    ) {
      Objects.requireNonNull(builder);
      put(name, builder);
      return this;
    }

    public ColumnFilterConfig build() {
      return new ColumnFilterConfig(this);
    }
  }

  public ColumnFilterConfig(Map<String, JSONObject> filters) {
    super(filters);
  }

  public void forEach(BiConsumer<String, JSONObject> fn) {
    entrySet().stream().forEach(entry -> fn.accept(entry.getKey(), entry.getValue()));
  }

  @Override
  public String toString() {
    JSONObject configs = new JSONObject();
    for (Entry<String, JSONObject> config : entrySet()) {
      configs.put(config.getKey(), config.getValue());
    }
    return configs.toString();
  }
}
