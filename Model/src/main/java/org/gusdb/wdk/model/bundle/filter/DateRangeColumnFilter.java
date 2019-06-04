package org.gusdb.wdk.model.bundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.bundle.ColumnFilter;
import org.gusdb.wdk.model.bundle.filter.config.RangeConfig;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.gusdb.wdk.model.bundle.filter.DateColumnFilter.DATE_JAVA;
import static org.gusdb.wdk.model.bundle.filter.DateColumnFilter.DATE_SQL;

public class DateRangeColumnFilter extends AbstractColumnFilter {
  // JSON Keys
  public static final String
    JS_MIN      = "min",
    JS_MAX      = "max",
    JS_L_CLOSED = "leftClosed",
    JS_R_CLOSED = "rightClosed",
    JS_COMPL    = "complement";

  // Defaults
  public static final boolean
    DEF_L_CLOSED = true,
    DEF_R_CLOSED = true,
    DEF_COMPL    = false;

  // SQL chunks
  private static final String
    SQL_GEQ   = ">=",
    SQL_LEQ   = "<=",
    SQL_GT    = ">",
    SQL_LT    = "<",
    SQL_START = "(%1$s ",
    SQL_AND   = " to_date('%2$s', '" + DATE_SQL + "') AND %1$s ",
    SQL_OR    = " to_date('%2$s', '" + DATE_SQL + "') OR %1$s ",
    SQL_END   = " to_date('%3$s', '" + DATE_SQL + "'))";

  private RangeConfig<LocalDateTime> config;
  private final DateTimeFormatter formatter;

  public DateRangeColumnFilter() {
    formatter = DateTimeFormatter.ofPattern(DATE_JAVA);
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.has(JS_MIN) && js.has(JS_MAX);
  }

  @Override
  public ColumnFilter copy() {
    var out = copyInto(new DateRangeColumnFilter());

    if (config != null)
      out.config = config.copy();

    return out;
  }


  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.DATE;
  }

  @Override
  public SchemaBuilder inputSpec() {
    var js = Schema.draft4();
    return js.asObject()
      .requiredProperty(JS_MIN, js.asString()
        .pattern("^(\\d{4}-\\d\\d-\\d\\d(?:T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d{4})?)?(?:[Zz]|[+-]\\d\\d:\\d\\d)?)$"))
      .requiredProperty(JS_MAX, js.asString()
        .pattern("^(\\d{4}-\\d\\d-\\d\\d(?:T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d{4})?)?(?:[Zz]|[+-]\\d\\d:\\d\\d)?)$"))
      .optionalProperty(JS_L_CLOSED, js.asBoolean().defaultValue(DEF_L_CLOSED))
      .optionalProperty(JS_R_CLOSED, js.asBoolean().defaultValue(DEF_R_CLOSED))
      .optionalProperty(JS_COMPL, js.asBoolean().defaultValue(DEF_COMPL));
  }

  @Override
  protected RangeConfig<LocalDateTime> genConfig() {
    return config = new Config()
      .setLeftClosed(DEF_L_CLOSED)
      .setRightClosed(DEF_R_CLOSED)
      .setComplement(DEF_COMPL);
  }

  @Override
  protected SqlBuilder newSqlBuilder(String col) {
    return () -> String.format(
      sql(),
      col,
      config.getLeft().format(formatter),
      config.getRight().format(formatter));
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

  private static final class Config extends RangeConfig<LocalDateTime> {}
}
