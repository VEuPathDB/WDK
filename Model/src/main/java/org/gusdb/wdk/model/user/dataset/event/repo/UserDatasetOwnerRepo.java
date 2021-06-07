package org.gusdb.wdk.model.user.dataset.event.repo;

import java.sql.Types;
import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;

/**
 * Repo for DB operations on the "userdatasetowner" table.
 */
public class UserDatasetOwnerRepo
{
  private static final String TABLE_USER_DATASET_OWNER = "userdatasetowner";

  private final String     schema;
  private final DataSource ds;

  public UserDatasetOwnerRepo(String schema, DataSource ds) {
    this.schema = schema;
    this.ds     = ds;
  }

  public void insertOwner(long userID, long userDatasetID) {
    var sql = "INSERT INTO " + schema + TABLE_USER_DATASET_OWNER + " (user_id, user_dataset_id)"
      + " VALUES (?, ?)";

    new SQLRunner(ds, sql, "grant-user-dataset-" + TABLE_USER_DATASET_OWNER)
      .executeUpdate(new Object[]{userID, userDatasetID}, new Integer[]{Types.BIGINT, Types.BIGINT});
  }

  public void deleteAllOwners(long userDatasetID) {
    var sql = "DELETE FROM " + schema + TABLE_USER_DATASET_OWNER + " WHERE user_dataset_id = ?";

    new SQLRunner(ds, sql, "revoke-all-user-dataset-access-1")
      .executeUpdate(new Object[]{userDatasetID}, new Integer[]{Types.BIGINT});

  }
}
