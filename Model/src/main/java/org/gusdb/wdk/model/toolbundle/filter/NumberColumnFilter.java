package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.config.ComparableConfig;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import static java.lang.String.format;

public class NumberColumnFilter extends AbstractColumnFilter {
  // JSON keys
  private static final String
    KEY_COMP  = "comparator",
    KEY_VALUE = "value";

  private static final FilterComparator DEF_COMP = FilterComparator.EQUALS;

  private static final Logger LOG = LogManager.getLogger(NumberColumnFilter.class);

  private ComparableConfig<Number> config;

  @Override
  protected SqlBuilder newSqlBuilder(String col) {
    LOG.debug("newSqlBuilder() <- " + col);
    LOG.debug("newSqlBuilder(): creating new SQL builder with config: " + config);
    return () -> format(config.getComparator().sql, col, config.getValue()
      .toString());
  }

  @Override
  public ColumnFilter copy() {
    final var out = copyInto(new NumberColumnFilter());
    if (config != null)
      out.config = config.copy();
    return out;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject()
      && js.has(KEY_VALUE);
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.NUMBER;
  }

  @Override
  public SchemaBuilder inputSpec() {
    var js = Schema.draft4();
    return js.asObject()
      .additionalProperties(false)
      .requiredProperty(KEY_VALUE, js.asNumber())
      .optionalProperty(KEY_COMP, js.asString().defaultValue(DEF_COMP.key));
  }

  @Override
  protected Object genConfig() {
    return config = new ComparableConfig<Number>()
      .setComparator(DEF_COMP);
  }
}

