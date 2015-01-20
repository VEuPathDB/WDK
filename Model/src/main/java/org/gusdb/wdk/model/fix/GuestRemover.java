package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

/**
 * Starting from build-23, we no longer back up user data, and will just delete guest data for each release.
 * 
 * @author Jerric
 *
 */
public class GuestRemover extends BaseCLI {

  private static final String ARG_CUTOFF_DATE = "cutoffDate";

  private static final String GUEST_TABLE = "wdk_guests";

	// 1000 makes the process too slow; 10000 is good but it is considered “long transaction” which will affect the replication.
  private static final int PAGE_SIZE = 9000;

  private static final Logger LOG = Logger.getLogger(GuestRemover.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    GuestRemover backup = new GuestRemover(cmdName);
    try {
      backup.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      LOG.info("WDK User Remover done.");
      System.exit(0);
    }
  }

  private WdkModel wdkModel;
  private String userSchema;

  public GuestRemover(String command) {
    super((command != null) ? command : "wdkGuestRemover",
        "This command removes expired guest user data from user DB.");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");

    addSingleValueOption(ARG_CUTOFF_DATE, true, null, "Any guest user "
        + "created by this date will be backed up, and removed "
        + "from the live schema defined in the model-config.xml. "
        + "The data should be in this format: yyyy/mm/dd");
  }

  @Override
  protected void execute() throws Exception {
    LOG.info("****IN EXECUTE******");

    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    String cutoffDate = (String) getOptionValue(ARG_CUTOFF_DATE);

    wdkModel = WdkModel.construct(projectId, gusHome);
    userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    userSchema = DBPlatform.normalizeSchema(userSchema);

    LOG.info("********** Looking up guest users... **********");
    String guestSql = lookupGuests(userSchema, cutoffDate);
    LOG.info("********** " + guestSql + " **********");
    LOG.info("********** Deleting guest users... **********");
    removeGuests(guestSql);
  }

  /**
   * @param userSchema
   * @param cutoffDate
   * @return a sql that returns the guests to be removed;
   * @throws SQLException
   * @throws DBStateException
   */
  private String lookupGuests(String userSchema, String cutoffDate) throws DBStateException, SQLException {
    // check if the guest table exists
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    String defaultSchema = wdkModel.getUserDb().getDefaultSchema();

    if (platform.checkTableExists(dataSource, defaultSchema, GUEST_TABLE)) {
      // guest table exists, will drop it first.
      SqlUtils.executeUpdate(dataSource, "DROP TABLE " + GUEST_TABLE, "backup-drop-guest-table.");
    }

    // create a new guest table with the guests created before the cutoff date
    SqlUtils.executeUpdate(dataSource, "CREATE TABLE " + GUEST_TABLE + " AS SELECT user_id FROM " +
        userSchema + "users " + " WHERE is_guest = 1 AND register_time < to_date('" + cutoffDate +
        "', 'yyyy/mm/dd')", "backup-create-guest-table");
		SqlUtils.executeUpdate(dataSource,"CREATE UNIQUE INDEX " + GUEST_TABLE + "_ix01 ON " + GUEST_TABLE + " (user_id)", "create-guest-index");

    return "SELECT user_id FROM " + GUEST_TABLE;
  }

  private void removeGuests(String guestSql) throws SQLException {
    LOG.info("****IN REMOVEGUESTS ******");
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    String userClause = "user_id IN (" + guestSql + ")";

		deleteByBatch(dataSource, "dataset_values", " dataset_id IN (SELECT dataset_id FROM " + userSchema +
        "datasets WHERE " + userClause + ")");
    deleteByBatch(dataSource, "datasets", userClause);
    deleteByBatch(dataSource, "preferences", userClause);
    deleteByBatch(dataSource, "user_baskets", userClause);
    deleteByBatch(dataSource, "favorites", userClause);
    deleteByBatch(dataSource, "strategies", userClause);
    deleteByBatch(dataSource, "step_analysis", " step_id IN (SELECT step_id FROM " + userSchema + "steps WHERE " +
        userClause + ")");
    deleteByBatch(dataSource, "steps", userClause + " AND step_id NOT IN (SELECT root_step_id FROM " +
        userSchema + "strategies)");
    deleteByBatch(dataSource, "user_roles", userClause);

    //deleteByBatch(dataSource, "users", userClause + " AND user_id NOT IN (SELECT user_id FROM " + userSchema +  "steps)");
		// jan 12 2015: the second condition makes the delete very slow (40sec for 10000 rows) 
		// and it should not be needed since steps from users in GUEST_TABLE should have been already removed from the steps table 
		deleteByBatch(dataSource, "users", userClause);

    // also delete guest data from GBrowse
    removeGBrowseGuests(dataSource);
  }

  private void removeGBrowseGuests(DataSource dataSource) throws SQLException {
    LOG.info("Deleting from gbrowseusers.sessions...");
    PreparedStatement psDelete = null;
    try {
      psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM gbrowseusers.sessions WHERE id IN ("
          + "  SELECT s.id sessionid FROM gbrowseusers.sessions s "
          + "    LEFT JOIN gbrowseusers.session_tbl st ON s.id = st.sessionid "
          + "    WHERE st.userid IS NULL)");
      int sum = 0;
      while (true) {
        int count = psDelete.executeUpdate();
        if (count == 0)
          break;

        sum += count;
        LOG.debug(sum + " rows deleted so far.");
      }
    }
    finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  private void deleteByBatch(DataSource dataSource, String table, String condition) throws SQLException {
    LOG.info("Deleting from " + table + "...");
    PreparedStatement psDelete = null;
    try {
      psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM " + userSchema + table + " WHERE (" +
				 condition + ") AND rownum <= " + PAGE_SIZE);

      int sum = 0;
      while (true) {
				//executeUpdate includes the commit
        int count = psDelete.executeUpdate();
				LOG.info("****deleted " + count + " rows\n");
        if (count == 0)
          break;
        sum += count;
        LOG.debug(sum + " rows deleted so far.");
      }
    }
    finally {
      SqlUtils.closeStatement(psDelete);
    }
  }
}
