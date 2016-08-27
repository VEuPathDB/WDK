package org.gusdb.wdk.model.fix;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class StepParamFilterModifier {

  // constants controlling behavior
  private static final int MAX_QUEUE_SIZE = 30;
  private static final int NUM_THREADS = 20;
  private static final int BATCH_COMMIT_SIZE = 100;
  private static final boolean INCLUDE_GUEST_USER_STEPS = false;

  public static interface StepParamFilterModifierPlugin {
    public TwoTuple<Boolean, StepData> processStep(StepData nextStep, WdkModel wdkModel);
  }

  private static class Config {
    public String projectId;
    public String pluginClassName;
    public StepParamFilterModifierPlugin plugin;
  }

  public static void main(String[] args) throws WdkModelException {
    Config config = parseArgs(args);
    WdkModel model = null;
    try {
      model = WdkModel.construct(config.projectId, GusHome.getGusHome());
      StepParamFilterModifier modifier = new StepParamFilterModifier(config.plugin, model);
      modifier.run();
    }
    finally {
      if (model != null) model.releaseResources();
    }
  }

  private static Config parseArgs(String[] args) {
    if (args.length != 2) {
      System.err.println(NL + "USAGE: StepParamFilterModifier <projectId> <plugin_class_name>" + NL);
      System.exit(1);
    }
    Config config = new Config();
    config.projectId = args[0];
    config.pluginClassName = args[1];
    try {
      @SuppressWarnings("unchecked")
      Class<? extends StepParamFilterModifierPlugin> pluginClass =
          (Class<? extends StepParamFilterModifierPlugin>) Class.forName(config.pluginClassName);
      StepParamFilterModifierPlugin plugin = pluginClass.newInstance();
      config.plugin = plugin;
    }
    catch (ClassCastException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to instantiate plugin class '" + config.pluginClassName + "'", e);
    }
    return config;
  }

  private final WdkModel _wdkModel;
  private final StepParamFilterModifierPlugin _plugin;

  public StepParamFilterModifier(StepParamFilterModifierPlugin plugin, WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _plugin = plugin;
  }

  @SuppressWarnings("serial")
  private static class StepQueue extends ConcurrentLinkedDeque<StepData> {
    public void pushStep(StepData step) {
      add(step);
    }
    public StepData popStep() {
      return pollFirst();
    }
  }

  private void run() {
    List<StepHandler> threads = new ArrayList<>();
    try {
      DatabaseInstance userDb = _wdkModel.getUserDb();
      StepQueue stepQueue = new StepQueue();
  
      // create and start threads that will listen to queue and pull off steps to process
      for (int i = 0; i < NUM_THREADS; i++) {
        StepHandler thread = new StepHandler(i + 1, stepQueue, _plugin, _wdkModel);
        threads.add(thread);
        thread.start();
      }
  
      // execute quepop();ry to read all steps from DB and submit them to handler threads
      new SQLRunner(userDb.getDataSource(), StepData.getAllStepsSql(getUserSchema(_wdkModel), INCLUDE_GUEST_USER_STEPS))
          .executeQuery(new StepDistributor(_wdkModel, stepQueue));
  
      // wait for queue to empty
      while (!stepQueue.isEmpty()) { /* wait */ }

    }
    finally {
      if (!threads.isEmpty()) {

        // tell threads to commit their changes even if batches not full
        for (StepHandler thread : threads) {
          thread.commitAndFinish();
        }
    
        // wait for threads to finish
        while (!allThreadsFinished(threads)) { /* wait */ }

      }
    }
  }

  private static String getUserSchema(WdkModel wdkModel) {
    return wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  private boolean allThreadsFinished(List<StepHandler> threads) {
    for (StepHandler thread : threads) {
      if (!thread.isFinished()) {
        return false;
      }
    }
    return true;
  }

  private static class StepDistributor implements ResultSetHandler {

    private final WdkModel _wdkModel;
    private final StepQueue _stepQueue;

    public StepDistributor(WdkModel wdkModel, StepQueue stepQueue) {
      _wdkModel = wdkModel;
      _stepQueue = stepQueue;
    }

    @Override
    public void handleResult(ResultSet rs) throws SQLException {
      DBPlatform platform = _wdkModel.getUserDb().getPlatform();
      while (rs.next()) {
        while (_stepQueue.size() >= MAX_QUEUE_SIZE) { /* wait */ }
        _stepQueue.pushStep(new StepData(rs, platform));
      }
    }
  }

  private static class StepHandler extends Thread {

    private static final Logger LOG = Logger.getLogger(StepHandler.class);

    private final int _threadId;
    private final StepQueue _stepQueue;
    private final StepParamFilterModifierPlugin _plugin;
    private final WdkModel _wdkModel;
    private final DatabaseInstance _userDb;

    private final AtomicBoolean _isFinished = new AtomicBoolean(false);
    private final AtomicBoolean _commitAndFinishFlag = new AtomicBoolean(false);

    public StepHandler(int threadId, StepQueue stepQueue, StepParamFilterModifierPlugin plugin, WdkModel wdkModel) {
      _threadId = threadId;
      _stepQueue = stepQueue;
      _plugin = plugin;
      _wdkModel = wdkModel;
      _userDb = _wdkModel.getUserDb();
    }

    public void commitAndFinish() {
      _commitAndFinishFlag.set(true);
    }

    public boolean isFinished() {
      return _isFinished.get();
    }

    private void log(String str) {
      LOG.info("Thread " + _threadId + ": " + str);
    }

    @Override
    public void run() {
      log("Ready");
      int numProcessedSteps = 0;
      int numModifiedSteps = 0;
      List<StepData> modifiedSteps = new ArrayList<>();
      while (!_commitAndFinishFlag.get()) {
        StepData nextStep = _stepQueue.popStep();
        if (nextStep != null) {
          numProcessedSteps++;
          TwoTuple<Boolean, StepData> result = _plugin.processStep(nextStep, _wdkModel);
          if (result.getFirst()) {
            // step has been modified
            modifiedSteps.add(result.getSecond());
            numModifiedSteps++;
          }
          if (modifiedSteps.size() >= BATCH_COMMIT_SIZE) {
            updateSteps(modifiedSteps);
          }
          if (numProcessedSteps % 1000 == 0) log("Processed " + numProcessedSteps);
          //if (numModifiedSteps > 0 && numModifiedSteps % 100 == 0) log("Modified " + numModifiedSteps);
        }
      }
      updateSteps(modifiedSteps);
      log("Shutting down. Processed " + numProcessedSteps + ", Modified " + numModifiedSteps);
      _isFinished.set(true);
    }

    private void updateSteps(final List<StepData> modifiedSteps) {
      // need to construct and argument batch around modified step list
      ArgumentBatch modifiedStepBatch = new ArgumentBatch() {

        @Override
        public Iterator<Object[]> iterator() {
          return transform(modifiedSteps.iterator(),
              new Function<StepData, Object[]>() {
                @Override public Object[] apply(StepData obj) {
                  return obj.toUpdateVals();
                }
              });
        }

        @Override
        public int getBatchSize() {
          return BATCH_COMMIT_SIZE;
        }

        @Override
        public Integer[] getParameterTypes() {
          return StepData.UPDATE_PARAMETER_TYPES;
        }

      };

      new SQLRunner(_userDb.getDataSource(), StepData.getUpdateStepSql(
          getUserSchema(_wdkModel)), true).executeUpdateBatch(modifiedStepBatch);

      modifiedSteps.clear();
    }
  }
}
