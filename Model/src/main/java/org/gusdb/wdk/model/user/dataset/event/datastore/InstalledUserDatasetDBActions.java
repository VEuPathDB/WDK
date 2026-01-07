package org.gusdb.wdk.model.user.dataset.event.datastore;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.ParamBuilder;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.handler.BasicResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces;

import java.sql.Connection;

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
  private static final Logger LOG = LogManager.getLogger(InstalledUserDatasetDBActions.class);
  private static final String TABLE_INSTALLED_USER_DATASET = "installeduserdataset";
  private static final String TABLE_INSTALLED_USER_DATASET_PROJ = "userdatasetproject";

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
      .executeQuery(new ParamBuilder().addLong(userDatasetID), handler);

    return handler.getNumRows() > 0;
  }

  public void insertUserDataset(long userDatasetID, String name, String projectId) {
    // Use a merge in case a dataset is installed by two separate project installers.
    // Only insert if not matched.
    var insertUserDatasetSql = "MERGE INTO " + schema + TABLE_INSTALLED_USER_DATASET + " datasets" +
      " USING (SELECT ? user_dataset_id, ? name FROM dual) to_insert" +
        " ON (datasets.user_dataset_id = to_insert.user_dataset_id)" +
        " WHEN NOT MATCHED THEN INSERT(user_dataset_id, name) VALUES (?, ?)";

    FunctionalInterfaces.ConsumerWithException<Connection> insertUserDatasetFunction = conn -> {
      LOG.info("Executing update to UD with ID " + userDatasetID + " with name " + name);
      new SQLRunner(conn, insertUserDatasetSql, "insert-user-dataset-row")
          .executeUpdate(new Object[]{userDatasetID, name, userDatasetID, name});
    };

    // No need to merge here since row is specific to the installer being run.
    var insertUserDatasetProjectSql = "INSERT INTO " + schema + TABLE_INSTALLED_USER_DATASET_PROJ +
        " (user_dataset_id, project) VALUES (?, ?)";

    //FunctionalInterfaces.ConsumerWithException<Connection> insertUserDatasetProjectFunction = ;

    try {
      // Project must be inserted after userdataset to avoid constraint violations.
      SqlUtils.performInTransaction(ds, insertUserDatasetFunction, conn -> {
        LOG.info("Executing update to UD with ID " + userDatasetID + " with name " + projectId);
        new SQLRunner(conn, insertUserDatasetProjectSql, "insert-user-dataset-project-row")
            .executeUpdate(new Object[]{userDatasetID, projectId});
      });
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteUserDataset(long userDatasetID) {
    var deleteUserDatasetSql = "DELETE FROM " + schema + TABLE_INSTALLED_USER_DATASET + " WHERE user_dataset_id = ?";

    FunctionalInterfaces.ConsumerWithException<Connection> deleteUdFunction = conn ->
        new SQLRunner(conn, deleteUserDatasetSql, "delete-user-dataset-row")
            .executeUpdate(new Object[]{userDatasetID});

    var deleteUserDatasetProjectSql = "DELETE FROM " + schema + TABLE_INSTALLED_USER_DATASET_PROJ + " WHERE user_dataset_id = ?";

    FunctionalInterfaces.ConsumerWithException<Connection> deleteUdProjectsFunction = conn ->
        new SQLRunner(ds, deleteUserDatasetProjectSql, "delete-user-dataset-project-row")
            .executeUpdate(new Object[]{userDatasetID});

    try {
      // Project must be deleted before userdataset to avoid constraint violations.
      SqlUtils.performInTransaction(ds, deleteUdProjectsFunction, deleteUdFunction);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
