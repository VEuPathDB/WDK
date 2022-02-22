package org.gusdb.wdk.model.columntool.byvalue.reporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.distribution.AbstractDistribution;
import org.gusdb.fgputil.distribution.DistributionStreamProvider;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.json.JSONObject;

import io.vulpine.lib.json.schema.SchemaBuilder;

public class ByValueDateReporter extends AbstractByValueReporter {

  @Override
  public SchemaBuilder getInputSchema() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void initialize(String jointAttributeIdSql) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected String convertToStringValue(ResultSet rs, String valueColumn) throws SQLException {
    // utility to convert null DB date values to real null
    Date value = rs.getDate(valueColumn);
    return value == null ? null : FormatUtil.formatDateTimeNoTimezone(value);
  }

  @Override
  protected AbstractDistribution createDistribution(DistributionStreamProvider distributionStreamProvider) {
    // TODO Auto-generated method stub
    return null;
  }

}
