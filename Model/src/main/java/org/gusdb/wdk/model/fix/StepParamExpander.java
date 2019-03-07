package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.ParamAndFiltersFormat;
import org.gusdb.wdk.model.fix.table.TableRowUpdater;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         this code extract the param values from steps, and put them into separate row. The result will be
 *         used to expedite the step validation.
 */
public class StepParamExpander extends BaseCLI {

  private static final Logger logger = Logger.getLogger(StepParamExpander.class);

  private static final boolean USE_THREADED_BY_DEFAULT = true;

  private static final String ARG_THREADED = "threaded";
  private static final String ARG_DROP = "dropTablesOnly";
  static final String STEP_PARAMS_TABLE = "step_params";
  private static final String UPDATED_STEPS_TABLE = "wdk_updated_steps";

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    StepParamExpander expender = new StepParamExpander(cmdName);
    try {
      expender.invoke(args);
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * @param command
   * @param description
   */
  protected StepParamExpander(String command) {
    super((command != null) ? command : "stepParamExpander",
        "expand the param clob into its own rows in " + STEP_PARAMS_TABLE + " table");
  }

  public void expand(WdkModel wdkModel) throws SQLException, WdkModelException {
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource ds = database.getDataSource();
    Connection connection = null;
    Statement selectStmt = null;
    ResultSet resultSet = null;
    PreparedStatement psInsert = null;
    try {
      Timer timer = new Timer();
      logger.info("Preparing database tables...");
      createParamTable(wdkModel);
      logger.info("Database table preparation took " + timer.getElapsedString());
      logger.info("Expanding params...");
      connection = ds.getConnection();
      connection.setAutoCommit(false);

      String selectSql = getSelectSql(userSchema, wdkModel.getProjectId());

      selectStmt = connection.createStatement();
      selectStmt.setFetchSize(1000);

      logger.info("Executing select steps query...");
      long start = System.currentTimeMillis();
      resultSet = selectStmt.executeQuery(selectSql);
      QueryLogger.logEndStatementExecution(selectSql, "wdk-select-step-params", start);
      psInsert = connection.prepareStatement(getInsertSql());

      int stepCount = 0;
      int stepsWithParamsCount = 0;
      int noParamStepCount = 0;
      int batchedParamValueRows = 0;
      int totalParamValueRows = 0;
      int numBatchesWritten = 0;

      logger.info("Processing step rows...");
      while (resultSet.next()) {

        stepCount++;
        int stepId = resultSet.getInt("step_id");
        String clob = database.getPlatform().getClobData(resultSet, "display_params");
        Map<String, Set<String>> values = parseClob(stepId, clob);
        if (stepCount % 1000 == 0) {
          logger.info(stepCount + " steps read.");
        }
        if (values.isEmpty()) {
          noParamStepCount++;
          continue;
        }
        stepsWithParamsCount++;

        // insert the values
        for (String paramName : values.keySet()) {
          Set<String> paramValues = values.get(paramName);
          for (String paramValue : paramValues) {
            psInsert.setInt(1, stepId);
            psInsert.setString(2, paramName);
            psInsert.setString(3, paramValue);
            psInsert.addBatch();
            batchedParamValueRows++;
          }
        }
        if (batchedParamValueRows > 200) {
          numBatchesWritten++;
          totalParamValueRows += batchedParamValueRows;
          logger.info("Committing batch #" + numBatchesWritten + " of " + batchedParamValueRows + " param values (" + totalParamValueRows + " total).");
          psInsert.executeBatch();
          connection.commit();
          batchedParamValueRows = 0;
        }
      }
      if (batchedParamValueRows > 0) {
        numBatchesWritten++;
        totalParamValueRows += batchedParamValueRows;
        logger.info("Committing batch #" + numBatchesWritten + " of " + batchedParamValueRows + " param values (" + totalParamValueRows + " total).");
        psInsert.executeBatch();
        connection.commit();
      }

      logger.info("Processed " + stepCount + " steps in " + timer.getElapsedString());
      logger.info(noParamStepCount + " steps had no parameters.");
      logger.info(stepsWithParamsCount + " steps had parameters.");
      int avgBatchSize = (numBatchesWritten == 0 ? 0 : totalParamValueRows / numBatchesWritten);
      logger.info(totalParamValueRows + " parameter rows were inserted over " + numBatchesWritten +
          " batches (avg batch size " + avgBatchSize + ").");
    }
    catch (SQLException | JSONException ex) {
      connection.rollback();
      throw new WdkModelException(ex);
    }
    finally {
      if (connection != null)
        connection.setAutoCommit(true);
      SqlUtils.closeQuietly(psInsert, resultSet, selectStmt, connection);
    }
  }

  private static String getSelectSql(String userSchema, String projectId) {
    return getSelectForColumns(userSchema, projectId, "step_id,display_params");
  }

  public static String getSelectForColumns(String userSchema, String projectId, String columns) {
    String projectIdCondition = (projectId != null ? " AND s.project_id = '" + projectId + "'" : "");
    return
        "SELECT " + columns +
        " FROM " + userSchema + "steps s, " + userSchema + "users u" +
        " WHERE s.user_id = u.user_id AND u.is_guest = 0 AND s.is_deleted = 0" + projectIdCondition +
        "   AND s.step_id NOT IN (SELECT step_id FROM " + STEP_PARAMS_TABLE + ")";
  }

  public static void createParamTable(WdkModel wdkModel) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();

    // check if table exists
    // in that case check if any steps have been updated (in wdk_update_steps) and remove them
    if (database.getPlatform().checkTableExists(dataSource, database.getDefaultSchema(), STEP_PARAMS_TABLE)) {
      
      // if wdk_updated_steps table exists, purge updated steps from step_params
      if (database.getPlatform().checkTableExists(dataSource, database.getDefaultSchema(), UPDATED_STEPS_TABLE)) {
        // 1- delete FROM step_params WHERE step_id IN (SELECT step_id FROM wdk_updated_steps)
        logger.info("Deleting step params that have been updated...");
        SqlUtils.executeUpdate(dataSource,
            "delete FROM " + STEP_PARAMS_TABLE + " WHERE step_id IN (SELECT step_id FROM " + UPDATED_STEPS_TABLE + ")",
            "wdk-update-param-table");
        // 2- clean wdk_update_steps
        logger.info("Leave " + UPDATED_STEPS_TABLE + " table empty for next use...");
        SqlUtils.executeUpdate(dataSource, "delete from " + UPDATED_STEPS_TABLE, "wdk-reset-updatedSteps-table");
      }
      else {
        logger.info(UPDATED_STEPS_TABLE + " table does not exist; will not remove any updated step params");
      }
    }
    else {
      logger.info("Creating " + STEP_PARAMS_TABLE + " table...");

      SqlUtils.executeUpdate(dataSource,
          "CREATE TABLE " + STEP_PARAMS_TABLE + " (" +
              " step_id NUMBER(12) NOT NULL, " +
              " param_name VARCHAR(200 CHAR) NOT NULL, " +
              " param_value VARCHAR(4000 CHAR), " +
              " migration NUMBER(12))",
          "wdk-create-param-table");
  
      SqlUtils.executeUpdate(dataSource,
          "CREATE UNIQUE INDEX step_params_ux01 " + "ON " + STEP_PARAMS_TABLE + " (step_id, param_name, param_value)",
          "wdk-create-param-indx");
  
      SqlUtils.executeUpdate(dataSource,
          "CREATE INDEX step_params_ix01 " + "ON " + STEP_PARAMS_TABLE + " (step_id)",
          "wdk-create-param-indx");
    }
  }

