/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wsf.util.BaseCLI;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class StepCountUpdater extends BaseCLI {

    private static final Logger logger = Logger.getLogger(StepCountUpdater.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        StepCountUpdater updater = new StepCountUpdater(cmdName);
        try {
            updater.invoke(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("step count updater done.");
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    public StepCountUpdater(String command) {
        super((command == null) ? command : "wdkUpdateStepCount",
                "Update the result count for valid steps");
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

        String projectIds = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = projectIds.split(",");
        for (String projectId : projects) {
            logger.info("Update step counts for project " + projectId);
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            updateCount(wdkModel);
        }
    }

    private void updateCount(WdkModel wdkModel) throws WdkUserException,
            SQLException, WdkModelException, JSONException,
            NoSuchAlgorithmException {
        List<Integer> users = getUserIds(wdkModel);
        int count = 0;
        for (int userId : users) {
            User user = wdkModel.getUserFactory().getUser(userId);
            Map<Integer, Step> steps = user.getStepsMap();
            for (Step step : steps.values()) {
                // no need to load invalid steps
                if (!step.isValid()) continue;

                try {
                    step.getResultSize();
                } catch (Exception ex) {
                    step.setValid(false);
                } finally {
                    step.update(false);
                }

                count++;
                if (count % 100 == 0)
                    logger.debug("updated the counts of " + count + " steps.");
            }
        }
    }

    private List<Integer> getUserIds(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDb = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDb.getUserSchema();
        String wdkSchema = userDb.getWdkEngineSchema();
        String sql = "SELECT DISTINCT u.user_id FROM " + userSchema
                + "users u, " + userSchema + "steps s, " + wdkSchema
                + "answers a "
                + " WHERE u.is_guest = true AND u.user_id = s.user_id "
                + " AND s.is_deleted = 0 AND s.answer_id = a.answer_id "
                + " AND a.project_id = '" + wdkModel.getProjectId() + "'";
        List<Integer> users = new ArrayList<Integer>();
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql);
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                users.add(userId);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        return users;
    }
}
