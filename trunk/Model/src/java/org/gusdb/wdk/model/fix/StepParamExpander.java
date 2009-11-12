/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wsf.util.BaseCLI;
import org.json.JSONException;

/**
 * @author xingao
 * 
 *         this code extract the param values from steps, and put them into
 *         separate row. The result will be used to expedite the step
 *         validation.
 */
public class StepParamExpander extends BaseCLI {

    private static final Logger logger = Logger.getLogger(StepParamExpander.class);

    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        StepParamExpander expender = new StepParamExpander(cmdName);
        try {
            expender.invoke(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("step params expanded.");
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    protected StepParamExpander(String command) {
        super((command == null) ? command : "stepParamExpander",
                "expand the param clob into its own rows in step_params table");
    }

    public void expand(WdkModel wdkModel) throws SQLException,
            NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException {
        createParamTable(wdkModel);

        ResultSet resultSet = null;
        PreparedStatement psInsert = null;
        try {
            Map<String, Map<String, Param>> allParams = getParams(wdkModel);
            resultSet = prepareSelect(wdkModel);
            psInsert = prepareInsert(wdkModel);
            DBPlatform platform = wdkModel.getUserPlatform();

            int count = 0;
            while (resultSet.next()) {
                int stepId = resultSet.getInt("step_id");
                String questionName = resultSet.getString("question_name");
                String clob = platform.getClobData(resultSet, "display_params");

                if (clob == null) continue;
                clob = clob.trim();
                if (!clob.startsWith("{")) continue;
                if (!allParams.containsKey(questionName)) continue;

                Map<String, Param> params = allParams.get(questionName);
                List<String[]> values = parseClob(wdkModel, params, clob);

                // insert the values
                for (String[] pair : values) {
                    String paramName = pair[0].trim();
                    String paramValue = pair[1].trim();
                    psInsert.setInt(1, stepId);
                    psInsert.setString(2, paramName);
                    psInsert.setString(3, paramValue);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();

                count++;
                if (count % 100 == 0)
                    logger.debug(count + " steps processed.");
            }
            logger.info("Totally processed " + count + " steps.");
        } finally {
            SqlUtils.closeResultSet(resultSet);
            SqlUtils.closeStatement(psInsert);
        }
    }

    private void createParamTable(WdkModel wdkModel) throws SQLException,
            WdkModelException, WdkUserException {
        DBPlatform platform = wdkModel.getUserPlatform();
        DataSource dataSource = platform.getDataSource();

        // check if table exists
        if (platform.checkTableExists(null, "step_params")) return;

        SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE TABLE step_params"
                + " ( step_id NUMBER(12) NOT NULL, "
                + " param_name VARCHAR(200) NOT NULL, "
                + " param_value VARCHAR(4000), migration NUMBER(12))");

        SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX "
                + "step_params_idx02 ON step_params (step_id, param_name)");
    }

    private Map<String, Map<String, Param>> getParams(WdkModel wdkModel) {
        Map<String, Map<String, Param>> allParams = new LinkedHashMap<String, Map<String, Param>>();
        for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
            for (Question question : questionSet.getQuestions()) {
                String questionName = question.getFullName();
                Map<String, Param> params = question.getParamMap();
                allParams.put(questionName, params);
            }
        }
        return allParams;
    }

    private ResultSet prepareSelect(WdkModel wdkModel) throws SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String user = userDB.getUserSchema() + "users";
        String step = userDB.getUserSchema() + "steps";
        String answer = userDB.getWdkEngineSchema() + "answers";
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(" s.step_id, sp.question_name, s.display_params FROM ");
        sql.append(step + " s, (SELECT DISTINCT s.step_id, a.question_name ");
        sql.append(" FROM " + step + " s, " + answer + " a, " + user + " u, ");
        sql.append("  (SELECT step_id FROM ").append(step);
        sql.append("   MINUS ");
        sql.append("   SELECT step_id FROM ").append("step_params) sm ");
        sql.append("WHERE s.step_id = sm.step_id ");
        sql.append("  AND s.user_id = u.user_id AND u.is_guest = 0 ");
        sql.append("  AND s.answer_id = a.answer_id ");
        sql.append("  AND a.project_id = ? ) sp ");
        sql.append(" WHERE s.step_id = sp.step_id ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psSelect = SqlUtils.getPreparedStatement(dataSource,
                sql.toString());
        psSelect.setString(1, wdkModel.getProjectId());
        return psSelect.executeQuery();
    }

    private PreparedStatement prepareInsert(WdkModel wdkModel)
            throws SQLException {
        StringBuffer sql = new StringBuffer("INSERT INTO step_params ");
        sql.append(" (step_id, param_name, param_value) VALUES (?, ?, ?)");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        return SqlUtils.getPreparedStatement(dataSource, sql.toString());
    }

    private List<String[]> parseClob(WdkModel wdkModel,
            Map<String, Param> params, String clob) throws JSONException,
            NoSuchAlgorithmException, WdkModelException, WdkUserException,
            SQLException {
        StepFactory stepFactory = wdkModel.getStepFactory();
        Map<String, String> values = stepFactory.parseParamContent(clob);
        List<String[]> newValues = new ArrayList<String[]>();
        User user = wdkModel.getSystemUser();
        for (String paramName : values.keySet()) {
            String value = values.get(paramName);
            Param param = params.get(paramName);
            if (param != null && param instanceof AbstractEnumParam) {
                AbstractEnumParam enumParam = (AbstractEnumParam) param;
                value = enumParam.dependentValueToRawValue(user, value);
                if (value == null) continue;
                String[] terms = value.split(",");
                for (String term : terms) {
                    newValues.add(new String[] { paramName, term });
                }
            } else {
                if (value != null && value.length() > 4000)
                    value = value.substring(0, 4000);
                newValues.add(new String[] { paramName, value });
            }
        }
        return newValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
                + " list of ProjectIds, which"
                + " should match the directory name under $GUS_HOME, where"
                + " model-config.xml is stored.");
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
            logger.info("Expanding params for project " + projectId);
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            expand(wdkModel);
        }
    }
}
