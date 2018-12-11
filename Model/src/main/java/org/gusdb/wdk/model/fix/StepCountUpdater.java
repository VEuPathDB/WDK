package org.gusdb.wdk.model.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.AutoCloseableList;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.runtime.ThreadUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao
 * 
 */
public class StepCountUpdater extends BaseCLI {

  private static final String ARG_COMPLETE_ID_FILE = "completeIdFile";
  private static final int RUNNER_COUNT = 10;

  private static final Logger logger = Logger.getLogger(StepCountUpdater.class);

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    StepCountUpdater updater = new StepCountUpdater(cmdName);
    try {
      updater.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public StepCountUpdater(String command) {
    super((command != null) ? command : "wdkUpdateStepCount", "Update the result count for valid steps");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null,
        "A comma-separated" + " list of ProjectIds, which should match the directory name" +
            " under $GUS_HOME, where model-config.xml is stored.");
    addSingleValueOption(ARG_COMPLETE_ID_FILE, true, null,
        "a file to store the step ids that have been processed.");
  }

  @Override
  protected void execute() throws Exception {

    String idFileName = (String) getOptionValue(ARG_COMPLETE_ID_FILE);
    Set<Long> completedSteps = loadCompletedSteps(idFileName);

    String projectIdStr = (String) getOptionValue(ARG_PROJECT_ID);
    String[] projectIds = projectIdStr.trim().split(",");

    try (AutoCloseableList<WdkModel> wdkModels = WdkModel.loadMultipleModels(GusHome.getGusHome(), projectIds);
         PrintWriter writer = new PrintWriter(new FileWriter(idFileName, true))) {

      Stack<Long> userIds = loadUserIds(wdkModels.get(0));
      SharedData sharedData = new SharedData(completedSteps, userIds, wdkModels, writer);

      // create runner threads and start
      StepRunner[] runners = new StepRunner[RUNNER_COUNT];
      for (int i = 0; i < RUNNER_COUNT; i++) {
        StepRunner runner = new StepRunner(sharedData);
        runner.start();
        runners[i] = runner;
      }

      // wait for runners to finish
      boolean finished = false;
      while (!finished) {
        finished = true;
        for (StepRunner runner : runners) {
          if (!runner.isFinished()) {
            finished = false;
            break;
          }
        }
        if (!finished) {
          if (ThreadUtil.sleep(1000)) {
            // FIXME: should interrupt child threads from here, wait for them to shut down, then exit
            System.err.println("Interruption detected, but must wait for child threads to finish.");
          }
        }
      }
    }
  }

  private static Set<Long> loadCompletedSteps(String idFileName) throws NumberFormatException, IOException {
    Set<Long> completedSteps = new HashSet<>();
    File idFile = new File(idFileName);
    if (!idFile.exists()) {
      idFile.createNewFile();
      return completedSteps;
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(idFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0)
          continue;
        completedSteps.add(Long.parseLong(line));
      }
    }
    return completedSteps;
  }

  private static Stack<Long> loadUserIds(WdkModel wdkModel) throws SQLException {
    ModelConfigUserDB userDb = wdkModel.getModelConfig().getUserDB();
    String userSchema = userDb.getUserSchema();
    String sql = "SELECT DISTINCT u.user_id FROM " + userSchema + "users u, " + userSchema + "steps s " +
        " WHERE u.is_guest = 0 AND u.user_id = s.user_id " + " AND s.is_deleted = 0";
    Stack<Long> userIds = new Stack<>();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, "wdk-select-users");
      while (resultSet.next()) {
        long userId = resultSet.getLong("user_id");
        userIds.push(userId);
      }
      return userIds;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  private static class SharedData {

    private final PrintWriter _writer;
    private final Set<Long> _completedSteps;
    private final Stack<Long> _userIds;
    private final List<WdkModel> _wdkModels;
    private final int _totalUsers;

    public SharedData(Set<Long> completedSteps, Stack<Long> userIds, List<WdkModel> wdkModels, PrintWriter writer) {
      _completedSteps = completedSteps;
      _userIds = userIds;
      _totalUsers = userIds.size();
      _wdkModels = wdkModels;
      _writer = writer;
    }

    public synchronized boolean isCompleted(long stepId) {
      return _completedSteps.contains(stepId);
    }

    public synchronized long getUserId() {
      return _userIds.isEmpty() ? 0 : _userIds.pop();
    }

    public synchronized void recordStep(long stepId) {
      _completedSteps.add(stepId);
      _writer.println(stepId);
    }

    public synchronized int getUserCount() {
      return _userIds.size();
    }

    public int getTotalUsers() {
      return _totalUsers;
    }

    public List<WdkModel> getWdkModels() {
      return _wdkModels;
    }
  }

  private static class StepRunner extends Thread {

    private SharedData _sharedData;
    private AtomicBoolean _isFinished = new AtomicBoolean(false);

    public StepRunner(SharedData sharedData) {
      _sharedData = sharedData;
    }

    public boolean isFinished() {
      return _isFinished.get();
    }

    @Override
    public void run() {
      List<WdkModel> wdkModels = _sharedData.getWdkModels();
      while (true) {
        try {
          long userId = _sharedData.getUserId();
          if (userId == 0) {
            _isFinished.set(true);
            break; // no more users in stack
          }
          String count = _sharedData.getUserCount() + "/" + _sharedData.getTotalUsers();
          logger.info("process steps for user #" + userId + " - " + count);

          for (WdkModel wdkModel : wdkModels) {
            updateSteps(wdkModel.getUserFactory().getUserById(userId)
                .orElseThrow(() -> new WdkModelException("Cannot find user with ID " + userId)));
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
          _isFinished.set(true);
          throw new RuntimeException(ex);
        }
      }
    }

    private void updateSteps(User user) throws WdkModelException {
      Map<Long, Step> steps = StepUtilities.getStepsMap(user);
      for (Step step : steps.values()) {
        long stepId = step.getStepId();
        if (_sharedData.isCompleted(stepId))
          continue;

        // just need to process valid steps
        if (step.isValid()) {
          try {
            step.getResultSize();
          }
          catch (Exception ex) {
            // don't need to update DB here since updating in finally
            //step.setValidFlag(false); // RRD 7/23/18, all validity is runtime checked now...
          }
          finally {
            step.writeMetadataToDb(false);
          }
        }
        _sharedData.recordStep(stepId);
      }
    }
  }
}
