package org.gusdb.wdk.model.user.dataset.event.datastore;

import java.sql.Types;
import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;

/**
 * Repo for DB operations on the {@code UserDatasetOwner} table.
 * <p>
 * {@code UserDatasetOwner} table:
 * <table>
 *   <tr>
 *     <th>Column</th>
 *     <th>Type</th>
 *   </tr>
 *   <tr>
 *     <td><code>USER_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 * </table>
 */
public class UserDatasetOwnerDBActions
{
  private static final String TABLE_USER_DATASET_OWNER = "userdatasetowner";

  private final String     schema;
  private final DataSource ds;

  public UserDatasetOwnerDBActions(String schema, DataSource ds) {
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
