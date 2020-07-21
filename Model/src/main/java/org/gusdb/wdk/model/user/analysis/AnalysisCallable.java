package org.gusdb.wdk.model.user.analysis;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.user.Step;

public class AnalysisCallable implements Callable<ExecutionStatus> {

  private static final Logger LOG = Logger.getLogger(AnalysisCallable.class);

  /**
   * Small private container to encapsulate info associated with an execution
   * should NOT be used outside this class.  See ExecutionResult for a similar
   * public class
   */
  private static class ExecutionData {
    public ExecutionStatus status;
    public String charData;
    public byte[] binaryData;
    public boolean executionRowNeedsUpdating;
    public ExecutionData(ExecutionStatus status, String charData, byte[] binaryData, boolean executionRowNeedsUpdating) {
      this.status = status;
      this.charData = charData;
      this.binaryData = binaryData;
      this.executionRowNeedsUpdating = executionRowNeedsUpdating;
    }
  }

  private final RunnableObj<StepAnalysisInstance> _instance;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;

  public AnalysisCallable(RunnableObj<StepAnalysisInstance> instance, StepAnalysisDataStore dataStore, StepAnalysisFileStore fileStore) {
    _instance = instance;
    _dataStore = dataStore;
    _fileStore = fileStore;
  }

  @Override
  public ExecutionStatus call() throws Exception {
    String contextHash = StepAnalysisInstance.getContextHash(_instance);
    try {
      // update database that the thread is running
      _dataStore.updateExecution(contextHash, ExecutionStatus.RUNNING, new Date(), null, null);

      ExecutionData executionData = runAnalysis(contextHash);

      // status completed successfully or was interrupted
      if (executionData.executionRowNeedsUpdating) {
        _dataStore.updateExecution(contextHash, executionData.status, new Date(), executionData.charData, executionData.binaryData);
        executionData.executionRowNeedsUpdating = false;
      }

      return executionData.status;
    }
    catch (Exception e) {
      LOG.error("Step Analysis run failed.", e);
      _dataStore.updateExecution(contextHash, ExecutionStatus.ERROR, new Date(), FormatUtil.getStackTrace(e), null);
      return ExecutionStatus.ERROR;
    }
  }

  private ExecutionData runAnalysis(String contextHash) throws WdkModelException {
    // create step analysis instance and run
    StepAnalyzer analyzer = StepAnalysisFactoryImpl.getConfiguredAnalyzer(_instance, _fileStore);
    RunnableObj<Step> runnableStep = _instance.get().getStep().getRunnable()
        // cannot analyze unrunnable step
        .getOrThrow(step -> new WdkModelException(
            "Request made to analyze unrunnable step with ID " + step.getStepId() +
            ", validation: " + step.getValidationBundle().toString(2)));

    // otherwise, step is runnable
    ExecutionStatus status = analyzer.runAnalysis(
        AnswerValueFactory.makeAnswer(runnableStep).cloneWithNewPaging(1, -1),
        new StatusLogger(contextHash, _dataStore));

    LOG.info("Analyzer returned without exception and with status: " + status);

    // return runtime data if successful completion
    if (status.equals(ExecutionStatus.COMPLETE)) {
      return new ExecutionData(status, analyzer.getPersistentCharData(), analyzer.getPersistentBinaryData(), true);
    }

    // check to see if DB status changed out from under us (via monitor thread)
    ExecutionInfo info = _dataStore.getAnalysisStatus(contextHash)
        .orElseThrow(() -> new WdkModelException("Execution can no longer be found.  It's possible the cache was cleared during this run."));
    if (info.getStatus().equals(ExecutionStatus.EXPIRED)) {
      // this context was marked expired before it completed running (possibly by another server);
      // must ignore results and go with that determination
      return new ExecutionData(ExecutionStatus.EXPIRED, null, null, false);
    }

    if (!status.isTerminal()) {
      // illegal status returned from plugin; set to ERROR
      throw new WdkModelException("Step Analysis Plugin " + _instance.get().getAnalysisName() +
          " returned illegal status " + status + " when running instance with ID " +
          _instance.get().getAnalysisId() + ".  Status must be terminal.");
    }

    return new ExecutionData(status, null, null, true);
  }
}
