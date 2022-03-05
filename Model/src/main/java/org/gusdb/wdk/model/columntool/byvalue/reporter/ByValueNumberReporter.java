package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.gusdb.fgputil.Range;
import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.FloatingPointBinDistribution;
import org.gusdb.fgputil.distribution.NumberBinDistribution.NumberBinSpec;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueNumberReporter extends AbstractByValueReporter {

  private static final int DEFAULT_BIN_COUNT = 20;

  private static final String PROP_BIN_SIZE = "binSize";

  public ByValueNumberReporter() {
    super(List.of(AttributeFieldDataType.NUMBER));
  }

  @Override
  public SchemaBuilder getInputSchema() {
    UntypedSchema rs = Schema.draft4();
    return rs.asObject()
        .optionalProperty(OMIT_HISTOGRAM_CONFIG_PROPERTY, rs.asBoolean())
        .optionalProperty(DisplayRangeFactory.PROP_DISPLAY_RANGE_MIN, rs.asNumber())
        .optionalProperty(DisplayRangeFactory.PROP_DISPLAY_RANGE_MAX, rs.asNumber())
        .optionalProperty(PROP_BIN_SIZE, rs.asNumber().minimum(0)); // actually must be > 0
  }

  @Override
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    return String.valueOf(readDouble(rs, valueColumn));
  }

  @Override
  protected AbstractDistribution createDistribution(JSONObject config) throws WdkModelException, ReporterConfigException {
    try {
      return new FloatingPointBinDistribution(this, ValueSpec.COUNT, determineBinSpec(config));
    }
    // important to catch these here and convert to avoid 500s
    catch (JSONException | IllegalArgumentException e) {
      throw new ReporterConfigException(e.getMessage());
    }
  }

  private NumberBinSpec determineBinSpec(JSONObject config) throws ReporterConfigException {

    Range<Double> displayRange = new DisplayRangeFactory(_appDb, _attributeField.getName(), _jointAttributeSql)
        .calculateDisplayRange(config,
            (json, key) -> doubleOrNull(json, key),
            (rs, key) -> readDouble(rs, key));

    Double binSize = calculateBinSize(displayRange, config);

    return new NumberBinSpec() {
      @Override public Object getDisplayRangeMin() { return displayRange.getBegin(); }
      @Override public Object getDisplayRangeMax() { return displayRange.getEnd(); }
      @Override public Object getBinSize()         { return binSize; }
    };
  }

  private static Double calculateBinSize(Range<Double> displayRange, JSONObject config) {

    Double binSize = doubleOrNull(config, PROP_BIN_SIZE);

    if (binSize != null) {
      if (binSize <= 0)
        throw new IllegalArgumentException(PROP_BIN_SIZE + " must be a positive number");

      long numPredictedBins = Double.valueOf(Math.ceil((displayRange.getEnd() - displayRange.getBegin()) / binSize)).longValue();
      if (numPredictedBins > MAX_BIN_COUNT)
        throw new IllegalArgumentException("The predicted bin count (" +
            numPredictedBins + ") is higher than the allowed maximum, " + MAX_BIN_COUNT);

      // passed checks
      return binSize;
    }

    // bin size null; calculate based on desired bins; subtract one so remainder falls into the last bin
    return (displayRange.getEnd() - displayRange.getBegin()) / (DEFAULT_BIN_COUNT - 1);
  }

  private static Double readDouble(ResultSet rs, String columnName) throws SQLException {
    double value = rs.getDouble(columnName);
    return rs.wasNull() ? null : value;
  }

  private static Double doubleOrNull(JSONObject config, String key) {
    return config.has(key) ? config.getDouble(key) : null;
  }
}
