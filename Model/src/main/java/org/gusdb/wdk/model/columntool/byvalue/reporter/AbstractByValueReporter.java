package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.stream.ResultSets;
import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.DistributionStreamProvider;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.SingleAttributeRecordStream;
import org.gusdb.wdk.model.columntool.ColumnReporter;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public abstract class AbstractByValueReporter implements ColumnReporter, DistributionStreamProvider {

  protected static final int MAX_BIN_COUNT = 5000;

  protected abstract String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException;
  protected abstract AbstractDistribution createDistribution(JSONObject config) throws WdkModelException, ReporterConfigException;

  private final List<AttributeFieldDataType> _supportedDataTypes;
  protected AnswerValue _answerValue;
  protected DataSource _appDb;
  protected AttributeField _attributeField;
  protected int _resultSize;
  protected String _jointAttributeSql;
  protected AbstractDistribution _distribution;
  private Stream<TwoTuple<String, Long>> _groupStream;

  protected AbstractByValueReporter(List<AttributeFieldDataType> supportedDataTypes) {
    _supportedDataTypes = supportedDataTypes;
  }

  @Override
  public List<AttributeFieldDataType> getSupportedDataTypes() {
    return _supportedDataTypes;
  }

  @Override
  public AbstractByValueReporter setModelProperties(Map<String,String> properties) {
    // no-op; this reporter does not use any properties
    return this;
  }

  @Override
  public AbstractByValueReporter setAnswerValue(AnswerValue answerValue) throws WdkModelException {
    _answerValue = answerValue;
    _appDb = answerValue.getWdkModel().getAppDb().getDataSource();
    _resultSize = _answerValue.getResultSizeFactory().getResultSize();
    return this;
  }

  @Override
  public AbstractByValueReporter setAttributeField(AttributeField attributeField) {
    _attributeField = attributeField;
    return this;
  }

  @Override
  public long getRecordCount() {
    return _resultSize;
  }

  @Override
  public Stream<TwoTuple<String, Long>> getDistributionStream() {
    String colName = _attributeField.getName();
    String orderedGroupingSql =
        "select * from (" +
        "  select " + colName + " as key, count(" + colName + ") as value" +
        "  from (" + _jointAttributeSql + ")" +
        "  group by " + colName +
        ") " +
        "order by key asc";
    _groupStream = ResultSets.openStream(_appDb, orderedGroupingSql, "attribute-value-distribution",
        row -> Optional.of(new TwoTuple<>(convertToStringValue(row, "key"), row.getLong("value"))));
    return _groupStream;
  }

  @Override
  public final Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    // build base SQL for this answer
    try (SingleAttributeRecordStream attrStream = new SingleAttributeRecordStream(_answerValue, List.of(_attributeField))) {
      _jointAttributeSql = attrStream.getSql();
    }
    _distribution = createDistribution(config);
    return this;
  }

  @Override
  public final void report(OutputStream out) throws WdkModelException {
    try {
      // write distribution response to out
      Writer writer = new BufferedWriter(new OutputStreamWriter(out));
      writer.write(JsonUtil.serializeObject(_distribution.generateDistribution()));
      writer.flush();
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to write distribution response", e);
    }
    finally {
      if (_groupStream != null) {
        _groupStream.close();
      }
    }
  }

  /**
   * {
   *   histogram: [{
   *     value: number
   *     binStart: string
   *     binEnd: string
   *     binLabel: string
   *   }],
   *   statistics: {
   *     subsetSize: integer
   *     subsetMin?: number | String
   *     subsetMax?: number | String
   *     subsetMean?: number | String
   *     numVarValues: integer
   *     numDistinctValues: integer
   *     numDistinctEntityRecords: integer
   *     numMissingCases: integer
   *   }
   * }
   */
  @Override
  public SchemaBuilder getOutputSchema() {
    UntypedSchema rs = Schema.draft4();
    return rs
      .asObject()
      .additionalProperties(false)
      .requiredProperty("histogram")
        .description("ordered array of histogram bins")
        .asArray()
        .items(rs
          .asObject()
          .additionalProperties(false)
          .requiredProperty("value", rs.description("count for this bin").asInteger().minimum(0))
          .requiredProperty("binStart", rs.description("displayable start value for this bin").asString())
          .requiredProperty("binEnd", rs.description("displayable end value for this bin").asString())
          .requiredProperty("binLabel", rs.description("displayable label for this bin").asString())
        )
        .close()
      .requiredProperty("statistics")
        .description("statistics of this dataset gathered during histogram processing")
        .asObject()
        .additionalProperties(false)
        .requiredProperty("subsetSize", rs.description("total size of this result").asInteger().minimum(0))
        .optionalProperty("subsetMin", rs.description("minimum column value in this result").oneOf().addNumber().close().addString().close())
        .optionalProperty("subsetMax", rs.description("maximum column value in this result").oneOf().addNumber().close().addString().close())
        .optionalProperty("subsetMean", rs.description("mean column value in this result").oneOf().addNumber().close().addString().close())
        .requiredProperty("numVarValues", rs.description("total number of values processed").asInteger().minimum(0))
        .requiredProperty("numDistinctValues", rs.description("number of distinct values found").asInteger().minimum(0))
        .requiredProperty("numDistinctEntityRecords", rs.description("number of records in the result (some use cases have >1 value per record)").asInteger().minimum(0))
        .requiredProperty("numMissingCases", rs.description("number of records in the result that had zero values for this column").asInteger().minimum(0))
        .close();
  }
}
