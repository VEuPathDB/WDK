package org.gusdb.wdk.model.user.dataset.event.repo;

import java.sql.Types;
import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;

/**
 * Repository for DB actions relating to user dataset shares.
 * <p>
 * UserDatasetSharedWith table:
 * <table>
 *   <tr>
 *     <th>Column</th>
 *     <th>Type</th>
 *   </tr>
 *   <tr>
 *     <td><code>OWNER_USER_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>RECIPIENT_USER_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 * </table>
 */
public class UserDatasetShareRepo
{
  private static final String TABLE_USER_DATASET_SHARE = "userdatasetsharedwith";

  private final String     schema;
  private final DataSource ds;

  public UserDatasetShareRepo(String schema, DataSource ds) {
    this.schema = schema;
    this.ds     = ds;
  }

  /**
   * Inserts a single share record for the given input params.
   *
   * @param owner         Dataset owner user ID.
   * @param recipient     Share recipient user ID.
   * @param userDatasetID User dataset ID.
   */
  public void insertShare(long owner, long recipient, long userDatasetID) {
    var sql = "INSERT INTO " + schema + TABLE_USER_DATASET_SHARE
      + " (owner_user_id, recipient_user_id, user_dataset_id) VALUES (?, ?, ?)";

    new SQLRunner(ds, sql, "grant-user-dataset-" + TABLE_USER_DATASET_SHARE)
      .executeUpdate(new Object[]{owner, recipient, userDatasetID});
  }

  /**
   * Deletes a single share record matching the given input params.
   *
   * @param owner         Dataset owner user ID
   * @param recipient     Share recipient user ID
   * @param userDatasetID Dataset ID
   */
  public void deleteShare(long owner, long recipient, long userDatasetID) {
    var sql = "DELETE FROM " + schema + TABLE_USER_DATASET_SHARE
      + " WHERE owner_user_id = ?"
      + " AND recipient_user_id = ?"
      + " AND user_dataset_id = ?";

    new SQLRunner(ds, sql, "revoke-user-dataset-" + TABLE_USER_DATASET_SHARE)
      .executeUpdate(new Object[]{owner, recipient, userDatasetID});
  }

  /**
   * Deletes all share records for the given dataset ID.
   *
   * @param userDatasetID ID of the dataset for which all share records should
   *                      be deleted.
   */
  public void deleteAllShares(long userDatasetID) {
    var sql = "DELETE FROM " + schema + TABLE_USER_DATASET_SHARE + " WHERE user_dataset_id = ?";

    new SQLRunner(ds, sql, "revoke-all-user-dataset-access-1")
      .executeUpdate(new Object[]{userDatasetID}, new Integer[]{Types.BIGINT});
  }
}
