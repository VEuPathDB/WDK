/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.sql.SQLException;
import java.sql.ResultSet;

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
 * @author steve fischer
 * 
 *  this script needs to be run after the model expander & step expander.
 *
 *  prints a report of invalid steps 
 */
public class InvalidStepReporter extends BaseCLI {

    private static final Logger logger = Logger.getLogger(StepValidator.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        InvalidStepReporter reporter = new InvalidStepReporter(cmdName);
        try {
            reporter.invoke(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("report complete.");
            System.exit(0);
        }
    }

    public InvalidStepReporter(String command) {
        super((command == null) ? command : "wdkInvalidStepReport",
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
        report(wdkModel);
    }

    private void report(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
	// not previously marked invalid
	System.out.println("=================================================");
	System.out.println("============= Will become invalid ===============");
	System.out.println("=================================================");
	System.out.println("");       

	questionNames(wdkModel, "!=");
        paramNames(wdkModel, "!=");
        paramValues(wdkModel, "FlatVocabParam", "!=");
        paramValues(wdkModel, "EnumParam", "!=");

	// previously marked invalid
	System.out.println("=================================================");
	System.out.println("============ Already marked invalid =============");
	System.out.println("=================================================");
	System.out.println("");       

        questionNames(wdkModel, "=");
        paramNames(wdkModel, "=");
        paramValues(wdkModel, "FlatVocabParam", "=");
        paramValues(wdkModel, "EnumParam", "=");
    }

    private void questionNames(WdkModel wdkModel, String operator) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

	String sql = 
	    "SELECT count(*) count, a.project_id, a.question_name" 
	    + " FROM " + answer + " a,"
	    + "  (SELECT project_id, question_name FROM " + answer
	    + "   MINUS"
	    + "   SELECT project_id, question_name FROM wdk_questions) d"
	    + " WHERE a.project_id = d.project_id"
	    + " AND a.question_name = d.question_name"
	    + " AND a.is_valid " + operator + "0"
	    + " group by a.project_id, a.question_name"
	    + " order by a.project_id, a.question_name";

	ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql);
	System.out.println("----------- Invalid Question Name ------------");
	System.out.println("");
	System.out.println("count\tproject_id\tquestion_name");
	try {
	    while (resultSet.next()) {
		String count = resultSet.getString("count");
		String project_id = resultSet.getString("project_id");
		String question_name = resultSet.getString("question_name");
		System.out.println(count + "\t" + project_id + "\t" + question_name);
	    }
        } finally {
            resultSet.close();
        }
	System.out.println("");
	System.out.println("");
    }

    private void paramNames(WdkModel wdkModel, String operator) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

	String sql = 
	    "SELECT count(*) count, a.project_id, a.question_name, sp.param_name"                
	    + " FROM step_params sp, " + answer + " a, " + step + " s, wdk_questions wq,"
	    + "      (SELECT a.project_id, a.question_name, sp.param_name "
	    + "       FROM step_params sp, " + step + " s, " + answer + " a"
	    + "       WHERE sp.step_id = s.step_id "
	    + "         AND s.answer_id = a.answer_id "
	    + "         AND a.is_valid " + operator + "0"
	    + "       MINUS  "                 
	    + "       SELECT q.project_id, q.question_name, p.param_name  "
	    + "       FROM wdk_questions q, wdk_params p "
	    + "       WHERE q.question_id = p.question_id) d     "
	    + "    WHERE a.project_id = d.project_id  "
	    + "      AND a.question_name = d.question_name  "
	    + "      AND a.answer_id = s.answer_id  "
	    + "      and a.project_id = wq.project_id "
	    + "      and a.question_name = wq.question_name "
	    + "      AND s.step_id = sp.step_id  "
	    + "      AND sp.param_name = d.param_name " 
	    + "      AND a.is_valid " + operator + "0"
	    + "      group by a.project_id, a.question_name, sp.param_name "
	    + "      order by a.project_id, a.question_name, sp.param_name ";

	ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql);
	System.out.println("----------- Invalid Param Name ------------");
	System.out.println("");
	System.out.println("count\tproject_id\tquestion_name\tparam_name");
	try {
	    while (resultSet.next()) {
		String count = resultSet.getString("count");
		String project_id = resultSet.getString("project_id");
		String question_name = resultSet.getString("question_name");
		String param_name = resultSet.getString("param_name");
		System.out.println(count + "\t" + project_id + "\t" + question_name + "\t" + param_name);

	    }
        } finally {
            resultSet.close();
        }
	System.out.println("");
	System.out.println("");
    }

    private void paramValues(WdkModel wdkModel, String type, String operator) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String answer = userDB.getWdkEngineSchema() + "answers";
        String step = userDB.getUserSchema() + "steps";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();

	String sql = 
"SELECT count(*) count, a.project_id, a.question_name, sp.param_name, d.param_value"                   
	    + " FROM step_params sp, " + answer + "  a, " + step + "  s, wdk_questions wq, wdk_params wp, "
	    + " (SELECT a.project_id, a.question_name,  "
	    + "         sp.param_name, sp.param_value  "
	    + "  FROM step_params sp, " + step + "  s, " + answer + "  a, "
	    + "        wdk_questions q, wdk_params p  "
	    + "  WHERE sp.step_id = s.step_id  "
	    + "    AND s.answer_id = a.answer_id  "
	    + "    AND a.is_valid IS not NULL  "
	    + "    AND a.project_id = q.project_id  "
	    + "    AND a.question_name = q.question_name  "
	    + "    AND q.question_id = p.question_id  "
	    + "    AND sp.param_name = p.param_name  "
	    + "    AND p.param_type = '" + type + "'"
	    + "  MINUS  "                 
	    + "  SELECT q.project_id, q.question_name,  "
	    + "         p.param_name, ep.param_value  "
	    + "  FROM wdk_questions q, wdk_params p,  "
	    + "       wdk_enum_params ep  "
	    + "  WHERE q.question_id = p.question_id  "
	    + "    AND p.param_id = ep.param_id) d  "
	    + " WHERE a.project_id = d.project_id  "
	    + " AND a.question_name = d.question_name  "
	    + " AND a.answer_id = s.answer_id  "
	    + " AND s.step_id = sp.step_id  "
	    + " AND sp.param_name = d.param_name  "
	    + " AND sp.param_value = d.param_value  "
	    + " and a.project_id = wq.project_id "
	    + " and a.question_name = wq.question_name "
	    + " and wp.question_id = wq.question_id "
	    + " and sp.param_name = wp.param_name "
 	    + " AND a.is_valid IS not NULL "
 	    + " group by a.project_id, a.question_name, sp.param_name, d.param_value "
	    + " order by a.project_id, a.question_name, sp.param_name, d.param_value ";     
     
	System.out.println("----------- Invalid Param Value (" + type + ") ------------");
	System.out.println("");
	System.out.println("count\tproject_id\tquestion_name\tparam_name\tparam_value");

	ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql);
	try {
	    while (resultSet.next()) {
		String count = resultSet.getString("count");
		String project_id = resultSet.getString("project_id");
		String question_name = resultSet.getString("question_name");
		String param_name = resultSet.getString("param_name");
		String param_value = resultSet.getString("param_value");
		System.out.println(count + "\t" + project_id + "\t" + question_name + "\t" + param_name + "\t" + param_value);
	    }
        } finally {
            resultSet.close();
        }
	System.out.println("");
	System.out.println("");
    }
}
