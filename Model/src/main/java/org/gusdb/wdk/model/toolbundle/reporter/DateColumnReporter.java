package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.reporter.report.DateReport;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class DateColumnReporter extends AbstractColumnReporter<LocalDateTime> {
  public static final String
    KEY_MAX_VALS = "maxValues";

  private long maxVals = -1;

  @Override
  public DateColumnReporter copy() {
    var out = copyInto(new DateColumnReporter());
    out.maxVals = maxVals;
    return out;
  }

  @Override
  public boolean isCompatibleWith(AttributeFieldDataType type) {
    return type == AttributeFieldDataType.DATE;
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
        .description("Maximum number of values to return in the result"));
  }

  @Override
  public SchemaBuilder outputSpec() {
    return DateReport.outputSchema();
  }

  @Override
  public Aggregator<LocalDateTime> build(OutputStream out) {
    return new Aggregator<>() {
      private final DateReport rep = new DateReport(maxVals, SortDirection.DESC);

      @Override
      public LocalDateTime parse(String val) throws WdkModelException {
        if (val == null)
          return null;
        return tryParse(val)
          .mapError(WdkModelException::new)
          .valueOrElseThrow();
      }

      @Override
      public void write(LocalDateTime field) {
        rep.pushValue(field);
      }

      @Override
      public void close() throws WdkModelException {
        try { Jackson.writeValue(out, rep); }
        catch (Exception e) { throw new WdkModelException(e); }
      }
    };
  }

  @Override
  public void parseConfig(JsonNode config) {
    // TODO: Sort direction should maybe be configurable.
  }

  public Result<Exception, LocalDateTime> tryParse(String val) {
    var err = new ArrayList<String>();
    err.add("Failed to parse date: " + val);

    // Default parse
    try { return Result.value(LocalDateTime.parse(val)); }
    catch (Exception e) { err.add(e.getMessage()); }

    // Wonkydate parse
    try { return Result.value(LocalDateTime.parse(val,
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))); }
    catch (Exception e) { err.add(e.getMessage()); }

    return Result.error(new Exception(String.join(NL, err)));
  }
}
