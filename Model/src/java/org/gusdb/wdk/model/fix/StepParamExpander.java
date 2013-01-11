/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.QueryFactory;
import org.gusdb.wdk.model.user.StepFactory;
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

    private static final Logger logger = Logger
            .getLogger(StepParamExpander.class);

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
        super((command != null) ? command : "stepParamExpander",
                "expand the param clob into its own rows in step_params table");
    }

    public void expand(WdkModel wdkModel) throws SQLException,
            NoSuchAlgorithmException, JSONException, WdkModelException,
            WdkUserException {

        ResultSet resultSet = null;
        PreparedStatement psInsert = null;
        try {
            createParamTable(wdkModel);

            String schema = wdkModel.getModelConfig().getUserDB()
                    .getUserSchema();
            resultSet = prepareSelect(wdkModel, schema);
            psInsert = prepareInsert(wdkModel);
            DBPlatform platform = wdkModel.getUserPlatform();

            int count = 0;
            while (resultSet.next()) {
                int stepId = resultSet.getInt("step_id");
                String clob = platform.getClobData(resultSet, "display_params");

                if (clob == null)
                    continue;
                clob = clob.trim();
                if (!clob.startsWith("{"))
                    continue;

                List<String[]> values = parseClob(wdkModel, clob);

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
        String schema = "";
        int length = schema.length();
        String s = (length == 0) ? null : schema.substring(0, length - 1);
        if (platform.checkTableExists(s, "step_params"))
            return;

        SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE TABLE " + schema
                + "step_params ( step_id NUMBER(12) NOT NULL, "
                + " param_name VARCHAR(200) NOT NULL, "
                + " param_value VARCHAR(4000), migration NUMBER(12))",
                "wdk-create-table");

        SqlUtils.executeUpdate(wdkModel, dataSource, "CREATE INDEX " + schema
                + "step_params_idx02 ON step_params (step_id, param_name)",
                "wdk-create-indx");
    }

    private ResultSet prepareSelect(WdkModel wdkModel, String schema)
            throws SQLException, WdkUserException, WdkModelException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String user = userDB.getUserSchema() + "users";
        String step = userDB.getUserSchema() + "steps";
        String answer = userDB.getWdkEngineSchema() + "answers";
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(" s.step_id, s.display_params FROM ");
        sql.append(step + " s, (SELECT DISTINCT s.step_id, a.question_name ");
        sql.append(" FROM " + step + " s, " + answer + " a, " + user + " u, ");
        sql.append("  (SELECT step_id FROM " + step);
        sql.append("   MINUS ");
        sql.append("   SELECT step_id FROM " + schema + "step_params) sm ");
        sql.append(" WHERE s.step_id = sm.step_id ");
        sql.append("   AND s.user_id = u.user_id AND u.is_guest = 0 ");
        sql.append("   AND s.answer_id = a.answer_id) sp ");
        sql.append(" WHERE s.step_id = sp.step_id ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        return SqlUtils.executeQuery(wdkModel, dataSource, sql.toString(),
                "wdk-select-step-params");
    }

    private PreparedStatement prepareInsert(WdkModel wdkModel)
            throws SQLException, WdkModelException {
        String schema = "";
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(schema + "step_params ");
        sql.append(" (step_id, param_name, param_value) "
                + "  VALUES (?, ?, ?)");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        return SqlUtils.getPreparedStatement(dataSource, sql.toString());
    }

    private List<String[]> parseClob(WdkModel wdkModel, String clob)
            throws JSONException, NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException {
        StepFactory stepFactory = wdkModel.getStepFactory();
        QueryFactory queryFactory = wdkModel.getQueryFactory();
        Map<String, String> values = stepFactory.parseParamContent(clob);
        List<String[]> newValues = new ArrayList<String[]>();
        for (String paramName : values.keySet()) {
            String value = values.get(paramName);
            String prefix = Utilities.PARAM_COMPRESSE_PREFIX;
            if (value.startsWith(prefix)) {
                String checksum = value.substring(prefix.length()).trim();
                String decompressed = queryFactory.getClobValue(checksum);
                if (decompressed != null)
                    value = decompressed;
            }
            String[] terms = value.split(",");
            for (String term : terms) {
                if (term.length() > 4000)
                    term = term.substring(0, 4000);
                newValues.add(new String[] { paramName, term });
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
        addSingleValueOption(ARG_PROJECT_ID, true, null, "ProjectId, which"
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

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);

        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

        // expand step params
        logger.info("Expanding params...");
        expand(wdkModel);
    }
}
