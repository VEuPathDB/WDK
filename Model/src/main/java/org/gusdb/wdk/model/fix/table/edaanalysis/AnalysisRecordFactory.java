package org.gusdb.wdk.model.fix.table.edaanalysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowFactory;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowWriter;

/**
 * Provides the read/write mechanisms for updating EDA analysis table rows needed by the
 * TableRowUpdater API.
 */
public class AnalysisRecordFactory implements TableRowFactory<AnalysisRow>, TableRowWriter<AnalysisRow> {

  private String _schema;

  public AnalysisRecordFactory(String projectId) {
    // set schema based on project (userdb schema is not where eda data lives
    switch(projectId) {
      case "ClinEpiDB": _schema = "edauserce"; break;
      case "MicrobiomeDB": _schema = "edausermb"; break;
      default: _schema = "edauservb"; break;
    }
  }

  @Override
  public String getRecordsSql(String schema, String projectId) {
    return
        "select analysis_id, study_id, analysis_descriptor, num_filters, num_computations, num_visualizations" +
        " from " + _schema + ".analysis";
  }

  @Override
  public AnalysisRow newTableRow(ResultSet rs, DBPlatform platform) throws SQLException {
    return new AnalysisRow(rs, platform);
  }

  @Override
  public List<String> getTableNamesForBackup(String schema) {
    return List.of(_schema + "_analysis");
  }

  @Override
  public String getWriteSql(String schema) {
    return
        "update " + _schema + ".analysis set study_id = ?, " +
            "analysis_descriptor = ?, num_filters = ?, num_computations = ?, num_visualizations = ? " +
            "where analysis_id = ?";
  }

  @Override
  public Integer[] getParameterTypes() {
    return new Integer[] {
        Types.VARCHAR,
        Types.CLOB,
        Types.INTEGER,
        Types.INTEGER,
        Types.INTEGER,
        Types.VARCHAR
    };
  }

  @Override
  public Collection<Object[]> toValues(AnalysisRow obj) {
    return ListBuilder.asList(obj.toOrderedValues());
  }

  @Override
  public void setUp(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

  @Override
  public void tearDown(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

}
