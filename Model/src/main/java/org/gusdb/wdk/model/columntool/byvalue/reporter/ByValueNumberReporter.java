package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.DistributionStreamProvider;
import org.gusdb.fgputil.distribution.FloatingPointBinDistribution;
import org.gusdb.fgputil.distribution.NumberBinDistribution.NumberBinSpec;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueNumberReporter extends AbstractByValueReporter {

  private Double _displayRangeMin;
  private Double _displayRangeMax;
  private Double _binSize;

  @Override
  public SchemaBuilder getInputSchema() {
    UntypedSchema rs = Schema.draft4();
    return rs.asObject()
        .optionalProperty("displayRangeMin", rs.asNumber())
        .optionalProperty("displayRangeMax", rs.asNumber())
        .optionalProperty("binSize", rs.asNumber().minimum(0)); // actually must be >0
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    try {
      _displayRangeMin = doubleOrNull(config, "displayRangeMin");
      _displayRangeMax = doubleOrNull(config, "displayRangeMax");
      _binSize = doubleOrNull(config, "binSize");
      if (_binSize != null && _binSize <= 0)
        throw new ReporterConfigException("binSize must be a positive number");
      if (_displayRangeMin != null && _displayRangeMax != null && _displayRangeMin >= _displayRangeMax)
        throw new ReporterConfigException("displayRangeMax must be greater than displayRangeMin");
      return this;
    }
    catch (JSONException e) {
      throw new ReporterConfigException(e.getMessage());
    }
  }

  @Override
  protected void initialize(String jointAttributeIdSql) throws WdkModelException {
    // if range min or max not specified, then find them
    if (_displayRangeMin == null || _displayRangeMax == null) {

      // SQL to find min and max of the data range
      String sql = "select " +
          "min(" + _attributeField.getName() + ") as min, " +
          "max(" + _attributeField.getName() + ") as max " +
          "from (" + jointAttributeIdSql + ")";

      // fill tuple with min/max
      TwoTuple<Double,Double> dataRange = new SQLRunner(_appDb, sql).executeQuery(rs -> {
        return rs.next() ? new TwoTuple<Double,Double>(rs.getDouble("min"), rs.getDouble("max")) :
          Functions.doThrow(() -> new WdkRuntimeException("Could not find data range for result (no rows)"));
      });

      // assign data range bounds as needed
      if (_displayRangeMin == null || _displayRangeMin >= dataRange.getSecond()) {
        _displayRangeMin = dataRange.getFirst();
      }
      if (_displayRangeMax == null || _displayRangeMax <= dataRange.getFirst()) {
        _displayRangeMax = dataRange.getSecond();
      }

      // calculate bin size if necessary
      if (_binSize == null || (_displayRangeMax - _displayRangeMin) / _binSize > MAX_BIN_COUNT) {
        // calculate bin size based on desired bins; subtract one so remainder falls into the last bin
        _binSize = (_displayRangeMax - _displayRangeMin) / (DEFAULT_BIN_COUNT - 1);
      }
    }
  }

  @Override
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    double value = rs.getDouble(valueColumn);
    return  rs.wasNull() ? null : String.valueOf(value);
  }

  @Override
  protected AbstractDistribution createDistribution(DistributionStreamProvider distributionStreamProvider) {
    return new FloatingPointBinDistribution(distributionStreamProvider, ValueSpec.COUNT, new NumberBinSpec() {
      @Override public Object getDisplayRangeMin() { return _displayRangeMin; }
      @Override public Object getDisplayRangeMax() { return _displayRangeMax; }
      @Override public Object getBinSize() { return _binSize; }
    });
  }

  private Double doubleOrNull(JSONObject config, String key) {
    return config.has(key) ? config.getDouble(key) : null;
  }

}
