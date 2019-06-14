package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.util.Collection;
import java.util.stream.Collectors;

public class StringSetColumnFilter extends AbstractColumnFilter {
  private static final String KEY_FILTERS = "filters";
  private static final int
    FILTERS_MIN = 1,
    FILTERS_MAX = 101; // TODO: This should be configurable or something?
  private static final String SQL = "%s IN (%s)";

  private Config config;

  @Override
  protected SqlBuilder newSqlBuilder(String col) {
    return () -> String.format(SQL, col, format(config.filters));
  }

  @Override
  public ColumnFilter copy() {
    final var out = new StringSetColumnFilter();
    out.config = config;
    return copyInto(out);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.STRING;
  }

  @Override
  protected Object genConfig() {
    return config = new Config();
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject()
      && js.has(KEY_FILTERS)
      && js.get(KEY_FILTERS).isArray();
  }

  /**
   * Returns a json schema object
   *
   * @return input specification object
   */
  @Override
  public SchemaBuilder inputSpec() {
    return Schema.draft4()
      .asObject()
      .requiredProperty(KEY_FILTERS)
        .asArray()
          .maxItems(FILTERS_MAX)
          .minItems(FILTERS_MIN)
          .items()
            .asString()
            .close()
          .close();
  }

  private static String format(final Collection<String> in) {
    return in.stream()
      .map(StringSetColumnFilter::escape)
      .collect(Collectors.joining("','", "'", "'"));
  }

  private static String escape(final String in) {
    return in.replace("'", "''");
  }

  private static class Config {
    public Collection<String> filters;
  }
}
