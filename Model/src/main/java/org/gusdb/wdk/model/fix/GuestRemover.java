package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;

/**
 * Starting from build-23, we no longer back up user data, and will just delete guest data for each release.
 * Starting build 24 we add deletion of deleted strategies/steps and deletion of steps not connected to a strategy
 * Starting build 25 we move all cleaning activity to its own script CleanBrokenStratsSteps
 * Starting build 27 we select not only guest users but also last_active null users
 * starting build 29 gus4 we remove last_active null condition given that thee are few and they might want to just use our galaxy
 *
 * @author Jerric
 */
public class GuestRemover extends BaseCLI {

  private static final Logger LOG = Logger.getLogger(GuestRemover.class);

  private static final String ARG_CUTOFF_DATE = "cutoffDate";

  private static final String GUEST_TABLE = "wdk_guests";

  /**
   * 1000 makes the process too slow; 10000 is good but it is considered a 
   * “long transaction” which will affect replication.
   */
  private static final int PAGE_SIZE = 9000;

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    GuestRemover backup = new GuestRemover(cmdName);
    try {
      backup.invoke(args);
      LOG.info("WDK User Remover done.");
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

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

    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    String cutoffDate = (String) getOptionValue(ARG_CUTOFF_DATE);

    try (WdkModel wdkModel = WdkModel.construct(projectId, GusHome.getGusHome())) {

      DatabaseInstance userDb = wdkModel.getUserDb();
      String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  
      LOG.info("********** Looking up guest users: generate temp table wdk_guests... **********");
      loadGuestIdTable(userDb, userSchema, cutoffDate);
   
      LOG.info("********** Deleting all data belonging to guest users in userlogins5 schema... **********");
      removeGuests(userDb, userSchema);

    }
  }

  /**
   * @param userSchema
   * @param cutoffDate
   * @return an sql query that returns the guests to be removed;
   * @throws SQLException
   * @throws DBStateException
   */
  private void loadGuestIdTable(DatabaseInstance userDb, String userSchema, String cutoffDate) throws DBStateException, SQLException {

    // check if the guest table exists
    DBPlatform platform = userDb.getPlatform();
    DataSource dataSource = userDb.getDataSource();
    String defaultSchema = userDb.getDefaultSchema();

    if (platform.checkTableExists(dataSource, defaultSchema, GUEST_TABLE)) {
      // guest table exists, will drop it first.
      SqlUtils.executeUpdate(dataSource,
          "DROP TABLE " + GUEST_TABLE,
          "backup-drop-guest-table");
    }

    // create a new guest table with the guests created before the cutoff date
    SqlUtils.executeUpdate(dataSource,
        "CREATE TABLE " + GUEST_TABLE + " AS " +
            "SELECT user_id " +
            "FROM " + userSchema + "users " +
            "WHERE is_guest = 1 " +
            "AND first_access < to_date('" + cutoffDate + "', 'yyyy/mm/dd')",
        "backup-create-guest-table");

    // create an index on the table for faster joins
    SqlUtils.executeUpdate(dataSource,
        "CREATE UNIQUE INDEX " + GUEST_TABLE + "_ix01 ON " + GUEST_TABLE + " (user_id)",
        "create-guest-index");

  }

  private void removeGuests(DatabaseInstance userDb, String userSchema) throws SQLException {

    LOG.info("****IN REMOVEGUESTS ******");
    DataSource dataSource = userDb.getDataSource();
    String userClause = " EXISTS ( SELECT '1' FROM " + GUEST_TABLE + " g WHERE g.user_id = t.user_id )";

    deleteByBatch(dataSource, userSchema + "dataset_values", " dataset_id IN (SELECT dataset_id FROM " + userSchema + "datasets t WHERE " + userClause + ")");
    deleteByBatch(dataSource, userSchema + "datasets t", userClause);
    deleteByBatch(dataSource, userSchema + "preferences t", userClause);
    deleteByBatch(dataSource, userSchema + "user_baskets t", userClause); // we don't know why we get some
    deleteByBatch(dataSource, userSchema + "favorites t", userClause);
    deleteByBatch(dataSource, userSchema + "strategies t", userClause);
    deleteByBatch(dataSource, userSchema + "step_analysis", " step_id IN (SELECT step_id FROM " + userSchema + "steps t WHERE " + userClause + ")");
    deleteByBatch(dataSource, userSchema + "steps t", userClause);
    deleteByBatch(dataSource, userSchema + "user_roles t", userClause);
    deleteByBatch(dataSource, userSchema + "users t", userClause);
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
        LOG.info(sum + " rows deleted so far.");
      }
      LOG.info("***** Deleted " + sum + " total rows from " + table + " *****");
      return sum;
    }
    finally {
      SqlUtils.closeStatement(psDelete);
    }
  }
}
