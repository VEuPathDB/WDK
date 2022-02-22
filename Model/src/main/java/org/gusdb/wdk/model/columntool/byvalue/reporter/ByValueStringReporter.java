package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.DiscreteDistribution;
import org.gusdb.fgputil.distribution.DistributionStreamProvider;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;

public class ByValueStringReporter extends AbstractByValueReporter {

  @Override
  public SchemaBuilder getInputSchema() {
    // no configuration needed for this reporter
    return Schema.draft4().asObject().additionalProperties(false);
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    // nothing to do here
    return this;
  }

  @Override
  protected void initialize(String jointAttributeIdSql) throws WdkModelException {
    // nothing to do here
  }

  @Override
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    return rs.getString(valueColumn);
  }

  @Override
  protected AbstractDistribution createDistribution(DistributionStreamProvider distributionStreamProvider) {
    return new DiscreteDistribution(distributionStreamProvider, ValueSpec.COUNT);
  }

}