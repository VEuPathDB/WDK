/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.sql.SQLException;

import javax.sql.DataSource;

import java.sql.PreparedStatement; // <MOD-AG 042911>
import java.sql.ResultSet; // <MOD-AG 042911>
import java.sql.Connection; // <MOD-AG 042911>

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 *         this script needs to be run after the model expender & step expender.
 * 
 *         just accept one model. the model is only used to provide the access
 *         to user db, the result of this program will affect all projects in
 *         that user db.
 */
public class StepValidator extends BaseCLI {

    private static final Logger logger = Logger.getLogger(StepValidator.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        StepValidator validator = new StepValidator(cmdName);
        try {
            validator.invoke(args);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        finally {
            logger.info("step validator done.");
            System.exit(0);
        }
    }

    public StepValidator(String command) {
        super((command == null) ? command : "wdkValidateStep",
                "store model information into database");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId"
                + ", which should match the directory name under $GUS_HOME, "
                + "where model-config.xml is stored. You just need to use "
                + "one project to provide connection to database, and the "
                + "change will affect all projects.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);
        logger.info("Validate steps & answers for all projects... ");

        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

        dropDanglingSteps(wdkModel);
        deleteInvalidParams(wdkModel);

        resetFlags(wdkModel);
        detectQuestions(wdkModel);
        detectParams(wdkModel);
        detectEnumParams(wdkModel);

        flagSteps(wdkModel);
        flagDependentSteps(wdkModel);
    }

    private void resetFlags(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        logger.debug("resetting is_valid flags...");
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDB.getUserSchema();
        String wdkSchema = userDB.getWdkEngineSchema();

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

        resetByBatch(wdkModel, dataSource, "UPDATE " + wdkSchema
                + "answers SET is_valid = NULL", "wdk-reset-answer-flag"); // <ADD-AG
                                                                           // 042911>

        // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + wdkSchema
        // + "answers SET is_valid = NULL", "wdk-reset-answer-flag");

        resetByBatch(wdkModel, dataSource, "UPDATE " + userSchema
                + "steps SET is_valid = NULL", "wdk-reset-step-flag"); // <ADD-AG
                                                                       // 042911>

        // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
        // + "steps SET is_valid = NULL", "wdk-reset-step-flag");

        // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
        // + "strategies SET is_valid = NULL");
    }

    private void detectQuestions(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        logger.debug("detecting invalid questions...");

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // mark invalid answers
        // SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
        // + " SET is_valid = 0 WHERE answer_id IN "
        // + "  (SELECT a.answer_id FROM " + answer + " a, "
        // + "    (SELECT project_id, question_name FROM " + answer
        // + "     MINUS "
        // + "     SELECT project_id, question_name FROM wdk_questions) d"
        // + "   WHERE a.project_id = d.project_id"
        // + "     AND a.question_name = d.question_name"
        // + "     AND a.is_valid IS NULL)", "wdk-invalidate-question");

        // <ADD-AG 042911> ----------------------------------------------------

        String sql = "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "(SELECT a.answer_id FROM " + answer + " a, "
                + "    (SELECT project_id, question_name FROM " + answer
                + "     MINUS "
                + "     SELECT project_id, question_name FROM wdk_questions) d"
                + "   WHERE a.project_id = d.project_id"
                + "     AND a.question_name = d.question_name"
                + "     AND a.is_valid IS NULL)";

        executeByBatch(wdkModel, source, sql, "ANSWER:wdk-invalidate-question",
                null, null);

        // </ADD-AG 042911> ---------------------------------------------------
    }

    private void detectParams(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        logger.debug("detecting invalid params...");

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
        // + " SET is_valid = 0 WHERE answer_id IN "
        // + "   (SELECT a.answer_id                 "
        // + "    FROM step_params sp, " + answer + " a, " + step + " s, "
        // + "     (SELECT a.project_id, a.question_name, sp.param_name "
        // + "      FROM step_params sp, " + step + " s, " + answer + " a"
        // + "      WHERE sp.step_id = s.step_id "
        // + "        AND s.answer_id = a.answer_id "
        // + "        AND a.is_valid IS NULL "
        // + "      MINUS                  "
        // + "      SELECT q.project_id, q.question_name, p.param_name "
        // + "      FROM wdk_questions q, wdk_params p"
        // + "      WHERE q.question_id = p.question_id) d "
        // + "   WHERE a.project_id = d.project_id "
        // + "     AND a.question_name = d.question_name "
        // + "     AND a.answer_id = s.answer_id "
        // + "     AND s.step_id = sp.step_id "
        // + "     AND sp.param_name = d.param_name "
        // + "     AND a.is_valid IS NULL)", "wdk-invalidate-param");

        // <ADD-AG 042911> ----------------------------------------------------

        String sql = "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "(SELECT a.answer_id                 "
                + "    FROM step_params sp, " + answer + " a, " + step + " s, "
                + "     (SELECT a.project_id, a.question_name, sp.param_name "
                + "      FROM step_params sp, " + step + " s, " + answer + " a"
                + "      WHERE sp.step_id = s.step_id "
                + "        AND s.answer_id = a.answer_id "
                + "        AND a.is_valid IS NULL "
                + "      MINUS                  "
                + "      SELECT q.project_id, q.question_name, p.param_name "
                + "      FROM wdk_questions q, wdk_params p"
                + "      WHERE q.question_id = p.question_id) d "
                + "   WHERE a.project_id = d.project_id "
                + "     AND a.question_name = d.question_name "
                + "     AND a.answer_id = s.answer_id "
                + "     AND s.step_id = sp.step_id "
                + "     AND sp.param_name = d.param_name "
                + "     AND a.is_valid IS NULL)";

        executeByBatch(wdkModel, source, sql, "ANSWER:wdk-invalidate-param",
                null, null);

        // </ADD-AG 042911> ---------------------------------------------------
    }

    private void detectEnumParams(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        logger.debug("detecting invalid enum params...");

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
        // + " SET is_valid = 0 WHERE answer_id IN "
        // + "   (SELECT a.answer_id                 "
        // + "    FROM step_params sp, " + answer + " a, " + step + " s, "
        // + "     (SELECT a.project_id, a.question_name, "
        // + "             sp.param_name, sp.param_value "
        // + "      FROM step_params sp, " + step + " s, " + answer
        // + "        a, wdk_questions q, wdk_params p "
        // + "      WHERE sp.step_id = s.step_id "
        // + "        AND s.answer_id = a.answer_id "
        // + "        AND a.is_valid IS NULL "
        // + "        AND a.project_id = q.project_id "
        // + "        AND a.question_name = q.question_name "
        // + "        AND q.question_id = p.question_id "
        // + "        AND sp.param_name = p.param_name "
        // + "        AND p.param_type IN ('EnumParam', 'FlatVocabParam')"
        // + "      MINUS                  "
        // + "      SELECT q.project_id, q.question_name, "
        // + "             p.param_name, ep.param_value "
        // + "      FROM wdk_questions q, wdk_params p, "
        // + "           wdk_enum_params ep "
        // + "      WHERE q.question_id = p.question_id "
        // + "        AND p.param_id = ep.param_id) d "
        // + "   WHERE a.project_id = d.project_id "
        // + "     AND a.question_name = d.question_name "
        // + "     AND a.answer_id = s.answer_id "
        // + "     AND s.step_id = sp.step_id "
        // + "     AND sp.param_name = d.param_name "
        // + "     AND sp.param_value = d.param_value "
        // + "     AND a.is_valid IS NULL)", "wdk-invalidate-enum-param");

        // <ADD-AG 042911> ----------------------------------------------------

        String sql = "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "(SELECT a.answer_id                 "
                + "    FROM step_params sp, " + answer + " a, " + step + " s, "
                + "     (SELECT a.project_id, a.question_name, "
                + "             sp.param_name, sp.param_value "
                + "      FROM step_params sp, " + step + " s, " + answer
                + "        a, wdk_questions q, wdk_params p "
                + "      WHERE sp.step_id = s.step_id "
                + "        AND s.answer_id = a.answer_id "
                + "        AND a.is_valid IS NULL "
                + "        AND a.project_id = q.project_id "
                + "        AND a.question_name = q.question_name "
                + "        AND q.question_id = p.question_id "
                + "        AND sp.param_name = p.param_name "
                + "        AND p.param_type IN ('EnumParam', 'FlatVocabParam')"
                + "      MINUS                  "
                + "      SELECT q.project_id, q.question_name, "
                + "             p.param_name, ep.param_value "
                + "      FROM wdk_questions q, wdk_params p, "
                + "           wdk_enum_params ep "
                + "      WHERE q.question_id = p.question_id "
                + "        AND p.param_id = ep.param_id) d "
                + "   WHERE a.project_id = d.project_id "
                + "     AND a.question_name = d.question_name "
                + "     AND a.answer_id = s.answer_id "
                + "     AND s.step_id = sp.step_id "
                + "     AND sp.param_name = d.param_name "
                + "     AND sp.param_value = d.param_value "
                + "     AND a.is_valid IS NULL)";

        executeByBatch(wdkModel, source, sql,
                "ANSWER:wdk-invalidate-enum-param", null, null);

        // </ADD-AG 042911> ---------------------------------------------------
    }

    private void flagSteps(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        logger.debug("flagging invalid steps...");

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        String sql = "UPDATE " + step + " SET is_valid = 0 "
                + "WHERE (is_valid IS NULL OR is_valid = 1) "
                + "  AND answer_id IN (SELECT answer_id FROM " + answer
                + "                    WHERE is_valid = 0)";
        // mark invalid steps
        // SqlUtils.executeUpdate(wdkModel, source, sql, "wdk-invalidate-step");

        executeByBatch(wdkModel, source, sql, "STEP:wdk-invalidate-step", null,
                null); // <ADD-AG 042911>
    }

    private void flagDependentSteps(WdkModel wdkModel) throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("Flagging invalid dependent steps...");

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        String tempTable = "wdk_part_steps";

        String sql = "CREATE TABLE " + tempTable + " NOLOGGING AS "
                + " SELECT step_id, user_id, display_id, left_child_id, "
                + "        right_child_id, is_valid " + " FROM " + step
                + " WHERE user_id IN " + "   (SELECT user_id FROM " + step
                + " WHERE is_valid = 0)";
        SqlUtils.executeUpdate(wdkModel, source, sql,
                "wdk-invalidate-create-part-steps");

        sql = "UPDATE " + step + " SET is_valid = 0 "
                + "WHERE is_valid IS NULL AND step_id IN ("
                + "SELECT step_id FROM " + tempTable
                + "  START WITH is_valid = 0 "
                + "  CONNECT BY (prior display_id = right_child_id "
                + "              OR prior display_id = left_child_id) "
                + "            AND prior user_id = user_id)";

        // SqlUtils.executeUpdate(wdkModel, source, sql,
        // "wdk-invalidate-parent-step");

        executeByBatch(wdkModel, source, sql,
                "STEP:wdk-invalidate-parent-step", null, null); // <ADD-AG
                                                                // 042911>

        sql = "DROP TABLE " + tempTable + " PURGE";
        SqlUtils.executeUpdate(wdkModel, source, sql,
                "wdk-invalidate-drop-part-steps");

    }

    private void deleteInvalidParams(WdkModel wdkModel)
            throws WdkUserException, WdkModelException, SQLException {
        logger.info("Deleting params which doesn't have a valid step...");
        String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append("step_params WHERE step_id NOT IN ");
        sql.append("(SELECT step_id FROM " + userSchema + "steps)");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-delete-invalid-step-params");
    }

    private void dropDanglingSteps(WdkModel wdkModel) throws WdkUserException,
            WdkModelException, SQLException {
        logger.info("drop dangling steps table and related resources...");

        String stepTable = "wdk_dangle_steps";
        String parentTable = "wdk_parent_steps";
        String strategyTable = "wdk_dangle_strategies";

        // drop the temp tables if exist
        DBPlatform platform = wdkModel.getUserPlatform();
        if (platform.checkTableExists(null, stepTable))
            platform.dropTable(null, stepTable, true);
        if (platform.checkTableExists(null, parentTable))
            platform.dropTable(null, parentTable, true);
        if (platform.checkTableExists(null, strategyTable))
            platform.dropTable(null, strategyTable, true);

        String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        if (schema.length() > 0 && !schema.endsWith(".")) schema += ".";

        selectDanglingSteps(wdkModel, schema, stepTable);
        selectParentSteps(wdkModel, schema, stepTable, parentTable);
        selectDanglingStrategies(wdkModel, schema, strategyTable, parentTable);
        deleteDanglingStrategies(wdkModel, schema, strategyTable);
        deleteDanglingSteps(wdkModel, schema, parentTable);

        // do not drop the temp tables immediately, need to investigate those
        // steps and strategies, and notify user.

        // DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        // SqlUtils.executeUpdate(wdkModel, dataSource, "DROP TABLE "
        // + parentTable + " PURGE", "wdk_drop_dangle_strategies");
        // SqlUtils.executeUpdate(wdkModel, dataSource, "DROP TABLE "
        // + stepTable + " PURGE", "wdk_drop_dangle_steps");
        // SqlUtils.executeUpdate(wdkModel, dataSource, "DROP TABLE "
        // + parentTable + " PURGE", "wdk_drop_parent_steps");
    }

    private void selectDanglingSteps(WdkModel wdkModel, String schema,
            String danglingTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("looking for dangling steps...");

        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(danglingTable + " AS ");
        sql.append("  ( (SELECT s1.step_id ");
        sql.append("     FROM " + stepTable + " s1 ");
        sql.append("     LEFT JOIN " + stepTable + " s2 ");
        sql.append("       ON s1.user_id = s2.user_id");
        sql.append("          AND s1.left_child_id = s2.display_id ");
        sql.append("     WHERE s1.left_child_id IS NOT NULL ");
        sql.append("       AND s2.display_id IS NULL) ");
        sql.append("   UNION ");
        sql.append("   ( SELECT s1.step_id  ");
        sql.append("     FROM " + stepTable + " s1 ");
        sql.append("     LEFT JOIN " + stepTable + " s2 ");
        sql.append("       ON s1.user_id = s2.user_id");
        sql.append("          AND s1.right_child_id = s2.display_id ");
        sql.append("     WHERE s1.right_child_id IS NOT NULL ");
        sql.append("       AND s2.display_id IS NULL) ");
        sql.append("  )");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-dangling-step");
    }

    private void selectParentSteps(WdkModel wdkModel, String schema,
            String danglingTable, String parentTable) throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("looking for parents of dangling steps...");

        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(parentTable + " AS ");
        sql.append("  (SELECT s.step_id, s.user_id, s.display_id ");
        sql.append("   FROM " + stepTable + " s ");
        sql.append("   START WITH s.step_id IN ");
        sql.append("     (SELECT step_id FROM " + danglingTable + ") ");
        sql.append("   CONNECT BY PRIOR s.user_id = s.user_id");
        sql.append("     AND (PRIOR s.display_id = s.left_child_id");
        sql.append("          OR PRIOR s.display_id = s.right_child_id) ");
        sql.append("  )");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-parent-step");
    }

    private void selectDanglingStrategies(WdkModel wdkModel, String schema,
            String strategyTable, String parentTable) throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("Selecting dangling strategies...");

        String stratTable = schema + "strategies";
        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("CREATE TABLE " + strategyTable);
        sql.append(" AS (SELECT sr.strategy_id  ");
        sql.append("     FROM " + stratTable + " sr, wdk_parent_steps ps ");
        sql.append("     WHERE sr.user_id = ps.user_id ");
        sql.append("     AND sr.root_step_id = ps.display_id ");
        sql.append("   UNION ");
        sql.append("    SELECT sr.strategy_id ");
        sql.append("     FROM " + stratTable + " sr ");
        sql.append("       LEFT JOIN " + stepTable + " sp ");
        sql.append("       ON sr.user_id = sp.user_id ");
        sql.append("         AND sr.root_step_id = sp.display_id ");
        sql.append("     WHERE sp.display_id IS NULL");
        sql.append("  )"); // <MOD-AG 050511>

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-dangling-strategies");
    }

    private void deleteDanglingStrategies(WdkModel wdkModel, String schema,
            String strategyTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Deleting dangling strategies...");

        String stratTable = schema + "strategies";

        StringBuilder sql = new StringBuilder("DELETE FROM " + stratTable);
        sql.append(" WHERE strategy_id IN ");
        sql.append("(SELECT strategy_id FROM " + strategyTable + ")");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

        // <ADD-AG 050511>
        executeByBatch(wdkModel, dataSource, sql.toString(),
                "STRATEGIES:wdk-delete-dangling-strategy", null, null);
    }

    private void deleteDanglingSteps(WdkModel wdkModel, String schema,
            String parentTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Deleting dangling steps...");

        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("DELETE FROM " + stepTable);
        sql.append(" WHERE step_id IN (SELECT step_id FROM " + parentTable
                + ")");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

        executeByBatch(wdkModel, dataSource, sql.toString(),
                "STEPS:wdk-delete-dangling-step", null, null); // <ADD-AG
                                                               // 050511>

        // SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
        // "wdk-delete-dangling-step");
    }

    // <ADD-AG 042911>
    // -----------------------------------------------------------

    private void executeByBatch(WdkModel wdkModel, DataSource dataSource,
            String sql, String name, String dmlSql, String selectSql)
            throws SQLException, WdkUserException, WdkModelException {

        if ((dmlSql == null) || (selectSql == null)) {
            dmlSql = sql.substring(0, sql.indexOf("IN ", 0)) + " = ?";
            selectSql = sql.substring(sql.indexOf("IN ", 0) + 4);
            selectSql = selectSql.substring(0, selectSql.length() - 1);

            // logger.info("dmlSql= " + dmlSql);
            // logger.info("selectSql= " + selectSql);
        }

        Connection connection = null;
        PreparedStatement psInsert = null;
        ResultSet resultSet = null;

        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, selectSql,
                    "wdk-backup-" + name);

            connection = dataSource.getConnection();
            psInsert = connection.prepareStatement(dmlSql);

            int count = 0;

            while (resultSet.next()) {
                int userId = resultSet.getInt(1);

                psInsert.setInt(1, userId);
                psInsert.addBatch();

                count++;
                if (count % 1000 == 0) {
                    psInsert.executeBatch();
                    logger.info("Rows processed for " + name + " = " + count
                            + ".");
                }
            }

            psInsert.executeBatch();
            logger.info("Total rows processed for " + name + " = " + count
                    + ".");
        }
        finally {
            SqlUtils.closeResultSet(resultSet);
            SqlUtils.closeStatement(psInsert);
        }
    }

    // ---------------------------------------------------------------------------

    private void resetByBatch(WdkModel wdkModel, DataSource dataSource,
            String sql, String name) throws SQLException, WdkUserException,
            WdkModelException {

        sql = sql + " where is_valid is not NULL and rownum < 1000";

        int rowsAffected = 1000;
        int totalAffected = 0;

        while (rowsAffected > 0) {
            rowsAffected = SqlUtils.executeUpdate(wdkModel, dataSource, sql,
                    name);

            totalAffected += rowsAffected;

            logger.info("Rows reset for " + name + " = " + totalAffected + ".");
        }
    }

    // </ADD-AG 042911>
    // ----------------------------------------------------------

}
