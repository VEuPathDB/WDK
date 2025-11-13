package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Starting from build-23, we no longer back up user data, and will just delete guest data for each release.
 * Starting build 24 we add deletion of deleted strategies/steps and deletion of steps not connected to a strategy
 * Starting build 25 we move all cleaning activity to its own script CleanBrokenStratsSteps
 * Starting build 27 we select not only guest users but also last_active null users
 * starting build 29 gus4 we remove last_active null condition given that thee are few and they might want to just use our galaxy
 * starting build 35 we have the option of removing those guest users having a first access that falls within a given period starting
 * from the oldest first access found in the database.
 * @author Jerric
 *
 */
public class RetroactiveGuestRemover extends BaseCLI {

  private static final String ARG_CUTOFF_DATE = "cutoffDate";
  private static final String ARG_OLDEST_DAY_COUNT = "oldestDayCount";

  private static final String GUEST_TABLE = "wdk_guests";

  // 1000 makes the process too slow; 10000 is good but it is considered “long transaction” which will affect
  // the replication.
  private static final int PAGE_SIZE = 9000;

  private static final Logger LOG = Logger.getLogger(RetroactiveGuestRemover.class);

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    RetroactiveGuestRemover backup = new RetroactiveGuestRemover(cmdName);
    try {
      backup.invoke(args);
      LOG.info("WDK Retroactive Guest User Remover done.");
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  /**
   * Obtains the registry date for the oldest wdk guest user and adds two weeks to it to
   * obtain the cutoff date.  If the cutoff date is inside the last month, return a 
   * cutoff date of one month prior to the current date instead.
   * @param dataSource
   * @param userSchema
   * @param oldestNumberOfDays - awkward name for a parameter that indicates how many
   * days forward from the oldest guest user registry one goes to select a cutoff date.
   * @return
   * @throws WdkModelException
   */
  public static Date deriveCutoffDate(DataSource dataSource, String userSchema, String oldestNumberOfDays) throws WdkModelException {
    int dayCount = 14;  
    try {  
      dayCount = Integer.parseInt(oldestNumberOfDays);
    }
    catch(NumberFormatException nfe) {
      throw new WdkModelException("The oldest number of days argument, '" + oldestNumberOfDays + "', must be an integer.");
    }
    Calendar oneMonthPriorToNowCal = Calendar.getInstance();
    oneMonthPriorToNowCal.add(Calendar.MONTH, -1);
    Calendar cutoffCal = findOldestGuestUserRegistry(dataSource, userSchema);
    cutoffCal.add(Calendar.DATE, dayCount);
    if(cutoffCal.getTimeInMillis() >= oneMonthPriorToNowCal.getTimeInMillis()) {
      oneMonthPriorToNowCal.getTime();
    }
    return cutoffCal.getTime();
  }

  /**
   * Runs a sql query against users table to find the oldest wdk guest user and returns the registry date
   * for that user.
   * @param dataSource
   * @param userSchema
   * @return
   * @throws WdkModelException
   */
  public static Calendar findOldestGuestUserRegistry(DataSource dataSource, String userSchema) throws WdkModelException {
    String sql = "SELECT min(first_access) AS oldest_date FROM " + userSchema + "users WHERE is_guest = 1";
    try {
      return new SQLRunner(dataSource, sql).executeQuery(resultSet -> {
        Calendar calendar = Calendar.getInstance();
        if(resultSet.next()) {
          calendar.setTime(resultSet.getTimestamp("oldest_date"));
        }
        return calendar;
      });
    }
    catch(SQLRunnerException se) {
      return WdkModelException.unwrap(se, "Unable to obtain oldest guest first access.");
    }
  }

  public static int deleteByBatch(DataSource dataSource, String table, String condition) throws SQLException {
    LOG.info("\n\nDeleting from table: " + table + " with condition: " + condition);
    PreparedStatement psDelete = null;
    try {
      String sql = "DELETE FROM " + table + " WHERE " + condition + " AND rownum < " + PAGE_SIZE;
      psDelete = SqlUtils.getPreparedStatement(dataSource, sql, SqlUtils.Autocommit.ON);

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

  public RetroactiveGuestRemover(String command) {
    super((command != null) ? command : "wdkRetroactiveGuestRemover",
        "This command removes expired guest user data from user DB.");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");

    addSingleValueOption(ARG_CUTOFF_DATE, false, null, "Any guest user "
        + "created BEFORE this date, and the user's data, will be removed "
        + "from the live schema defined in the model-config.xml. "
        + "The date should be in this format: yyyy-mm-dd");
    
    addSingleValueOption(ARG_OLDEST_DAY_COUNT, false, null, "Finds the "
    	+ "oldest registry for a guest user and counts up the number of "
    	+ "days provided to derive a cutoff date.  Alternative mechanism "
    	+ "meant to help eliminate bloat incrementally.");
  }

  @Override
  protected void execute() throws Exception {
    LOG.info("****IN EXECUTE******");

    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    String cutoffDateStr = (String) getOptionValue(ARG_CUTOFF_DATE);
    String oldestDayCount = (String) getOptionValue(ARG_OLDEST_DAY_COUNT);
    
    try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {
      DatabaseInstance userDb = wdkModel.getUserDb();
      String userSchema = DBPlatform.normalizeSchema(wdkModel.getModelConfig().getUserDB().getUserSchema());

      Date cutoffDate = (cutoffDateStr == null || cutoffDateStr.isEmpty()) &&
                          (oldestDayCount != null && !oldestDayCount.isEmpty()) ?
                        deriveCutoffDate(userDb.getDataSource(), userSchema, oldestDayCount) :
                        FormatUtil.toDate(FormatUtil.parseDate(cutoffDateStr));
      
      LOG.info("********** Cutoff Date: " + cutoffDate + " **********");
      
      LOG.info("********** Looking up guest users: generate temp table wdk_guests... **********");
      String guestSql = lookupGuests(userDb, userSchema, cutoffDate);
      LOG.info("********** " + guestSql + " **********");
   
      LOG.info("********** Deleting all data belonging to guest users in userlogins5 schema... **********");
      removeGuests(userDb, userSchema, guestSql);
  
      LOG.info("********** Deleting all data belonging to guest users in gbrowseusers schema... **********");
      // though we cannot use the cutoff date here...
      removeGBrowseGuests(userDb.getDataSource());
      
    }
  }

  /**
   * @param userSchema
   * @param cutoffDate
   * @return a sql that returns the guests to be removed;
   * @throws SQLException
   * @throws DBStateException
   */
  private String lookupGuests(DatabaseInstance userDb, String userSchema, Date cutoffDate) throws DBStateException, SQLException {
    // check if the guest table exists
    DBPlatform platform = userDb.getPlatform();
    DataSource dataSource = userDb.getDataSource();
    String defaultSchema = userDb.getDefaultSchema();
    if (platform.checkTableExists(dataSource, defaultSchema, GUEST_TABLE)) {
      // guest table exists, will drop it first.
      SqlUtils.executeUpdate(dataSource, "DROP TABLE " + GUEST_TABLE, "backup-drop-guest-table.");
    }
    // create a new guest table with the guests created before the cutoff date
    SqlUtils.executeUpdate(dataSource, "CREATE TABLE " + GUEST_TABLE + " AS SELECT user_id FROM " +
        userSchema + "users " + " WHERE is_guest = 1 AND first_access < to_date('" + FormatUtil.formatDate(cutoffDate) +
        "', 'yyyy-mm-dd')", "backup-create-guest-table");
    SqlUtils.executeUpdate(dataSource, "CREATE UNIQUE INDEX " + GUEST_TABLE + "_ix01 ON " + GUEST_TABLE +
        " (user_id)", "create-guest-index");
    return "SELECT '1' FROM " + GUEST_TABLE + " g WHERE g.user_id = t.user_id";
  }

  private void removeGuests(DatabaseInstance userDb, String userSchema, String guestSql) throws SQLException {
    LOG.info("****IN REMOVEGUESTS ******");
    DataSource dataSource = userDb.getDataSource();
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
          + "    WHERE st.userid IS NULL)", SqlUtils.Autocommit.ON);
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
