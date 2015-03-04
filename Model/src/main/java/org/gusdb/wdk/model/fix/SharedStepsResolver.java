package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.StepFactory;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 */
public class SharedStepsResolver extends BaseCLI {

  private static final String TEMP_STEP_TABLE = "tmp_steps";

  private static final Logger LOG = Logger.getLogger(SharedStepsResolver.class);

  private static class StepInfo {
    int oldId;
    int newId;
    int leftId;
    int rightId;
    String content;
  }

  public static void main(String[] args) throws Exception {
    LOG.info("***** Starting SharedStepsResolver ******");
    String cmdName = System.getProperty("cmdName");
    SharedStepsResolver updater = new SharedStepsResolver(cmdName);
    try {
      updater.invoke(args);
    }
    catch (RuntimeException ex) {
      ex.printStackTrace();
      throw ex;
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      LOG.info("SharedStepsResolver done.");
      System.exit(0);
    }
  }

  /**
   * @param command
   * @param description
   */
  public SharedStepsResolver(String command) {
    super((command != null) ? command : "wdkSharedStepsResolver",
        "Resolve shared steps and create copies to make sure steps are not shared. It also deletes the"
            + " unused resources, such as deleted strategies, or steps there are not referenced by"
            + " strategies or other steps, etc.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "A project Id, which should match the directory name"
        + " under $GUS_HOME/config/, where model-config.xml is stored.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {

    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    userSchema = DBPlatform.normalizeSchema(userSchema);
    DataSource dataSource = wdkModel.getUserDb().getDataSource();

    // set up the temp table
    try {
      SqlUtils.executeUpdate(dataSource, "CREATE TABLE " + TEMP_STEP_TABLE + " AS SELECT * FROM " +
          userSchema + "steps WHERE rownum = 1", "create-tmp-steps");
    }
    catch (SQLException ex) {}
    SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TEMP_STEP_TABLE, "delete-tmp-steps");

    mapSharedRootSteps(wdkModel, dataSource, userSchema);
    mapSharedChildSteps(wdkModel, dataSource, userSchema, true);
    mapSharedChildSteps(wdkModel, dataSource, userSchema, false);

    SqlUtils.executeUpdate(dataSource, "DROP TABLE " + TEMP_STEP_TABLE, "drop-tmp-steps");
  }

