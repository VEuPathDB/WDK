package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import static java.lang.String.format;

/**
 * Column filter that can do exact or partial matches against a single string
 * value.
 */
public class StringPatternColumnFilter extends AbstractColumnFilter {
  private static final String KEY_FILTER = "filter";

  // SQL Statements
  private static final String SQL_LIKE  = "%s LIKE '%s'";
  private static final String SQL_EQUAL = "%s = '%s'";

  private Config config;

  @Override
  public ColumnFilter copy() {
    final var copy = new StringPatternColumnFilter();
    copy.config = config;
    return copyInto(copy);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.STRING;
  }

  @Override
  public SchemaBuilder inputSpec() {
    return Schema.draft4()
      .asObject()
      .requiredProperty(KEY_FILTER)
      .asString()
      .close();
  }

  @Override
  protected Object genConfig() {
    return config = new Config();
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject()
      && js.has(KEY_FILTER)
      && js.get(KEY_FILTER).isTextual();
  }

  @Override
  protected SqlBuilder newSqlBuilder(final String column) {
    final var val = escape(config.filter);
    return () -> format(hasWildcard(val) ? SQL_LIKE : SQL_EQUAL, column, val);
  }

  private static String escape(final String in) {
    return in.replace('*', '%').replace("'", "''");
  }

  private static boolean hasWildcard(final String in) {
    return in.indexOf('%') > -1;
  }

  private static class Config {
    public String filter;
  }
}
