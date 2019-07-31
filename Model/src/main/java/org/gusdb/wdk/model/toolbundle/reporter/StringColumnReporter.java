package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.STRING;

import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.reporter.report.StringReport;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class StringColumnReporter extends AbstractSingleTypeColumnReporter {

  private static final String KEY_MAX_VALS = "maxValues";

  private static final String DESC_MAX_VALS = "Maximum number of unique values"
    + "to return in the output \"values\" object";

  private static final long DEFAULT_MAX_VALS = -1;

  public StringColumnReporter() {
    super(STRING);
  }

  @Override
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
    return Schema.draft4()
      .asObject()
      .optionalProperty(KEY_MAX_VALS)
        .asInteger()
        .description(DESC_MAX_VALS)
        .minimum(1)
        .close();
  }

  @Override
  public SchemaBuilder outputSpec(AttributeFieldDataType type) {
    return StringReport.outputSchema();
  }

  @Override
  public ColumnReporterInstance makeInstance(AnswerValue answerValue, AttributeField field,
      ColumnToolConfig config) throws WdkModelException {
    return new ColumnReporterInstanceImpl(answerValue, field, config, this,
        new TypedAggregationColumnProcessor<String>(
            getConfiguredReport(config.getConfig())));
  }

  private StringReport getConfiguredReport(JsonNode config) {
    long maxVals = !config.has(KEY_MAX_VALS) ? DEFAULT_MAX_VALS :
        config.get(KEY_MAX_VALS).asLong(DEFAULT_MAX_VALS);
    return new StringReport(maxVals, SortDirection.DESC);
  }

}
