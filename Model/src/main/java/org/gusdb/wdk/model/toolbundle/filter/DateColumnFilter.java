package org.gusdb.wdk.model.toolbundle.filter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.filter.config.ComparableConfig;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateColumnFilter extends AbstractColumnFilter {
  // JSON Keys
  private static final String
    KEY_VALUE = "value",
    KEY_COMP  = "comparator";

  // Date formats
  static final String
    DATE_SQL  = "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"",
    DATE_JAVA = "yyyy-MM-dd'T'HH:mm:ss'Z'";


  private static final FilterComparator DEF_COMP = FilterComparator.EQUALS;

  private final DateTimeFormatter formatter;

  private ComparableConfig<LocalDateTime> config;

  public DateColumnFilter() {
    formatter = DateTimeFormatter.ofPattern(DATE_JAVA);
  }

  @Override
  protected SqlBuilder newSqlBuilder(String col) {
    return () -> {
      var sql = config.getComparator().sql;
      var raw = config.getValue();
      return String.format(sql, col, valWrap(raw));
    };
  }

  @Override
  public ColumnFilter copy() {
    var a = copyInto(new DateColumnFilter());
    if (config != null)
      a.config = config.copy();
    return a;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type ==  AttributeFieldDataType.DATE;
  }

  @Override
  public SchemaBuilder inputSpec() {
    var js = Schema.draft4();
    return js.asObject()
      .additionalProperties(false)
      .requiredProperty(KEY_VALUE, js.asString()
        .pattern("^(\\d{4}-\\d\\d-\\d\\d(?:T\\d\\d:\\d\\d:\\d\\d(?:\\.\\d{4})?)?(?:[Zz]|[+-]\\d\\d:\\d\\d)?)$"))
      .optionalProperty(KEY_COMP, js.asString()
        .enumValues(FilterComparator.simpleValue()));
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject()
      && js.has(KEY_VALUE)
      && js.get(KEY_VALUE).isTextual();
  }

  @Override
  protected ComparableConfig<LocalDateTime> genConfig() {
    return config = new Config()
      .setComparator(DEF_COMP);
  }

  private String valWrap(LocalDateTime val) {
    return "to_date('" + val.format(formatter) + "', '" + DATE_SQL + "')";
  }

  private static class Config extends ComparableConfig<LocalDateTime> {}
}