  public static String getInsertSql() {
    return "INSERT INTO " + STEP_PARAMS_TABLE + " (step_id, param_name, param_value) VALUES (?, ?, ?)";
  }

  public static Map<String, Set<String>> parseClob(int stepId, String clob)
      throws JSONException {
    if (clob == null || clob.trim().isEmpty()) {
      return new LinkedHashMap<>();
    }
    // parse JSON object out of clob
    try {
      return parseDisplayParams(stepId, new JSONObject(clob.trim()));
    }
    catch (JSONException e) {
      logger.warn("Step " + stepId + ": display_params CLOB is not a JSON object.");
      return new LinkedHashMap<>();
    }
  }

  public static Map<String, Set<String>> parseDisplayParams(int stepId, JSONObject displayParams) {
    return parseParams(stepId, displayParams.has(ParamAndFiltersFormat.KEY_PARAMS) ?
        // new displayParams format, fetch params object from params property
        displayParams.getJSONObject(ParamAndFiltersFormat.KEY_PARAMS) :
        // old format, entire object is the params
        displayParams);
  }

  private static Map<String, Set<String>> parseParams(int stepId, JSONObject params) {
    Map<String, Set<String>> newValues = new LinkedHashMap<>();
    for (Entry<String, JsonType> paramEntry : JsonIterators.objectIterable(params)) {
      String paramName = paramEntry.getKey().trim();
      JsonType rawValue = paramEntry.getValue();
      if (!rawValue.getType().equals(ValueType.STRING)) {
        logger.warn("Step " + stepId + ": Param [" + paramName + "] has value " +
            rawValue.toString() + " which is not a string.");
        continue;
      }
      String value = rawValue.getString();
       String[] terms = value.split(",");
      Set<String> termSet = new HashSet<>();
      for (String term : terms) {
        termSet.add(truncateTerm(term));
      }
      newValues.put(paramName, termSet);
    }
    return newValues;
  }

