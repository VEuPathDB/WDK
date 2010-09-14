/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
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
        addSingleValueOption(ARG_PROJECT_ID, true, null, "a list of ProjectIds"
                + ", which should match the directory name"
                + " under $GUS_HOME, where model-config.xml is stored.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        String ids = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projectIds = ids.split(",");
        for (String projectId : projectIds) {
            projectId = projectId.trim();
            logger.info("Validate steps & answers for " + projectId + "... ");
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            validate(wdkModel);
        }
    }

    private void validate(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        deleteDanglingSteps(wdkModel);
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
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDB.getUserSchema();
        String wdkSchema = userDB.getWdkEngineSchema();

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + wdkSchema
                + "answers SET is_valid = NULL", "wdk-reset-answer-flag");
        SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
                + "steps SET is_valid = NULL", "wdk-reset-step-flag");
        // SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
        // + "strategies SET is_valid = NULL");
    }

    private void detectQuestions(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // mark invalid answers
        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "  (SELECT a.answer_id FROM " + answer + " a, "
                + "    (SELECT project_id, question_name FROM " + answer
                + "     MINUS "
                + "     SELECT project_id, question_name FROM wdk_questions) d"
                + "   WHERE a.project_id = d.project_id"
                + "     AND a.question_name = d.question_name"
                + "     AND a.is_valid IS NULL)", "wdk-invalidate-question");
    }

    private void detectParams(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "   (SELECT a.answer_id                 "
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
                + "     AND a.is_valid IS NULL)", "wdk-invalidate-param");
    }

    private void detectEnumParams(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "   (SELECT a.answer_id                 "
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
                + "     AND a.is_valid IS NULL)", "wdk-invalidate-enum-param");
    }

    private void flagSteps(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        String sql = "UPDATE " + step + " SET is_valid = 0 "
                + "WHERE (is_valid IS NULL OR is_valid = 1) "
                + "  AND answer_id IN (SELECT answer_id FROM " + answer
                + "                    WHERE is_valid = 0)";
        // mark invalid steps
        SqlUtils.executeUpdate(wdkModel, source, sql, "wdk-invalidate-step");
    }

    private void flagDependentSteps(WdkModel wdkModel) throws WdkUserException,
            WdkModelException, SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        String sql = "UPDATE " + step + " SET is_valid = 0 "
                + "WHERE is_valid IS NULL AND step_id IN ("
                + "    SELECT step_id FROM " + step
                + "    START WITH is_valid = 0 "
                + "    CONNECT BY (prior display_id = right_child_id "
                + "                OR prior display_id = left_child_id) "
                + "               AND prior user_id = user_id)";

        SqlUtils.executeUpdate(wdkModel, source, sql,
                "wdk-invalidate-parent-step");

    }

    private void deleteInvalidParams(WdkModel wdkModel)
            throws WdkUserException, WdkModelException, SQLException {
        logger.info("Deleting params which doesn't have a valid step...");
        String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append("step_params WHERE step_id IN ");
        sql.append("(SELECT step_id FROM " + userSchema + "steps)");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-delete-invalid-step-params");
    }

    private void deleteDanglingSteps(WdkModel wdkModel)
            throws WdkUserException, WdkModelException, SQLException {
        logger.info("deleting dangling steps and related resources...");

        String danglingTable = "wdk_dangle_steps";
        String parentTable = "wdk_parent_steps";

        String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        if (schema.length() > 0 && !schema.endsWith(".")) schema += ".";

        selectDanglingSteps(wdkModel, schema, danglingTable);
        selectParentSteps(wdkModel, schema, danglingTable, parentTable);
        deleteDanglingStrategies(wdkModel, schema, parentTable);
        deleteDanglingSteps(wdkModel, schema, parentTable);

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, "DROP TABLE "
                + danglingTable, "wdk_drop_dangle_steps");
        SqlUtils.executeUpdate(wdkModel, dataSource, "DROP TABLE "
                + parentTable, "wdk_drop_parent_steps");
    }

    private void selectDanglingSteps(WdkModel wdkModel, String schema,
            String danglingTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("looking for dangling steps...");

        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(danglingTable + " AS ");
        sql.append("  (( SELECT s.step_id ");
        sql.append("     FROM " + stepTable + " s,  ");
        sql.append("          (  SELECT user_id, left_child_id ");
        sql.append("             FROM " + stepTable);
        sql.append("             WHERE left_child_id IS NOT NULL ");
        sql.append("           MINUS ");
        sql.append("             SELECT user_id, display_id FROM " + stepTable);
        sql.append("          ) u ");
        sql.append("     WHERE s.user_id = u.user_id ");
        sql.append("       AND s.left_child_id = u.left_child_id) ");
        sql.append("   UNION ");
        sql.append("   ( SELECT s.step_id  ");
        sql.append("     FROM " + stepTable + " s,  ");
        sql.append("          (  SELECT user_id, right_child_id ");
        sql.append("             FROM " + stepTable);
        sql.append("             WHERE right_child_id IS NOT NULL ");
        sql.append("           MINUS ");
        sql.append("             SELECT user_id, display_id FROM " + stepTable);
        sql.append("          ) u ");
        sql.append("     WHERE s.user_id = u.user_id ");
        sql.append("       AND s.right_child_id = u.right_child_id) ");
        sql.append("  )");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-dangling-step");
    }

    private void selectParentSteps(WdkModel wdkModel, String schema,
            String danglingTable, String parentTable) throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("looking for parents of dangling steps...");

        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(parentTable + " AS ");
        sql.append("  (SELECT s.step_id, s.user_id, s.display_id ");
        sql.append("   FROM " + schema + "steps s");
        sql.append("   START WITH s.step_id IN ");
        sql.append("     (SELECT step_id FROM " + danglingTable + ")");
        sql.append("   CONNECT BY PRIOR s.user_id = s.user_id");
        sql.append("     AND (PRIOR s.display_id = s.left_child_id");
        sql.append("          OR PRIOR s.display_id = s.right_child_id))");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-parent-step");
    }

    private void deleteDanglingStrategies(WdkModel wdkModel, String schema,
            String parentTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Deleting dangling strategies...");

        String stratTable = schema + "strategies";
        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("DELETE FROM " + stratTable);
        sql.append("WHERE strategy_id IN ");
        sql.append("  ( (SELECT sr.strategy_id  ");
        sql.append("     FROM " + stratTable + " sr, wdk_parent_steps ps ");
        sql.append("     WHERE sr.user_id = ps.user_id ");
        sql.append("     AND sr.root_step_id = ps.display_id) ");
        sql.append("   UNION ");
        sql.append("    (SELECT sr.strategy_id ");
        sql.append("     FROM " + stratTable + " sr ");
        sql.append("       LEFT JOIN " + stepTable + " sp ");
        sql.append("       ON sr.user_id = sp.user_id ");
        sql.append("         AND sr.root_step_id = sp.display_id ");
        sql.append("     WHERE sp.display_id IS NULL)");
        sql.append("  )");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-delete-dangling-strategy");
    }

    private void deleteDanglingSteps(WdkModel wdkModel, String schema,
            String parentTable) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Deleting dangling steps...");

        String stepTable = schema + "steps";

        StringBuilder sql = new StringBuilder("DELETE FROM " + stepTable);
        sql.append("WHERE step_id IN (SELECT step_id FROM " + parentTable + ")");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-delete-dangling-step");
    }

}
