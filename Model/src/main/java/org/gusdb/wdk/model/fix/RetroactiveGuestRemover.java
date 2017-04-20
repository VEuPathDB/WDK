package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 *  This is intended as a one-off program to rid bloated user dbs of guest users until only 2 weeks of
 *  guest users remain.  The program removes 2 weeks of guest users at a time and is to be run regularly
 *  via Jenkins until only the last 2 weeks of guest users remain.
 */
public class RetroactiveGuestRemover extends BaseCLI {

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
      LOG.info("WDK User Remover done.");
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  /**
   * Obtains the registry date for the oldest wdk guest user and adds two weeks to it to
   * obtain the cutoff date.  If the cutoff date is inside the last two weeks, return a 
   * cutoff date of two weeks prior to the current date instead.
   * @param dataSource
   * @param userSchema
   * @return
   * @throws WdkModelException
   */
  public static String deriveCutoffDate(DataSource dataSource, String userSchema) throws WdkModelException {
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	Calendar twoWeeksPriorToNowCal = Calendar.getInstance();
	twoWeeksPriorToNowCal.add(Calendar.DATE, -14);
	Calendar cutoffCal = findOldestGuestUserRegistry(dataSource, userSchema);
	cutoffCal.add(Calendar.DATE, 14);
    if(cutoffCal.getTimeInMillis() >= twoWeeksPriorToNowCal.getTimeInMillis()) {
      return formatter.format(twoWeeksPriorToNowCal);
    }
    return formatter.format(cutoffCal.getTime());
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
    final Calendar calendar = Calendar.getInstance();
	String sql = "SELECT min(register_time) AS oldest_date FROM " + userSchema + "users WHERE is_guest = 1";
    try {
      new SQLRunner(dataSource, sql).executeQuery(new ResultSetHandler() {
        @Override
        public void handleResult(ResultSet resultSet) throws SQLException {
          try { 
            if(resultSet.next()) {
              calendar.setTime(resultSet.getTimestamp("oldest_date"));
            }
          }
          catch(SQLException sre) {
            throw new SQLRunnerException("Unable to oldest guest user registry.");
          }
        }  
      }); 
    }
    catch(SQLRunnerException se) {
      throw new WdkModelException("Unable to oldest guest user registry.", se.getCause());
    }
    return calendar;
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

  public RetroactiveGuestRemover(String command) {
    super((command != null) ? command : "wdkGuestRemover",
        "This command removes expired guest user data from user DB.");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");
  }

  @Override
  protected void execute() throws Exception {
    LOG.info("****IN EXECUTE******");

    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);

    try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {
      DatabaseInstance userDb = wdkModel.getUserDb();
      String userSchema = DBPlatform.normalizeSchema(wdkModel.getModelConfig().getUserDB().getUserSchema());
      String cutoffDate = deriveCutoffDate(userDb.getDataSource(), userSchema);
      LOG.info("********** Cutoff Date: " + cutoffDate + " **********");
      if(false) {
  
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
  }

  /**
   * @param userSchema
   * @param cutoffDate
   * @return a sql that returns the guests to be removed;
   * @throws SQLException
   * @throws DBStateException
   */
  private String lookupGuests(DatabaseInstance userDb, String userSchema, String cutoffDate) throws DBStateException, SQLException {
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
        userSchema + "users " + " WHERE is_guest = 1 AND register_time < to_date('" + cutoffDate +
        "', 'yyyy/mm/dd')", "backup-create-guest-table");
    SqlUtils.executeUpdate(dataSource, "CREATE UNIQUE INDEX " + GUEST_TABLE + "_ix01 ON " + GUEST_TABLE +
        " (user_id)", "create-guest-index");
    //return "SELECT user_id FROM " + GUEST_TABLE;
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
