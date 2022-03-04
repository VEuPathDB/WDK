package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.wdk.model.record.attribute.AttributeFieldDataType;
import org.gusdb.fgputil.distribution.DiscreteDistribution;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.Schema;
import io.vulpine.lib.json.schema.SchemaBuilder;
import io.vulpine.lib.json.schema.v4.UntypedSchema;

public class ByValueStringReporter extends AbstractByValueReporter {

  public ByValueStringReporter() {
    super(List.of(AttributeFieldDataType.STRING));
  }

  @Override
  public SchemaBuilder getInputSchema() {
    UntypedSchema rs = Schema.draft4();
    return rs.asObject()
        .optionalProperty(OMIT_HISTOGRAM_CONFIG_PROPERTY, rs.asBoolean());
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