  public static String truncateTerm(String term) {
    return (term.length() <= 4000 ? term : term.substring(0, 4000));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "ProjectId, which" +
        " should match the directory name under $GUS_HOME, where" + " model-config.xml is stored.");
    addSingleValueOption(ARG_THREADED, false, String.valueOf(USE_THREADED_BY_DEFAULT), "Set to true to use TableRowUpdater (threaded) version; else false");
    addNonValueOption(ARG_DROP, false, "Do not expand steps.  Just drop the " + STEP_PARAMS_TABLE + " and " + UPDATED_STEPS_TABLE + " tables, then exit.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {
    String projectId = (String)getOptionValue(ARG_PROJECT_ID);

    if ((Boolean) getOptionValue(ARG_DROP)) {
      try (WdkModel wdkModel = WdkModel.construct(projectId, GusHome.getGusHome())) {
        dropTables(wdkModel);
      }
      System.exit(0);
    }

    boolean useThreaded = Boolean.valueOf((String)getOptionValue(ARG_THREADED));
    System.exit(useThreaded ?
        TableRowUpdater.run(new String[]{ StepParamExpanderPlugin.class.getName(), projectId }) :
        runOrig(projectId));
  }

  private int runOrig(String projectId) {
    Timer t = new Timer();
    try (WdkModel wdkModel = WdkModel.construct(projectId, GusHome.getGusHome())) {
      expand(wdkModel);
      return 0;
    }
    catch(Exception e) {
      logger.error("Error expanding params", e);
      return 1;
    }
    finally {
      logger.info("Program duration: " + t.getElapsedString());
    }
  }

  private static void dropTables(WdkModel wdkModel) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();
    DBPlatform platform = database.getPlatform();
    String schema = database.getDefaultSchema();
    String[] tables = new String[] { STEP_PARAMS_TABLE, UPDATED_STEPS_TABLE };
    for (String table : tables) {
      if (platform.checkTableExists(dataSource, schema, table)) {
        SqlUtils.executeUpdate(dataSource, "DROP TABLE " + schema + table, "wdk-drop-table");
      }
    }
  }


}
