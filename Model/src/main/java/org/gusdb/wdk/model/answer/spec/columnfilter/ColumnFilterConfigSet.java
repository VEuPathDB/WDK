package org.gusdb.wdk.model.answer.spec.columnfilter;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.wdk.model.answer.spec.columnfilter.ColumnConfig.ColumnConfigBuilder;
import org.json.JSONObject;

/**
 * Represents a map from column name -> column config (a set of filters and their configs)
 */
public class ColumnFilterConfigSet extends ReadOnlyHashMap<String, ColumnConfig> {

  public static class ColumnFilterConfigSetBuilder {

    // map from column name to builder for a map of tool name -> filter config
    private final Map<String, ColumnConfigBuilder> builders = new HashMap<>();

    public ColumnFilterConfigSetBuilder() {}

    public ColumnFilterConfigSetBuilder(ColumnFilterConfigSet old) {
      old.entrySet().stream().forEach(
          entry -> builders.put(entry.getKey(), new ColumnConfigBuilder(entry.getValue())));
    }

    public Map<String, ColumnConfigBuilder> getAll() {
      return builders;
    }

    public ColumnFilterConfigSetBuilder put(
      final String name,
      final ColumnConfigBuilder builder) {
      builders.put(name, builder);
      return this;
    }

    public ColumnFilterConfigSetBuilder setFilterConfig(
      final String column,
      final String filter,
      final JSONObject config
    ) {
      builders.computeIfAbsent(column, x -> new ColumnConfigBuilder())
        .addEntry(filter, config);
      return this;
    }

    public ColumnFilterConfigSet build() {
      final var out = new HashMap<String, ColumnConfig>(builders.size());
      builders.forEach((k, v) -> out.put(k, v.build()));
      return new ColumnFilterConfigSet(out);
    }

    /**
     * Remove a filter config for the passed column name and filter name
     * 
     * @param columnName
     * @param filterName
     * @return true if filter removed, else false
     */
    public boolean remove(String columnName, String filterName) {
      ColumnConfigBuilder columnFilters = builders.get(columnName);
      if (columnFilters == null) return false;
      JSONObject config = columnFilters.get(filterName);
      if (config == null) return false;
      columnFilters.remove(filterName);
      return true;
    }
  }

  public ColumnFilterConfigSet(Map<String, ColumnConfig> configs) {
    super(configs);
  }

}
