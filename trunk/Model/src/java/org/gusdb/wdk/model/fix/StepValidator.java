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
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 */
/**
 * @author xingao
 *
 */
/**
 * @author xingao
 * 
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
            logger.info("model cacher done.");
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
        addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
                + " list of ProjectIds, which should match the directory name"
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

        String strProject = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = strProject.split(",");

        for (String projectId : projects) {
            logger.info("Expanding model for project " + projectId);
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            validate(wdkModel);
        }
    }

    private void validate(WdkModel wdkModel) throws SQLException {
        resetFlags(wdkModel);
        detectQuestions(wdkModel);
    }

    private void resetFlags(WdkModel wdkModel) throws SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDB.getUserSchema();
        String wdkSchema = userDB.getWdkEngineSchema();

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(dataSource, "UPDATE " + wdkSchema
                + "answers SET is_valid = NULL");
        SqlUtils.executeUpdate(dataSource, "UPDATE " + userSchema
                + "steps SET is_valid = NULL");
        SqlUtils.executeUpdate(dataSource, "UPDATE " + userSchema
                + "strategies SET is_valid = NULL");
    }

    private void detectQuestions(WdkModel wdkModel) throws SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource source = wdkModel.getUserPlatform().getDataSource();

        // mark invalid answers
        SqlUtils.executeUpdate(source, "UPDATE " + answer
                + " SET is_valid = 0 WHERE answer_id IN "
                + "  (SELECT a.answer_id FROM " + answer + " a, "
                + "    (SELECT project_id, question_name FROM " + answer
                + "     MINUS "
                + "     SELECT project_id, question_name FROM wdk_questions) d"
                + "   WHERE a.project_id = d.project_id"
                + "     AND a.question_name = d.question_name"
                + "     AND (a.is_valid IS NULL OR a.is_valid = 1))");

        // mark invalid steps
        SqlUtils.executeUpdate(source, "UPDATE " + step + " SET is_valid = 0 "
                + " WHERE (is_valid IS NULL OR is_valid = 1) "
                + "   AND answer_id IN (SELECT answer_id FROM " + answer
                + "                    WHERE is_valid = 0");
    }
}