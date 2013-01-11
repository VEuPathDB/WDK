/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wsf.util.BaseCLI;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * @author xingao
 * 
 */
public class StepCountUpdater extends BaseCLI {

    private static final String ARG_COMPLETE_ID_FILE = "completeIdFile";
    private static final int RUNNER_COUNT = 10;

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
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("step count updater done.");
            System.exit(0);
        }
    }

    private class StepRunner extends Thread {
        private StepCountUpdater updater;
        private boolean finished = false;

        public StepRunner(StepCountUpdater updater) {
            this.updater = updater;
        }

        @Override
        public void run() {
            while (true) {
                int userId = updater.getUserId();
                if (userId == 0) break;

                String count = updater.getUserCount() + "/" + totalUsers;
                logger.info("process steps for user #" + userId + " - " + count);

                for (WdkModel wdkModel : updater.wdkModels) {
                    try {
                        User user = wdkModel.getUserFactory().getUser(userId);
                        updateSteps(wdkModel, user);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            }
            finished = true;
        }

        private void updateSteps(WdkModel wdkModel, User user)
                throws WdkUserException, NoSuchAlgorithmException,
                WdkModelException, SQLException, JSONException {
            Map<Integer, Step> steps = user.getStepsMap();
            for (Step step : steps.values()) {
                int internalId = step.getInternalId();
                if (completedSteps.contains(internalId)) continue;

                // just need to process valid steps
                if (step.isValid()) {
                    try {
                        step.getResultSize();
                    } catch (Exception ex) {
                        step.setValid(false);
                    } finally {
                        step.update(false);
                    }
                }
                updater.recordStep(internalId);
            }
        }
    }

    private PrintWriter writer;
    private Set<Integer> completedSteps;
    private Stack<Integer> userIds;
    private WdkModel[] wdkModels;
    private int totalUsers;

    /**
     * @param command
     * @param description
     */
    public StepCountUpdater(String command) {
        super((command != null) ? command : "wdkUpdateStepCount",
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
        addSingleValueOption(ARG_COMPLETE_ID_FILE, true, null,
                "a file to store the step ids that have been processed.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String idFileName = (String) getOptionValue(ARG_COMPLETE_ID_FILE);
        File idFile = new File(idFileName);
        if (!idFile.exists()) idFile.createNewFile();

        loadCompletedSteps(idFile);
        loadModels();
        loadUserIds(wdkModels[0]);

        this.writer = new PrintWriter(new FileWriter(idFile, true));

        StepRunner[] runners = new StepRunner[RUNNER_COUNT];
        try {
            for (int i = 0; i < RUNNER_COUNT; i++) {
                StepRunner runner = new StepRunner(this);
                runner.start();
                runners[i] = runner;
            }
            boolean finished = false;
            while (!finished) {
                finished = true;
                for (StepRunner runner : runners) {
                    if (!runner.finished) {
                        finished = false;
                        break;
                    }
                }
                if (!finished) Thread.sleep(1000);
            }
        } finally {
            writer.close();
        }
    }

    private void loadCompletedSteps(File idFile) throws NumberFormatException,
            IOException {
        completedSteps = new HashSet<Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(idFile));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            completedSteps.add(Integer.parseInt(line));
        }
        reader.close();
    }

    private void loadUserIds(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        ModelConfigUserDB userDb = wdkModel.getModelConfig().getUserDB();
        String userSchema = userDb.getUserSchema();
        String sql = "SELECT DISTINCT u.user_id FROM " + userSchema
                + "users u, " + userSchema + "steps s "
                + " WHERE u.is_guest = 0 AND u.user_id = s.user_id "
                + " AND s.is_deleted = 0";
        this.userIds = new Stack<Integer>();
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                    "wdk-select-users");
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                userIds.push(userId);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        this.totalUsers = userIds.size();
    }

    private void loadModels() throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String projectIds = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = projectIds.split(",");

        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        List<WdkModel> models = new ArrayList<WdkModel>();
        for (String projectId : projects) {
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);
            models.add(wdkModel);
        }
        this.wdkModels = new WdkModel[models.size()];
        models.toArray(this.wdkModels);
    }

    synchronized boolean isCompleted(int stepId) {
        return completedSteps.contains(stepId);
    }

    synchronized int getUserId() {
        return userIds.isEmpty() ? 0 : userIds.pop();
    }

    synchronized void recordStep(int stepId) {
        completedSteps.add(stepId);
        writer.println(stepId);
        writer.flush();
    }

    synchronized int getUserCount() {
        return userIds.size();
    }
}
