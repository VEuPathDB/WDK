package org.gusdb.wdk.model.answer.spec;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.wdk.model.answer.spec.ColumnFilterConfig.ColumnFilterConfigBuilder;
import org.json.JSONObject;

/**
 * Represents a map from column name -> column config (a set of filters and their configs)
 */
public class ColumnFilterConfigSet extends ReadOnlyHashMap<String, ColumnFilterConfig> {

  public static class ColumnFilterConfigSetBuilder {

    // map from column name to builder for a map of tool name -> filter config
    private final Map<String, ColumnFilterConfigBuilder> builders = new HashMap<>();

    public ColumnFilterConfigSetBuilder() {}

    public ColumnFilterConfigSetBuilder(ColumnFilterConfigSet old) {
      old.entrySet().stream().forEach(
          entry -> builders.put(entry.getKey(), new ColumnFilterConfigBuilder(entry.getValue())));
    }

    public Map<String, ColumnFilterConfigBuilder> getAll() {
      return builders;
    }

    public ColumnFilterConfigSetBuilder put(
      final String name,
      final ColumnFilterConfigBuilder builder) {
      builders.put(name, builder);
      return this;
    }

    public ColumnFilterConfigSetBuilder setFilterConfig(
      final String column,
      final String filter,
      final JSONObject config
    ) {
      builders.computeIfAbsent(column, x -> new ColumnFilterConfigBuilder())
        .addEntry(filter, config);
      return this;
    }

    public ColumnFilterConfigSet build() {
      final var out = new HashMap<String, ColumnFilterConfig>(builders.size());
      builders.forEach((k, v) -> out.put(k, v.build()));
      return new ColumnFilterConfigSet(out);
    }

    /**
     * Remove a filter config for the passed column name and filter name
     * 
     * @param columnName
     * @param filterName
     *
     * @return true if filter removed, else false
     */
    public boolean remove(String columnName, String filterName) {
      ColumnFilterConfigBuilder columnFilters = builders.get(columnName);
      if (columnFilters == null) return false;
      JSONObject config = columnFilters.get(filterName);
      if (config == null) return false;
      columnFilters.remove(filterName);
      if (columnFilters.isEmpty()) {
        // don't let empty builder remain
        builders.remove(columnName);
      }
      return true;
    }
  }

  public ColumnFilterConfigSet(Map<String, ColumnFilterConfig> configs) {
    super(configs);
  }

}
