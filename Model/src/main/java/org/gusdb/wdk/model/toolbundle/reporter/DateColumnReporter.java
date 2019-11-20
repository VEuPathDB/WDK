package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.DATE;

import org.gusdb.fgputil.ComparableLocalDateTime;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.reporter.report.AbstractReport;
import org.gusdb.wdk.model.toolbundle.reporter.report.DateReport;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class DateColumnReporter extends AbstractSingleTypeColumnReporter {

  private static final String KEY_MAX_VALS = "maxValues";

  private static final long DEFAULT_MAX_VALS = -1;

  protected DateColumnReporter() {
    super(DATE);
  }

  @Override
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
    var schema = Schema.draft4();
    return schema.asObject()
      .additionalProperties(false)
      .optionalProperty(KEY_MAX_VALS, schema.asInteger().minimum(0)
        .description("Maximum number of values to return in the result"));
  }

  @Override
  public SchemaBuilder outputSpec(AttributeFieldDataType type) {
    return DateReport.outputSchema();
  }

  @Override
  public ColumnReporterInstance makeInstance(AnswerValue answerValue, AttributeField field,
      ColumnToolConfig config) throws WdkModelException {
    return new ColumnReporterInstanceImpl(answerValue, field, config, this,
        new TypedAggregationColumnProcessor<ComparableLocalDateTime>(
            getConfiguredReport(config.getConfig())));
  }

  private AbstractReport<ComparableLocalDateTime> getConfiguredReport(JsonNode config) {
    long maxVals = !config.has(KEY_MAX_VALS) ? DEFAULT_MAX_VALS :
        config.get(KEY_MAX_VALS).asLong(DEFAULT_MAX_VALS);
    return new DateReport(maxVals, SortDirection.DESC);
  }

}