  private void mapSharedRootSteps(WdkModel wdkModel, DataSource dataSource, String userSchema)
      throws WdkModelException {
    String strategyTable = userSchema + "strategies";
    String sqlSelect = "SELECT strategy_id, root_step_id FROM " + strategyTable +
        "  WHERE root_step_id IN (SELECT root_step_id FROM " + strategyTable +
        "                         GROUP BY root_step_id HAVING count(*) > 1)" +
        "  ORDER BY root_step_id ASC, strategy_id ASC";
    String sqlUpdate = "UPDATE " + strategyTable + " SET root_step_id = ? WHERE strategy_id = ?";
    ResultSet resultSet = null;
    PreparedStatement psUpdate = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sqlSelect, "wdk-get-duplicate-root-steps", 5000);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sqlUpdate);
      int previousStep = 0, count = 0;
      while (resultSet.next()) {
        int strategyId = resultSet.getInt("strategy_id");
        int stepId = resultSet.getInt("root_step_id");
        if (previousStep != stepId) { // found a new duplicated step, skip the first one
          previousStep = stepId;
        }
        else { // found the 2+ duplicate step, deep clone the step
          stepId = cloneStep(wdkModel, dataSource, userSchema, stepId);
          psUpdate.setInt(1, stepId);
          psUpdate.setInt(2, strategyId);
          psUpdate.addBatch();
          count++;
          if (count % 1000 == 0)
            psUpdate.executeBatch();
        }
        if (count % 1000 != 0)
          psUpdate.executeBatch();
      }
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      SqlUtils.closeStatement(psUpdate);
    }
  }

  private void mapSharedChildSteps(WdkModel wdkModel, DataSource dataSource, String userSchema, boolean left)
      throws WdkModelException {
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    String column = left ? "left_child_id" : "right_child_id";
    String stepTable = userSchema + "steps";
    String sqlSelect = "SELECT step_id, " + column + ", display_params FROM            " + stepTable +
        "  WHERE " + column + " IN (SELECT " + column + " FROM " + stepTable +
        "                           WHERE " + column + " IS NOT NULL " +
        "                           GROUP BY " + column + " HAVING count(*) > 1)            " +
        "  ORDER BY " + column + " ASC, step_id ASC";
    String sqlUpdate = "UPDATE " + stepTable + " SET " + column + " = ?, display_Params = ? " +
        "  WHERE step_id = ?";
    ResultSet resultSet = null;
    PreparedStatement psUpdate = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sqlSelect, "wdk-get-duplicate-root-steps", 5000);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sqlUpdate);
      int previousStep = 0, count = 0;
      while (resultSet.next()) {
        int stepId = resultSet.getInt("step_id");
        int childId = resultSet.getInt(column);
        if (previousStep != childId) { // found a new duplicated child, skip the first one
          previousStep = childId;
        }
        else { // found the 2+ duplicate step, deep clone the step
          String content = platform.getClobData(resultSet, "display_params");
          int newId = cloneStep(wdkModel, dataSource, userSchema, childId);
          Map<Integer, Integer> ids = new HashMap<>();
          ids.put(childId, newId);
          content = updateContent(content, ids);

          psUpdate.setInt(1, newId);
          platform.setClobData(psUpdate, 2, content, false);
          psUpdate.setInt(3, stepId);
          psUpdate.addBatch();
          count++;
          if (count % 1000 == 0)
            psUpdate.executeBatch();
        }
        if (count % 1000 != 0)
          psUpdate.executeBatch();
      }
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      SqlUtils.closeStatement(psUpdate);
    }
  }

  private int cloneStep(WdkModel wdkModel, DataSource dataSource, String userSchema, int stepId)
      throws SQLException, WdkModelException {
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    SqlUtils.executeUpdate(dataSource, "INSERT INTO                  " + TEMP_STEP_TABLE +
        "  SELECT * FROM " + userSchema + "steps START WITH step_id = " + stepId +
        "  CONNECT BY PRIOR left_child_id = step_id OR PRIOR right_child_id = step_id", "insert-tmp-steps");
    ResultSet resultSet = null;
    PreparedStatement psUpdate = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource,
          "SELECT step_id, left_child_id, right_child_id, display_params FROM " + TEMP_STEP_TABLE,
          "select-tmp-steps", 100);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, "UPDATE " + TEMP_STEP_TABLE +
          " SET step_id = ?, left_child_id = ?, right_child_id = ?, display_params = ? WHERE step_id = ?");
      Map<Integer, StepInfo> steps = new HashMap<>();
      while (resultSet.next()) {
        StepInfo step = new StepInfo();
        step.oldId = resultSet.getInt("step_id");
        Object left = resultSet.getObject("left_child_id");
        if (left != null)
          step.leftId = Integer.valueOf(left.toString());
        Object right = resultSet.getObject("right_child_id");
        if (right != null)
          step.rightId = Integer.valueOf(right.toString());
        step.content = platform.getClobData(resultSet, "display_params");
        steps.put(step.oldId, step);
      }

      int newId = cloneStep(platform, dataSource, psUpdate, userSchema, stepId, steps);
      psUpdate.executeBatch();

      // then copy back the new steps
      SqlUtils.executeUpdate(dataSource, "INSERT INTO " + userSchema + "steps SELECT * FROM " +
          TEMP_STEP_TABLE, "copy-tmp-steps");

      return newId;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      SqlUtils.closeStatement(psUpdate);
      SqlUtils.executeUpdate(dataSource, "DELETE FROM " + TEMP_STEP_TABLE, "delete-tmp-steps");
    }
  }

  private int cloneStep(DBPlatform platform, DataSource dataSource, PreparedStatement psUpdate,
      String userSchema, int stepId, Map<Integer, StepInfo> steps) throws SQLException, WdkModelException {
    StepInfo step = steps.get(stepId);
    if (step.newId != 0)
      return step.newId;
    // check if the step's children has been cloned
    Map<Integer, Integer> ids = new HashMap<>();
    if (step.leftId != 0) {
      int newId = cloneStep(platform, dataSource, psUpdate, userSchema, step.leftId, steps);
      ids.put(step.leftId, newId);
      step.leftId = newId;
    }
    if (step.rightId != 0) {
      int newId = cloneStep(platform, dataSource, psUpdate, userSchema, step.rightId, steps);
      ids.put(step.rightId, newId);
      step.rightId = newId;
    }
    // update the param content
    step.content = updateContent(step.content, ids);
    // get a new step id
    try {
      step.newId = platform.getNextId(dataSource, userSchema, "steps");
    }
    catch (DBStateException ex) {
      throw new WdkModelException(ex);
    }
    // update the step
    psUpdate.setInt(1, step.newId);
    psUpdate.setObject(2, (step.leftId != 0) ? step.leftId : null);
    psUpdate.setObject(3, (step.rightId != 0) ? step.rightId : null);
    platform.setClobData(psUpdate, 4, step.content, false);
    psUpdate.setInt(5, stepId);
    psUpdate.addBatch();
    return step.newId;
  }

  private String updateContent(String content, Map<Integer, Integer> ids) throws WdkModelException {
    JSONObject jsContent = new JSONObject(content);
    Map<String, String> params = StepFactory.parseParamContent(jsContent);

    // update param values
    String[] paramNames = params.keySet().toArray(new String[0]);
    for (int oldId : ids.keySet()) {
      String old = Integer.toString(oldId);
      for (String paramName : paramNames) {
        String value = params.get(paramName);
        if (value.equals(old))
          params.put(paramName, Integer.toString(ids.get(oldId)));
      }
    }
    // update JSON
    JSONObject jsParams = new JSONObject();
    for (String paramName : params.keySet()) {
      jsParams.put(paramName, params.get(paramName));
    }
    jsContent.put("params", jsParams);

    return jsContent.toString();
  }
}
