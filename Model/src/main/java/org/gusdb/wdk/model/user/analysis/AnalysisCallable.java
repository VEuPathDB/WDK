package org.gusdb.wdk.model.user.analysis;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.user.Step;

public class AnalysisCallable implements Callable<ExecutionStatus> {

  private static final Logger LOG = Logger.getLogger(AnalysisCallable.class);

  private static class ExecutionData {
    public ExecutionStatus status;
    public String charData;
    public byte[] binaryData;
    public boolean updateStatus;
    public ExecutionData(ExecutionStatus status, String charData, byte[] binaryData, boolean updateStatus) {
      this.status = status;
      this.charData = charData;
      this.binaryData = binaryData;
      this.updateStatus = updateStatus;
    }
  }

  private final StepAnalysisInstance _context;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;

  public AnalysisCallable(StepAnalysisInstance instance, StepAnalysisDataStore dataStore, StepAnalysisFileStore fileStore) {
    _context = instance;
    _dataStore = dataStore;
    _fileStore = fileStore;
  }

  @Override
  public ExecutionStatus call() throws Exception {
    String contextHash = _context.createHash();
    try {
      // update database that the thread is running
      _dataStore.updateExecution(contextHash, ExecutionStatus.RUNNING, new Date(), null, null);

      ExecutionData executionData = runAnalysis(contextHash);

      // status completed successfully or was interrupted
      if (executionData.updateStatus) {
        _dataStore.updateExecution(contextHash, executionData.status, new Date(), executionData.charData, executionData.binaryData);
      }

      return executionData.status;
    }
    catch (Exception e) {
      LOG.error("Step Analysis run failed.", e);
      _dataStore.updateExecution(contextHash, ExecutionStatus.ERROR, new Date(), FormatUtil.getStackTrace(e), null);
      return ExecutionStatus.ERROR;
    }
  }

  private ExecutionData runAnalysis(String contextHash) throws WdkModelException, WdkUserException {
    // create step analysis instance and run
    StepAnalyzer analyzer = StepAnalysisFactoryImpl.getConfiguredAnalyzer(_context, _fileStore);
    RunnableObj<Step> runnableStep = _context.getStep().getRunnable()
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
    StepAnalysisInstance currentContext = _dataStore.getAnalysisById(_context.getAnalysisId(), _fileStore);
    if (currentContext.getStatus().equals(ExecutionStatus.EXPIRED)) {
      // this context was marked expired before it completed running (possibly by another server);
      // must ignore results and go with that determination
      return new ExecutionData(ExecutionStatus.EXPIRED, null, null, false);
    }

    if (!status.isTerminal()) {
      // illegal status returned from plugin; set to ERROR
      throw new WdkModelException("Step Analysis Plugin " + _context.getStepAnalysis().getName() +
          " returned illegal status " + status + " when running instance with ID " +
          _context.getAnalysisId());
    }

    return new ExecutionData(status, null, null, true);
  }
}
