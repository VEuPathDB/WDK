package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.fgputil.db.runner.SQLRunner;

import javax.sql.DataSource;
import java.util.Optional;

public class VDIEntityIdRetriever {
  private DataSource eda;
  private String schema;

  public VDIEntityIdRetriever(DataSource eda, String schema) {
    this.eda = eda;
    this.schema = schema;
  }

  public Optional<String> queryEntityId(String vdiStableId) {
    final String sql = String.format("SELECT internal_abbrev FROM userstudydatasetid u" +
        "JOIN %s.entitytypegraph etg" +
        "ON u.study_stable_id = etg.study_stable_id" +
        "WHERE dataset_stable_id = ?", schema);
    return new SQLRunner(eda, sql).executeQuery(new Object[] { vdiStableId }, rs -> {
      rs.next();
      return Optional.ofNullable(rs.getString("internal_abbrev"));
    });
  }
}
