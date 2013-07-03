package org.gusdb.wdk.model.migrate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
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

  private Logger logger = Logger.getLogger(Migrator_b18_b19.class);

  @Override
  public void declareOptions(Options options) {
    // no additional options are used.
  }

  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine)
      throws WdkModelException {
    DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

    // addColumns(wdkModel, dataSource);
    fixDisplayIds(wdkModel, dataSource);
    // fixStrategyIds(wdkModel, dataSource);
    addConstraints(wdkModel, dataSource);
    addIndexes(wdkModel, dataSource);
    // dropColumns(wdkModel, dataSource);
  }

  private void addColumns(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    logger.debug("adding columns...");

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
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps ADD strategy_id NUMBER(12)", "wdk-add-column");

    // fill in the project_id and question names
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
    logger.debug("adding constraints...");

    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    Set<String> stepConstraints = getConstraints(wdkModel, dataSource,
        userSchema, "steps");
    // drop constraints
    if (stepConstraints.contains("steps_answer_id_fk"))
      SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
          + "steps DROP CONSTRAINT steps_answer_id_fk", "wdk-drop-constraint");

    Set<String> strategyConstraints = getConstraints(wdkModel, dataSource,
        userSchema, "strategies");
    if (strategyConstraints.contains("strategies_unique"))
      SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
          + "strategies DROP CONSTRAINT strategies_unique",
          "wdk-drop-constraint");
    if (strategyConstraints.contains("strategies_user_id_fk"))
      SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
          + "strategies DROP CONSTRAINT strategies_user_id_fk",
          "wdk-drop-constraint");

    // add constraints
    // SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
    // + "steps " + alterKeyword + " project_id varchar(50) NOT NULL",
    // "wdk-add-constraint");
    // SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
    // + "steps " + alterKeyword + " project_version varchar(50) NOT NULL",
    // "wdk-add-constraint");
    // SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
    // + "steps " + alterKeyword + " question_name varchar(200) NOT NULL",
    // "wdk-add-constraint");

    // cannot add fk constraint, since some of the ids cannot be mapped.
    // SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
    // + "strategies " + alterKeyword + " ADD CONSTRAINT strategies_fk01 "
    // + " FOREIGN KEY (root_step_id) REFERENCES " + userSchema
    // + "steps (step_id)", "wdk-add-constraint");
    SqlUtils.executeUpdate(wdkModel, dataSource,
        "ALTER TABLE " + userSchema + "strategies"
            + " ADD CONSTRAINT strategies_fk02 "
            + " FOREIGN KEY (user_id) REFERENCES " + userSchema
            + "users (user_id)", "wdk-add-constraint");
  }

  private Set<String> getConstraints(WdkModel wdkModel, DataSource dataSource,
      String schema, String table) throws WdkModelException {
    if (schema.endsWith("."))
      schema = schema.substring(0, schema.length() - 1);

    Set<String> constraintNames = new HashSet<>();
    ResultSet resultSet = null;
    try {
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
          "SELECT constraint_name FROM all_constraints "
              + "WHERE owner = ? AND table_name = ?");
      ps.setString(1, schema.toUpperCase());
      ps.setString(2, table.toUpperCase());
      resultSet = ps.executeQuery();
      while (resultSet.next()) {
        constraintNames.add(resultSet.getString("constraint_name").toLowerCase());
      }
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return constraintNames;
  }

  private void addIndexes(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    logger.debug("adding indexes...");

    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // drop reference constraint
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP CONSTRAINT steps_unique", "wdk-drop-constraint");

    // drop indexes from steps
    Set<String> stepIndexes = getIndexes(wdkModel, dataSource, userSchema,
        "steps");
    for (String index : stepIndexes) {
      if (index.endsWith("_pk")) continue;
      SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
          + index, "wdk-drop-index");
    }

    // add indexes
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx01 ON " + userSchema + "steps "
        + "(user_id, left_child_id, right_child_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx02 ON " + userSchema + "steps "
        + "(project_id, question_name, user_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx03 ON " + userSchema + "steps "
        + "(is_deleted, user_id, project_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx04 ON " + userSchema + "steps "
        + "(is_valid, user_id, project_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "steps_idx05 ON " + userSchema + "steps "
        + "(last_run_time, user_id, project_id)", "wdk-create-index");

    // drop indexes from strategies
    Set<String> strategiesIndexes = getIndexes(wdkModel, dataSource,
        userSchema, "strategies");
    for (String index : strategiesIndexes) {
      if (index.endsWith("_pk")) continue;
      SqlUtils.executeUpdate(wdkModel, dataSource, "DROP INDEX " + userSchema
          + index, "wdk-drop-index");
    }

    // add indexes to strategies table
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "strategies_idx01 ON " + userSchema + "strategies "
        + "(signature, project_id)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "strategies_idx02 ON " + userSchema + "strategies "
        + "(user_id, project_id, is_deleted, is_saved)", "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "strategies_idx03 ON " + userSchema + "strategies "
        + "(project_id, root_step_id, user_id, is_saved, is_deleted)",
        "wdk-create-index");
    SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + userSchema
        + "strategies_idx04 ON " + userSchema + "strategies "
        + "(is_deleted, is_saved, name, project_id, user_id)",
        "wdk-create-index");
  }

  private Set<String> getIndexes(WdkModel wdkModel, DataSource dataSource,
      String schema, String table) throws WdkModelException {
    if (schema.endsWith("."))
      schema = schema.substring(0, schema.length() - 1);

    Set<String> indexNames = new HashSet<>();
    ResultSet resultSet = null;
    try {
      PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
          "SELECT index_name FROM all_indexes "
              + "WHERE owner = ? AND table_name = ?");
      ps.setString(1, schema.toUpperCase());
      ps.setString(2, table.toUpperCase());
      resultSet = ps.executeQuery();
      while (resultSet.next()) {
        indexNames.add(resultSet.getString("index_name").toLowerCase());
      }
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return indexNames;
  }

  /**
   * Since some steps are shared between strategies, this method cannot be run.
   * Need to fix this before we can add strategy_id to the steps table;
   * 
   * @param wdkModel
   * @param dataSource
   * @throws WdkModelException
   */
  // private void fixStrategyIds(WdkModel wdkModel, DataSource dataSource)
  // throws WdkModelException {
  // logger.debug("adding strategy ids...");
  //
  // String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  //
  // // set the strategy_id for the root steps
  // logger.debug("updating root steps...");
  // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
  // + "steps sp SET sp.strategy_id = ("
  // + "     SELECT max(sr.strategy_id)                      "
  // + "     FROM " + userSchema + "strategies sr   "
  // + "     WHERE sr.user_id = sp.user_id    "
  // + "       AND sr.root_step_id = sp.display_id"
  // + "       AND sp.project_id = sr.project_id)", "wdk-update-root-step");
  //
  // // create a temp table with the strategy_id from root
  // logger.debug("creating nested steps...");
  // SqlUtils.executeUpdate(wdkModel, dataSource,
  // "CREATE TABLE wdk_nested_steps AS SELECT step_id, root_id"
  // + "  FROM (SELECT step_id, strategy_id, "
  // + "               CONNECT_BY_ROOT strategy_id AS root_id "
  // + "        FROM " + userSchema + "steps"
  // + "        START WITH strategy_id IS NOT NULL "
  // + "        CONNECT BY PRIOR user_id = user_id"
  // + "          AND PRIOR project_id = project_id"
  // + "          AND (display_id = PRIOR left_child_id"
  // + "               OR display_id = PRIOR right_child_id) )"
  // + "  WHERE strategy_id IS NULL", "wdk-create-nested-steps");
  //
  // logger.debug("adding index...");
  // SqlUtils.executeUpdate(wdkModel, dataSource,
  // "ALTER TABLE wdk_nested_steps ADD CONSTRAINT "
  // + " wdk_nested_steps_pk PRIMARY KEY (step_id)",
  // "wdk-nested-steps-index");
  //
  // logger.debug("updating nested steps...");
  // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE ("
  // + "    SELECT s.step_id, s.strategy_id, n.root_id"
  // + "    FROM " + userSchema + "steps s, wdk_nested_steps n"
  // + "    WHERE s.step_id = n.step_id)" + "  SET strategy_id = root_id",
  // "wdk-update-nested-steps");
  //
  // SqlUtils.executeUpdate(wdkModel, dataSource,
  // "DROP TABLE wdk_nested_steps", "wdk-drop-nested-steps");
  // }

  private void fixDisplayIds(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    logger.debug("updating display ids...");

    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // set the strategy_id for the root steps
    logger.debug("updating root step ids...");
    SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE "
        + "  (SELECT sr.root_step_id, sp.step_id" + "   FROM " + userSchema
        + "steps sp, " + userSchema + "strategies sr"
        + "   WHERE sp.user_id = sr.user_id"
        + "     AND sp.project_id = sr.project_id"
        + "     AND sp.display_id = sr.root_step_id)"
        + "  SET root_step_id = step_id", "wdk-update-root-step-id");

    // set the left & right child ids
    logger.debug("updating left and right child ids...");
    SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE userlogins3.steps s1"
        + "  SET s1.left_child_id = (SELECT s2.step_id "
        + "                          FROM " + userSchema + "steps s2 "
        + "                          WHERE s2.display_id = s1.left_child_id"
        + "                            AND s2.user_id = s1.user_id"
        + "                            AND s2.project_id = s1.project_id),"
        + "     s1.right_child_id = (SELECT s3.step_id "
        + "                          FROM " + userSchema + "steps s3 "
        + "                          WHERE s3.display_id = s1.right_child_id"
        + "                            AND s3.user_id = s1.user_id"
        + "                            AND s3.project_id = s1.project_id)",
        "wdk-update-child-step-id");
  }

  private void dropColumns(WdkModel wdkModel, DataSource dataSource)
      throws WdkModelException {
    logger.debug("dropping columns...");

    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    // drop unused columns
    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP COLUMN answer_id", "wdk-drop-column");

    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "steps DROP COLUMN display_id", "wdk-drop-column");

    SqlUtils.executeUpdate(wdkModel, dataSource, "ALTER TABLE " + userSchema
        + "strategies DROP COLUMN display_id", "wdk-drop-column");
  }
}
