package org.gusdb.wdk.model.toolbundle.reporter;

import static org.gusdb.wdk.model.record.attribute.AttributeFieldDataType.NUMBER;

import java.math.BigDecimal;

import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;
import org.gusdb.wdk.model.toolbundle.reporter.report.NumberReport;

import com.fasterxml.jackson.databind.JsonNode;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class NumberColumnReporter extends AbstractSingleTypeColumnReporter {

  private static final String
    KEY_MAX_VALS = "maxValues",
    KEY_SORT     = "sort";


  private static final long DEFAULT_MAX_VALS = -1;
  private static final SortDirection DEFAULT_SORT = SortDirection.DESC;

  protected NumberColumnReporter() {
    super(NUMBER);
  }

  @Override
  public SchemaBuilder getInputSpec(AttributeFieldDataType type) {
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
  public SchemaBuilder outputSpec(AttributeFieldDataType type) {
    return NumberReport.outputSchema();
  }

  @Override
  public ColumnReporterInstance makeInstance(AnswerValue answerValue, AttributeField field,
      ColumnToolConfig config) throws WdkModelException {
    return new ColumnReporterInstanceImpl(answerValue, field, config, this,
        new TypedAggregationColumnProcessor<BigDecimal>(
            getConfiguredReport(config.getConfig())));
  }

  private NumberReport getConfiguredReport(JsonNode config) {
    long maxVals = !config.has(KEY_MAX_VALS) ? DEFAULT_MAX_VALS :
        config.get(KEY_MAX_VALS).asLong(DEFAULT_MAX_VALS);
    SortDirection sort = config.has(KEY_SORT) ? DEFAULT_SORT :
        SortDirection.valueOf(config.get(KEY_SORT).textValue());
    return new NumberReport(maxVals, sort);
  }
}
