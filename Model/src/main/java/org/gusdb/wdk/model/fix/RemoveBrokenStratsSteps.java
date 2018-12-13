package org.gusdb.wdk.model.fix;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkRuntimeException;

public class RemoveBrokenStratsSteps extends BaseCLI {
  private static final int PAGE_SIZE = 9000;
  private static final Logger LOG = Logger.getLogger(RemoveBrokenStratsSteps.class);
  protected static final String ARG_REPORT_ONLY = "reportOnly";
  protected static final String SQL_WRONG_USER = "wrong_user";
  protected static final String SQL_WRONG_PROJECT = "wrong_project";
  protected static final String SQL_UNKNOWN_QUESTION = "unknown_question";
  protected static final String SQL_ORPHAN_STEPS = "orphaned_steps";
  protected static final String SQL_ORPHAN_ANALYSIS = "orphaned_analysis";


  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    RemoveBrokenStratsSteps backup = new RemoveBrokenStratsSteps(cmdName);
    try {
      backup.invoke(args);
      LOG.info("WDK RemoveBrokenStratsSteps done.");
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public RemoveBrokenStratsSteps(String command) {
    super((command != null) ? command : "wdkRemoveBroken",
        "This command cleans broken strategies and steps from the user DB");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");
    addNonValueOption(ARG_REPORT_ONLY, false, "Do not remove broken.  Just print report on broken steps and strategies");
  }

  @Override
  protected void execute() throws Exception {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);

    try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {
      String userSchema = DBPlatform.normalizeSchema(wdkModel.getModelConfig().getUserDB().getUserSchema());
      Map<String,String> sqlFroms = getSqlFroms(userSchema);
      if ((Boolean) getOptionValue(ARG_REPORT_ONLY)) {
	LOG.info("Remove Broken Steps: Reporting only! Not removing broken steps.");

        reportBroken(wdkModel, sqlFroms);
      } else {
	LOG.info("Remove Broken Steps: removing broken steps.");
	removeBroken(wdkModel, sqlFroms, userSchema);
      }
    }
  }
  
  private Map<String, String> getSqlFroms(String userSchema) {
    Map<String,String> selects = new LinkedHashMap<String,String>();
 
    String s = "FROM " +
        userSchema + "steps st, " + userSchema + "strategies s WHERE s.root_step_id = st.step_id AND s.user_id != st.user_id";
    selects.put(SQL_WRONG_USER, s);
    
    s = "FROM " +
        userSchema + "steps st, " + userSchema + "strategies s WHERE s.root_step_id = st.step_id AND s.project_id != st.project_id";
    selects.put(SQL_WRONG_PROJECT, s);

    s = "FROM " +
        userSchema + "steps st, " + userSchema + "strategies s WHERE s.root_step_id = st.step_id AND st.question_name NOT in " + 
        "(select question_name from wdk_questions)";
    selects.put(SQL_UNKNOWN_QUESTION, s);
    
    String stepTable = userSchema + "steps", strategyTable = userSchema + "strategies";
    String analysisTable = userSchema + "step_analysis";
    Date today = new Date(new java.util.Date().getTime());
    String condition = "step_id IN (" + "  select * from (" +
        "  SELECT step_id              FROM           " + stepTable + 
        "     WHERE create_time < ( to_date('" + today + "','yyyy-mm-dd') - 1)" +   // step has to be 24+ hours old to be an orphan, for safety
        "  MINUS SELECT root_step_id   FROM           " + strategyTable +
        "  MINUS SELECT left_child_id  FROM           " + stepTable +
        "  MINUS SELECT right_child_id FROM           " + stepTable +
        ") where rownum <= " + PAGE_SIZE + "  )";
    
    s = "FROM " + analysisTable + " WHERE " + condition;
    
    selects.put(SQL_ORPHAN_ANALYSIS, s);
    
    s = "FROM " + stepTable + " WHERE " + condition;

    selects.put(SQL_ORPHAN_STEPS, s);
    
    return selects;
  }
  
  private void reportBroken(WdkModel wdkModel, Map<String,String> sqlFroms) {
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    for (String queryName : sqlFroms.keySet()) {
      String sql = "select count(*) " + sqlFroms.get(queryName);
      long count = new SQLRunner(dataSource, sql, "report-broken-" + queryName)
          .executeQuery(new SingleLongResultSetHandler())
          .orElseThrow(() -> new WdkRuntimeException("Count query returned no rows."));
      System.out.println(queryName + ": " + count);
    }
  }
  
