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
 * Starting build 24 we add deletion of deleted strategies/steps and deletion of steps not connected to a strategy
 * Starting build 25 we move all cleaning activity to its own script CleanBrokenStratsSteps
 * Starting build 27 we select not only guest users but also last_active null users
 * @author Jerric
 *
 */
public class GuestRemover extends BaseCLI {

  private static final String ARG_CUTOFF_DATE = "cutoffDate";

  private static final String GUEST_TABLE = "wdk_guests";

  // 1000 makes the process too slow; 10000 is good but it is considered “long transaction” which will affect
  // the replication.
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

  public static int deleteByBatch(DataSource dataSource, String table, String condition) throws SQLException {
    LOG.info("\n\nDeleting from table: " + table + " with condition: " + condition);
    PreparedStatement psDelete = null;
    try {
      String sql = "DELETE FROM " + table + " WHERE " + condition + " AND rownum < " + PAGE_SIZE;
      psDelete = SqlUtils.getPreparedStatement(dataSource, sql);

      int sum = 0;
      while (true) {
        // executeUpdate includes the commit
        int count = psDelete.executeUpdate();
        if (count == 0)
          break;
        sum += count;
        LOG.debug(sum + " rows deleted so far.");
      }
      LOG.debug("***** totally deleted " + sum + " rows. *****");
      return sum;
    }
    finally {
      SqlUtils.closeStatement(psDelete);
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
        + "created BEFORE this date, and the user's data, will be removed "
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

    LOG.info("********** Looking up guest users: generate temp table wdk_guests... **********");
    String guestSql = lookupGuests(userSchema, cutoffDate);
    LOG.info("********** " + guestSql + " **********");
 
    LOG.info("********** Deleting all data belonging to guest users in userlogins5 schema... **********");
    removeGuests(guestSql);

    LOG.info("********** Deleting all data belonging to guest users in gbrowseusers schema... **********"); 
		DataSource dataSource = wdkModel.getUserDb().getDataSource();
		// though we cannot use the cutoff date here...
    removeGBrowseGuests(dataSource);

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
        userSchema + "users " + " WHERE (is_guest = 1 OR last_active is NULL) AND register_time < to_date('" + cutoffDate +
        "', 'yyyy/mm/dd')", "backup-create-guest-table");
    SqlUtils.executeUpdate(dataSource, "CREATE UNIQUE INDEX " + GUEST_TABLE + "_ix01 ON " + GUEST_TABLE +
        " (user_id)", "create-guest-index");
    //return "SELECT user_id FROM " + GUEST_TABLE;
		return "SELECT '1' FROM " + GUEST_TABLE + " g WHERE g.user_id = t.user_id";
  }

  private void removeGuests(String guestSql) throws SQLException {
    LOG.info("****IN REMOVEGUESTS ******");
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    //String userClause = "user_id IN (" + guestSql + ")";
		String userClause = " EXISTS (" + guestSql + ")";

    deleteByBatch(dataSource, userSchema + "dataset_values", " dataset_id IN (SELECT dataset_id FROM " +
        userSchema + "datasets t WHERE " + userClause + ")");
    deleteByBatch(dataSource, userSchema + "datasets t", userClause);
    deleteByBatch(dataSource, userSchema + "preferences t", userClause);
    deleteByBatch(dataSource, userSchema + "user_baskets t", userClause); // we dont know why we get some
    deleteByBatch(dataSource, userSchema + "favorites t", userClause);
    deleteByBatch(dataSource, userSchema + "strategies t", userClause);
    deleteByBatch(dataSource, userSchema + "step_analysis", " step_id IN (SELECT step_id FROM " + userSchema +
        "steps t WHERE " + userClause + ")");
    deleteByBatch(dataSource, userSchema + "steps t", userClause);
								//	" AND step_id NOT IN (SELECT root_step_id FROM " + userSchema + "strategies)");
    deleteByBatch(dataSource, userSchema + "user_roles t", userClause);
    deleteByBatch(dataSource, userSchema + "users t", userClause);
									// " AND user_id NOT IN (SELECT user_id FROM " + userSchema + "steps)");
  }

  private void removeGBrowseGuests(DataSource dataSource) throws SQLException {
    LOG.info("\n\nDeleting from gbrowseusers.sessions...");
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


}
