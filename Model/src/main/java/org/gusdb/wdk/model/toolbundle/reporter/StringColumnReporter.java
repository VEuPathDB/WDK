package org.gusdb.wdk.model.toolbundle.reporter;

import com.fasterxml.jackson.databind.JsonNode;
import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.toolbundle.reporter.report.StringReport;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;

import java.io.OutputStream;

import static org.gusdb.fgputil.json.JsonUtil.Jackson;

public class StringColumnReporter extends AbstractColumnReporter<String> {

  private static final String KEY_MAX_VALS = "maxValues";
  private static final String DESC_MAX_VALS = "Maximum number of unique values"
    + "to return in the output \"values\" object";

  private long maxVals = -1;

  @Override
  public SchemaBuilder inputSpec() {
    return Schema.draft4()
      .asObject()
      .optionalProperty(KEY_MAX_VALS)
        .asInteger()
        .description(DESC_MAX_VALS)
        .minimum(1)
        .close();
  }

  @Override
  public SchemaBuilder outputSpec() {
    return StringReport.outputSchema();
  }

  @Override
  public ColumnReporter<String> copy() {
    var out = copyInto(new StringColumnReporter());
    out.maxVals = maxVals;
    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.STRING;
  }

  @Override
  public boolean isCompatibleWith(JsonNode js) {
    return js.isObject();
  }

  @Override
  public void parseConfig(JsonNode js) {
    if (js.has(KEY_MAX_VALS))
      maxVals = js.get(KEY_MAX_VALS).longValue();
  }

  @Override
  public Aggregator<String> build(OutputStream out) {
    return new Aggregator<>() {
      private final StringReport report = new StringReport(maxVals, SortDirection.DESC);
      private int i;
      private double collects;
      private final Logger log = LogManager.getLogger(this.getClass());

      @Override
      public String parse(String raw) {
        return raw;
      }

      @Override
      public void write(String field) {
        if (i >= 10_000) {
          i = 0;
          log.debug(i + " collects @" + (collects / 1_000_000D) + "ms");
        }
        var a = System.nanoTime();
        report.pushValue(field);
        collects = System.nanoTime() - a;
      }

      @Override
      public void close() throws WdkModelException {
        try { Jackson.writeValue(out, report); }
        catch (Exception e) { throw new WdkModelException(e); }
      }
    };
  }
}
