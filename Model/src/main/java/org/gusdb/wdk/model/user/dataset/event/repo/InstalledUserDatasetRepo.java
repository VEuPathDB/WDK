package org.gusdb.wdk.model.user.dataset.event.repo;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;

/**
 * Repository for DB actions relating to installed user datasets.
 * <p>
 * InstalledUserDataset table:
 * <table>
 *   <tr>
 *     <th>Column</th>
 *     <th>Type</th>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_ID</code></td>
 *     <td><code>long</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>NAME</code></td>
 *     <td><code>String</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>IS_INVALID</code></td>
 *     <td><code>boolean</code></td>
 *   </tr>
 *   <tr>
 *     <td><code>INVALID_REASON</code></td>
 *     <td><code>String</code></td>
 *   </tr>
 * </table>
 */
public class InstalledUserDatasetRepo
{
  private static final String TABLE_INSTALLED_USER_DATASET = "installeduserdataset";

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
