package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.fgputil.db.runner.SQLRunner;

import javax.sql.DataSource;
import java.util.Optional;

public class VDIEntityIdRetriever {
  private DataSource eda;

  public VDIEntityIdRetriever(DataSource eda) {
    this.eda = eda;
  }

  public Optional<String> queryEntityId(String vdiStableId) {
    final String sql = "SELECT internal_abbrev FROM userstudydatasetid u" +
        "JOIN vdi_datasets_dev_s.entitytypegraph etg" +
        "ON u.study_stable_id = etg.study_stable_id" +
        "WHERE dataset_stable_id = ?";
    return new SQLRunner(eda, sql).executeQuery(new Object[] { vdiStableId }, rs -> {
      rs.next();
      return Optional.ofNullable(rs.getString("internal_abbrev"));
    });
  }
}
