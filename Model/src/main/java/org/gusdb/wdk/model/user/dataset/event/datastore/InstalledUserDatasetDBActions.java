package org.gusdb.wdk.model.user.dataset.event.datastore;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;

import java.util.Set;

/**
 * Repository for DB actions relating to the {@code InstalledUserDataset} DB
 * table.
 * <p>
 * {@code InstalledUserDataset} table:
 * <table>
 *   <caption>User Dataset Columns Definition</caption>
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
public class InstalledUserDatasetDBActions
{
  private static final String TABLE_INSTALLED_USER_DATASET = "installeduserdataset";
  private static final String TABLE_INSTALLED_USER_DATASET_PROJ = "installeduserdatasetproject";

  private final String     schema;
  private final DataSource ds;

  public InstalledUserDatasetDBActions(String schema, DataSource ds) {
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

  public void insertUserDataset(long userDatasetID, String name, String projectId) {
    // Use a merge in case a dataset is installed by two separate project installers.
    // Only insert if not matched.
    var insertUserDatasetSql = "MERGE INTO " + schema + TABLE_INSTALLED_USER_DATASET +
      " WHEN NOT MATCHED THEN INSERT(user_dataset_id, name) VALUES (?, ?)";

    new SQLRunner(ds, insertUserDatasetSql, "insert-user-dataset-row")
      .executeUpdate(new Object[]{userDatasetID, name, userDatasetID});

    // No need to merge here since row is specific to the installer being run.
    var insertUserDatasetProjectSql = "INSERT INTO " + schema + TABLE_INSTALLED_USER_DATASET_PROJ +
        " (user_dataset_id, project_id) VALUES (?, ?)";

    new SQLRunner(ds, insertUserDatasetProjectSql, "insert-user-dataset-project-row")
        .executeUpdate(new Object[]{userDatasetID, projectId});
  }

  public void deleteUserDataset(long userDatasetID) {
    var sql = "DELETE FROM " + schema + TABLE_INSTALLED_USER_DATASET + " WHERE user_dataset_id = ?";

    new SQLRunner(ds, sql, "delete-user-dataset-row").executeUpdate(new Object[]{userDatasetID});
  }
}
