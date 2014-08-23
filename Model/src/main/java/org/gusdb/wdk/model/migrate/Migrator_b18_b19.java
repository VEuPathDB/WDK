package org.gusdb.wdk.model.migrate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.StepFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 * 
 *         1. add columns to steps table: project_id, project_version, question_name, result_message 2. UPDATE
 *         those columns 3. drop steps_fk02 constraint 4. drop INDEX steps_idx01 through steps_idx08 5. drop
 *         steps.answer_id (or drop not null constraint) 6. CREATE indexes.
 * 
 */
public class Migrator_b18_b19 implements Migrator {

  private static final int UPDATE_PAGE = 100;

  private static final String[] LEFT_MAP = { "gene_result", "span_result", "sequence_result",
      "compound_result", "pathway_result", "group_answer", "sequence_answer", "htsIsolateList" };

  private static final Logger logger = Logger.getLogger(Migrator_b18_b19.class);

  @Override
  public void declareOptions(Options options) {}

  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine) throws WdkModelException {
    logger.info("updating step params...");

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    StepFactory stepFactory = wdkModel.getStepFactory();

    ResultSet rsSteps = null;
    PreparedStatement psUpdate = null;
    try {
      psUpdate = SqlUtils.getPreparedStatement(dataSource, "UPDATE " + schema +
          "steps SET display_params = ? WHERE step_id = ?");
      rsSteps = SqlUtils.executeQuery(
          dataSource,
          "SELECT step_id, display_params, left_child_id, right_child_id, question_name " + " FROM " +
              schema + "steps " + " WHERE left_child_id IS NOT NULL  " + "    OR right_child_id IS NOT NULL ",
          "wdk-migrate-select-steps", 5000);
      int count = 0;
      while (rsSteps.next()) {
        // test getting question name, and fail if we are still on old schema
        rsSteps.getString("question_name");

        // get step info
        int stepId = rsSteps.getInt("step_id");
        int leftChildId = rsSteps.getInt("left_child_id");
        int rightChildId = rsSteps.getInt("right_child_id");

        // update params
        String paramContent = platform.getClobData(rsSteps, "display_params");
        Map<String, String> params = stepFactory.parseParamContent(new JSONObject(paramContent));
        updateParams(stepId, params, leftChildId, rightChildId);
        paramContent = stepFactory.getParamContent(params).toString();

        // save changes
        platform.setClobData(psUpdate, 1, paramContent, false);
        psUpdate.setInt(2, stepId);
        psUpdate.addBatch();
        count++;
        if (count % UPDATE_PAGE == 0) {
          psUpdate.executeBatch();
          logger.info(count + " steps updated...");
        }
      }
      if (count % UPDATE_PAGE != 0) {
        psUpdate.executeBatch();
      }
      logger.info("Totally " + count + " steps updated...");
    }
    catch (SQLException | JSONException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsSteps);
      SqlUtils.closeStatement(psUpdate);
    }
  }

  private void updateParams(int stepId, Map<String, String> params, int leftChildId, int rightChildId) {
    String[] names = params.keySet().toArray(new String[0]);
    boolean leftFound = false, rightFound = false;
    for (String name : names) {
      if (name.startsWith("bq_left_op_")) {
        params.put(name, Integer.toString(leftChildId));
        leftFound = true;
      }
      else if (name.startsWith("bq_right_op_")) {
        params.put(name, Integer.toString(rightChildId));
        rightFound = true;
      }
    }
    params.remove("span_a)");
    params.remove("span_b)");
    if (!leftFound && !rightFound) {
      if (rightChildId == 0) { // only left child has value
        for (String name : LEFT_MAP) {
          if (params.containsKey(name)) {
            params.put(name, Integer.toString(leftChildId));
            leftFound = true;
          }
        }
      }
      else { // both left and right child have values
        if (params.containsKey("span_a")) {
          params.put("span_a", Integer.toString(leftChildId));
          leftFound = true;
        }
        if (params.containsKey("span_b")) {
          params.put("span_b", Integer.toString(rightChildId));
          rightFound = true;
        }
      }
    }
    if (!leftFound && !rightFound) {
      System.err.println("Couldn't find step param to update in step: #" + stepId);
    }
  }
}
