package org.gusdb.wdk.model.user.dataset.event.repo;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;

public class InstalledUserDatasetRepo
{
  private static final String TABLE_INSTALLED_USER_DATASET = "installeduserdataset";

  private static final Logger LOG = LogManager.getLogger(InstalledUserDatasetRepo.class);

  private final String     schema;
  private final DataSource ds;

  public InstalledUserDatasetRepo(String schema, DataSource ds) {
    this.schema = schema;
    this.ds     = ds;
  }

  public boolean isUserDatasetInstalled(long userDatasetID) {
    var handler = new BasicResultSetHandler();
    var sql = "SELECT user_dataset_id"
      + " FROM " + schema + TABLE_INSTALLED_USER_DATASET
      + " WHERE user_dataset_id = ?";

    new SQLRunner(ds, sql, "check-user-dataset-exists")
      .executeQuery(new Object[]{userDatasetID}, handler);

    return handler.getNumRows() > 0;
  }

  public void insertUserDataset(long userDatasetID, String name) {
    var sql = "INSERT INTO " + schema + TABLE_INSTALLED_USER_DATASET +
      " (user_dataset_id, name) VALUES (?, ?)";

    new SQLRunner(ds, sql, "insert-user-dataset-row")
      .executeUpdate(new Object[]{userDatasetID, name});
  }

  public void deleteUserDataset(long userDatasetID) {
    var sql = "DELETE FROM " + schema + TABLE_INSTALLED_USER_DATASET + " WHERE user_dataset_id = ?";

    new SQLRunner(ds, sql, "delete-user-dataset-row").executeUpdate(new Object[]{userDatasetID});
  }
}