  private void removeBroken(WdkModel wdkModel, Map<String,String> sqlFroms, String userSchema) throws Exception {
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    String defaultSchema = wdkModel.getUserDb().getDefaultSchema();
    String tempBrokenTable = "wdk_broken_strategies";
    String tempUnknownRCTable = "wdk_strats_unknownRC"; //unknown record class because invalid question name in root step

    /* TO CLEAN/REMOVE:
     * 1- strategies is_deleted = 1
     * 2- broken:
     *   - strategies with user_id different from user_id in root_step
     *   - strategies with project_id different from project_id in root_step
     *   - strategies with root_step inexistent
     *   - strategies with user_id inexistent
     * 3- strategies with a root_step that contains an invalid question _name (this could be done in validate, options in redmine #19239)
     * 123- finally remove all steps that do not belong to a strategy
     */
    if (platform.checkTableExists(dataSource, defaultSchema, tempBrokenTable)) {
      SqlUtils.executeUpdate(dataSource, "DROP TABLE " + tempBrokenTable, "drop-broken-strats-table.");
    }
    if (platform.checkTableExists(dataSource, defaultSchema, tempUnknownRCTable)) {
      SqlUtils.executeUpdate(dataSource, "DROP TABLE " + tempUnknownRCTable, "drop-unknownRC-strats-table.");
    }

    // 1
    GuestRemover.deleteByBatch(dataSource, userSchema + "strategies", " is_deleted = 1 ");

    // 2
    SqlUtils.executeUpdate(dataSource, "CREATE TABLE wdk_broken_strategies AS SELECT s.strategy_id " + sqlFroms.get(SQL_WRONG_USER), 
        "create-temp-broken-strats-table");
    
    SqlUtils.executeUpdate(dataSource, "INSERT INTO wdk_broken_strategies (strategy_id) AS SELECT s.strategy_id " + sqlFroms.get(SQL_WRONG_PROJECT), 
        "insert-into-temp-broken-strats-table");
    
    GuestRemover.deleteByBatch(dataSource, userSchema + "strategies", " strategy_id in (select strategy_id from wdk_broken_strategies) ");

    GuestRemover.deleteByBatch(dataSource, userSchema + "strategies", " user_id NOT in (select user_id from userlogins5.users) "); // deleted 2
    GuestRemover.deleteByBatch(dataSource, userSchema + "strategies", " root_step_id NOT in (select step_id from userlogins5.steps) "); // deleted 48

    deleteStepsAndAnalyses(dataSource, sqlFroms);
    
  }

  private void deleteStepsAndAnalyses(DataSource dataSource, Map<String,String> sqlFroms) throws SQLException {
    
    // for one page of the current set of orphaned steps, first delete their analyses, then delete them.
    // this leads to a new set of orphane steps.  continue until none remain.
    String analysisSql = "DELETE " + sqlFroms.get(SQL_ORPHAN_ANALYSIS);
    String stepSql = "DELETE " + sqlFroms.get(SQL_ORPHAN_STEPS);
    
    PreparedStatement psDeleteAnalyses = null;
    PreparedStatement psDeleteSteps = null;
    LOG.debug("\n" + stepSql + "\n");
    try {
      psDeleteAnalyses = SqlUtils.getPreparedStatement(dataSource, analysisSql);
      psDeleteSteps = SqlUtils.getPreparedStatement(dataSource, stepSql);
      int countAnalysisDelete = 0;
      int countStepsDelete = 0;
      int pageCount;
      do {
        // executeUpdate includes the commit
        countAnalysisDelete += psDeleteAnalyses.executeUpdate();
        pageCount = psDeleteSteps.executeUpdate();
        countStepsDelete += pageCount;
      }
      while (pageCount != 0);
      LOG.debug(" Deleted " + countAnalysisDelete + " step analysis rows.");
      LOG.debug(" Deleted " + countStepsDelete + " step step rows.");
     
    } finally {
      SqlUtils.closeStatement(psDeleteAnalyses);
      SqlUtils.closeStatement(psDeleteSteps);
    } 
  }
}
