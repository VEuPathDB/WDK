package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

/**
 * Starting build 25 we move here apicomm cleaning activity previously located in GuestRemover and StepValidator
 * - strategies is_deleted = 1
 * - strategies with user_id different from user_id in root_step
 * - strategies with project_id different from project_id in root_step
 * - strategies with root_step inexistent
 * - strategies with user_id inexistent
 * - strategies with a root_step that contains an invalid question_name (this could be done in validate?)
 * - finally remove all steps that do not belong to a strategy
 * @author Jerric
 *
 */
public class RemoveBrokenStratsSteps extends BaseCLI {

  private static final int PAGE_SIZE = 9000;
  private static final Logger LOG = Logger.getLogger(RemoveBrokenStratsSteps.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    RemoveBrokenStratsSteps backup = new RemoveBrokenStratsSteps(cmdName);
    try {
      backup.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      LOG.info("WDK RemoveBrokenStratsSteps done.");
      System.exit(0);
    }
  }


  private WdkModel wdkModel;
  private String userSchema;

  public RemoveBrokenStratsSteps(String command) {
    super((command != null) ? command : "wdkRemoveBroken",
        "This command cleans broken strategies and steps from the user DB");
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
    wdkModel = WdkModel.construct(projectId, gusHome);
    userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    userSchema = DBPlatform.normalizeSchema(userSchema);
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    String defaultSchema = wdkModel.getUserDb().getDefaultSchema();
		String tempBrokenTable = "wdk_broken_strategies";
		String tempUnknownRCTable = "wdk_strats_unknownRC";

/* TO CLEAN/REMOVE:
 * 1- strategies is_deleted = 1
 * 2- broken:
 *   - strategies with user_id different from user_id in root_step
 *   - strategies with project_id different from project_id in root_step
 *   - strategies with root_step inexistent
 *   - strategies with user_id inexistent
 * 3- strategies with a root_step that contains an invalid question _name (this could be done in validate?)
 * 123- finally remove all steps that do not belong to a strategy
 */
		if (platform.checkTableExists(dataSource, defaultSchema, tempBrokenTable)) {
			SqlUtils.executeUpdate(dataSource, "DROP TABLE " + tempBrokenTable, "drop-broken-strats-table.");
		}
		if (platform.checkTableExists(dataSource, defaultSchema, tempUnknownRCTable)) {
			SqlUtils.executeUpdate(dataSource, "DROP TABLE " + tempUnknownRCTable, "drop-unknownRC-strats-table.");
		}

    deleteByBatch(dataSource, userSchema + "strategies", " is_deleted = 1 ");

		//create a new temp table with the strategies to be deleted: wdk_broken_strategies
		SqlUtils.executeUpdate(dataSource, "CREATE TABLE wdk_broken_strategies AS SELECT s.strategy_id FROM " +
				userSchema + "steps st, userlogins5.strategies s WHERE s.root_step_id = st.step_id AND s.user_id != st.user_id", 
						"create-temp-broken-strats-table");
		SqlUtils.executeUpdate(dataSource, "INSERT INTO wdk_broken_strategies (strategy_id) SELECT s.strategy_id FROM " +
        userSchema + "steps st, userlogins5.strategies s WHERE s.root_step_id = st.step_id AND s.project_id != st.project_id", 
						"insert-into-temp-broken-strats-table");
    deleteByBatch(dataSource, userSchema + "strategies", " strategy_id in (select strategy_id from wdk_broken_strategies) ");

		deleteByBatch(dataSource, userSchema + "strategies", " user_id NOT in (select user_id from userlogins5.users) ");
		deleteByBatch(dataSource, userSchema + "strategies", " root_step_id NOT in (select step_id from userlogins5.steps) ");

		SqlUtils.executeUpdate(dataSource, "CREATE TABLE wdk_strats_unknownRC AS SELECT s.strategy_id FROM " +
				userSchema + "steps st, userlogins5.strategies s WHERE s.root_step_id = st.step_id AND st.question_name NOT in " + 
						"(select question_name from wdk_questions)", "create-temp-unknownRC-strats-table");

		// comment out deletion of these strategies when needed... it depends on correct content in wdk_questions local table
			deleteByBatch(dataSource, userSchema + "strategies", " strategy_id in (select strategy_id from wdk_strats_unknownRC) ");


    // after strategies have been cleanedup.. delete unused steps: every deletion will open up more to be deleted.
    boolean remain = true;
    while (remain) {
      int sum = removeUnusedStepAnalysis(dataSource, userSchema);
      try {
        sum = removeUnusedSteps(dataSource, userSchema);
        remain = (sum != 0);
      }
      catch (SQLException ex) {
        LOG.warn(ex.getMessage());
        remain = true;
      }
    }

  }



	// utility also in GuestRemover
  public static int deleteByBatch(DataSource dataSource, String table, String condition) throws SQLException {
    LOG.info("\n\nDeleting from table: " + table + " with condition: " + condition);
    PreparedStatement psDelete = null;
    try {
      String sql = "DELETE FROM " + table + " WHERE " + condition + " AND rownum <= " + PAGE_SIZE;
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


  private int removeUnusedStepAnalysis(DataSource dataSource, String userSchema) throws SQLException {
		LOG.info("\n\nRemoving unused step_analysis...");
    int count = 1, sum = 0;
    String stepTable = userSchema + "steps", strategyTable = userSchema + "strategies";
    while (count != 0) {
      count = RemoveBrokenStratsSteps.deleteByBatch(dataSource, userSchema + "step_analysis", "step_id IN (" +
          "  SELECT step_id              FROM           " + stepTable +
          "  MINUS SELECT root_step_id   FROM           " + strategyTable +
          "  MINUS SELECT left_child_id  FROM           " + stepTable +
          "  MINUS SELECT right_child_id FROM           " + stepTable + ")");
      sum += count;
    }
    LOG.debug(sum + " unused step_analysis deleted");
    return sum;
  }

  private int removeUnusedSteps(DataSource dataSource, String userSchema) throws SQLException {
		LOG.info("\n\nRemoving unused steps...");
    int count = 1, sum = 0;
    String stepTable = userSchema + "steps", strategyTable = userSchema + "strategies";
    while (count != 0) {
      count = RemoveBrokenStratsSteps.deleteByBatch(dataSource, stepTable, "step_id IN (" +
          "  SELECT step_id              FROM           " + stepTable +
          "  MINUS SELECT root_step_id   FROM           " + strategyTable +
          "  MINUS SELECT left_child_id  FROM           " + stepTable +
          "  MINUS SELECT right_child_id FROM           " + stepTable + ")");
      sum += count;
    }
    LOG.debug(sum + " unused steps deleted");
    return sum;
  }

}
