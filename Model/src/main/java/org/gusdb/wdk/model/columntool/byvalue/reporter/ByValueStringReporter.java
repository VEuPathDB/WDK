package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.DiscreteDistribution;
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
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    return rs.getString(valueColumn);
  }

  @Override
  protected AbstractDistribution createDistribution(JSONObject config) {
    return new DiscreteDistribution(this, ValueSpec.COUNT);
  }

}