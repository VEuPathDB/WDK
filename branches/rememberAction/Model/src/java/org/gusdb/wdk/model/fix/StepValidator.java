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
        addSingleValueOption(ARG_PROJECT_ID, true, null, "ProjectId, which "
                + "should match the directory name"
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

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);
        logger.info("Validate steps & answers... ");
        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
        validate(wdkModel);
    }

    private void validate(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        resetFlags(wdkModel);
        detectQuestions(wdkModel);
        detectParams(wdkModel);
        detectEnumParams(wdkModel);

        flagSteps(wdkModel);
    }

    private void resetFlags(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDB.getUserSchema();
        String wdkSchema = userDB.getWdkEngineSchema();

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + wdkSchema
                + "answers SET is_valid = NULL");
        SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
                + "steps SET is_valid = NULL");
        SqlUtils.executeUpdate(wdkModel, dataSource, "UPDATE " + userSchema
                + "strategies SET is_valid = NULL");
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
                + "     AND a.is_valid IS NULL)");
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
                + "     AND a.is_valid IS NULL)");
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
                + "     AND a.is_valid IS NULL)");
    }

    private void flagSteps(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // mark invalid steps
        SqlUtils.executeUpdate(wdkModel, source, "UPDATE " + step
                + " SET is_valid = 0 "
                + " WHERE (is_valid IS NULL OR is_valid = 1) "
                + "   AND answer_id IN (SELECT answer_id FROM " + answer
                + "                    WHERE is_valid = 0)");
    }
}
