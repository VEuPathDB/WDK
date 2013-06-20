package org.gusdb.wdk.model.migrate;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dbms.SqlUtils;

/**
 * @author jerric
 * 
 *         1. add columns to steps table: project_id, project_version,
 *         question_name, result_message 2. UPDATE those columns 3. drop
 *         steps_fk02 constraint 4. drop INDEX steps_idx01 through steps_idx08
 *         5. drop steps.answer_id (or drop not null constraint) 6. CREATE
 *         indexes.
 * 
 */
public class Migrator_b18_b19 implements Migrator {

  @Override
  public void declareOptions(Options options) {
    // no additional options are used.
  }

  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine)
      throws WdkModelException {
    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

    addColumns(wdkModel, dataSource);
    addConstraints(wdkModel, dataSource);
    addIndexes(wdkModel, dataSource);
    dropColumns(wdkModel, dataSource);
  }

  private void addColumns(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    ModelConfigUserDB config = wdkModel.getModelConfig().getUserDB();
    String userSchema = config.getUserSchema();
    String wdkSchema = config.getWdkEngineSchema();

    // add project_id, project_version and queston_name columns onto steps
    // table.
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps ADD project_id VARCHAR(50)", "wdk-add-column");
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps ADD project_version VARCHAR(50)", "wdk-add-column");
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps ADD question_name VARCHAR(200)", "wdk-add-column");

    // fill in the values
    SqlUtils.executeUpdate(wdkModel, dataSource,
        "UPDATE (SELECT s.project_id, s.project_version, s.question_name, "
            + "         a.project_id AS a_project_id, "
            + "         a.project_version AS a_project_version, "
            + "         a.question_name AS a_question_name          "
            + "  FROM " + userSchema + "steps s, " + wdkSchema + "answers a "
            + "  WHERE s.answer_id = a.answer_id) "
            + " SET project_id = a_project_id, "
            + "     project_version = a_project_version, "
            + "     question_name = a_question_name ",
        "wdk-filled-step-columns");
  }

  private void addConstraints(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // drop constraints
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP CONSTRAINT steps_answer_id_fk", "wdk-drop-constraint");

    // add constraints
    String alterKeyword = wdkModel.getUserPlatform().getAlterColumnKeyword();
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps " + alterKeyword + " project_id varchar(50) NOT NULL",
        "wdk-add-constraint");
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps " + alterKeyword + " project_version varchar(50) NOT NULL",
        "wdk-add-constraint");
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps " + alterKeyword + " question_name varchar(200) NOT NULL",
        "wdk-add-constraint");

  }

  private void addIndexes(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // drop reference constraint
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP CONSTRAINT steps_unique", "wdk-drop-constraint");

    // drop current indexes
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_unique", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx01", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx02", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx03", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx04", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx05", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx06", "wdk-drop-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
        + "steps_idx07", "wdk-drop-index");

    // add indexes
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE UNIQUE INDEX "
        + userSchema + "steps_idx01 ON " + userSchema
        + "steps (display_id, user_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx02 ON " + userSchema
        + "steps (user_id, left_child_id, right_child_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx03 ON " + userSchema
        + "steps (project_id, project_version, question_name)",
        "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx04 ON " + userSchema + "steps (is_deleted)",
        "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx05 ON " + userSchema + "steps (is_valid)",
        "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx06 ON " + userSchema + "steps (last_run_time)",
        "wdk-create-index");
  }

  private void dropColumns(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // drop unused columns
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP COLUMN answer_id", "wdk-drop-column");
  }
}
