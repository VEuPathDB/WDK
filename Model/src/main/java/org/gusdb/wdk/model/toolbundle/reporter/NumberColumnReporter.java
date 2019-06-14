package org.gusdb.wdk.model.toolbundle.reporter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.reporter.report.NumberReport;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.io.OutputStream;
import java.math.BigDecimal;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public class NumberColumnReporter extends AbstractColumnReporter<BigDecimal> {

  private static final String
    KEY_MAX_VALS = "maxValues",
    KEY_SORT     = "sort";

  private long maxVals = -1;
  private SortDirection sort = SortDirection.DESC;

  @Override
  public ColumnReporter<BigDecimal> copy() {
    var out = copyInto(new NumberColumnReporter());
    out.sort = sort;
    out.maxVals = maxVals;
    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.NUMBER;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject();
  }

  @Override
  public SchemaBuilder inputSpec() {
    var schema = Schema.draft4();
    return schema.asObject()
      .additionalProperties(false)
      .optionalProperty(KEY_MAX_VALS, schema.asInteger().minimum(1)
        .description("Max number of distinct values to return in result"))
      .optionalProperty(KEY_SORT, schema.asString()
        .enumValues(SortDirection.ASC.name(), SortDirection.DESC.name())
        .description("Sort order for distinct values in result"));
  }

  @Override
  public SchemaBuilder outputSpec() {
    return NumberReport.outputSchema();
  }

  @Override
  public Aggregator<BigDecimal> build(OutputStream out) {
    return new Aggregator<>() {
      private final NumberReport report = new NumberReport(maxVals, sort);

      @Override
      public BigDecimal parse(String raw) throws WdkModelException {
        if (raw == null) return null;
        try { return new BigDecimal(raw); }
        catch (Exception e) { throw new WdkModelException(e); }
      }

      @Override
      public void write(BigDecimal field) {
        report.pushValue(field);
      }

      @Override
      public void close() throws WdkModelException {
        try { Jackson.writeValue(out, report); }
        catch (Exception e) { throw new WdkModelException(e); }
      }
    };
  }

  @Override
  public void parseConfig(JsonNode config) {
    if (config.has(KEY_MAX_VALS))
      maxVals = config.get(KEY_MAX_VALS).longValue();
    if (config.has(KEY_SORT))
      sort = SortDirection.valueOf(config.get(KEY_SORT).textValue());
  }
}
