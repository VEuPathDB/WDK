package org.gusdb.wdk.model.bundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.bundle.filter.config.RangeConfig;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

public class NumberRangeColumnFilter extends AbstractColumnFilter {

  // JSON Keys
  private static final String
    KEY_COMPL = "complement",
    KEY_MAX   = "max",
    KEY_MIN   = "min",
    KEY_LEFT  = "leftClosed",
    KEY_RIGHT = "rightClosed";

  // Flag defaults
  private static final boolean
    DEF_L_CLOSED = true,
    DEF_R_CLOSED = true,
    DEF_COMPL = false;

  // SQL chunks
  private static final String
    SQL_GEQ   = ">=",
    SQL_LEQ   = "<=",
    SQL_GT    = ">",
    SQL_LT    = "<",
    SQL_START = "(%1$s ",
    SQL_AND   = " %2$s AND %1$s ",
    SQL_OR    = " %2$s OR %1$s ",
    SQL_END   = " %3$s)";

  private RangeConfig<Number> config;

  @Override
  protected SqlBuilder newSqlBuilder(String col) {
    return () -> String.format(sql(), col, config.getLeft(), config.getRight());
  }

  @Override
  public ColumnFilter copy() {
    final var out = copyInto(new NumberRangeColumnFilter());
    if (config != null)
      out.config = config.copy();
    return out;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject()
      && js.has(KEY_MIN)
      && js.has(KEY_MAX);
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
      .requiredProperty(KEY_MIN, js.asNumber())
      .requiredProperty(KEY_MAX, js.asNumber())
      .optionalProperty(KEY_LEFT, js.asBoolean().defaultValue(DEF_L_CLOSED))
      .optionalProperty(KEY_RIGHT, js.asBoolean().defaultValue(DEF_R_CLOSED))
      .optionalProperty(KEY_COMPL, js.asBoolean().defaultValue(DEF_COMPL));
  }

  private String sql() {
    return SQL_START + (
      config.isComplement()
        ? (config.isLeftClosed() ? SQL_LT : SQL_LEQ)
          + SQL_OR
          + (config.isRightClosed() ? SQL_GT : SQL_GEQ)
        : (config.isLeftClosed() ? SQL_GEQ : SQL_GT)
          + SQL_AND
          + (config.isRightClosed() ? SQL_LEQ : SQL_LT)
    ) + SQL_END;
  }

  @Override
  protected RangeConfig<Number> genConfig() {
    return config = new RangeConfig<Number>()
      .setLeftClosed(DEF_L_CLOSED)
      .setRightClosed(DEF_R_CLOSED)
      .setComplement(DEF_COMPL);
  }
}
