package org.gusdb.wdk.model.user.analysis;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.OptionallyInvalid;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.user.Step;

public class AnalysisCallable implements Callable<ExecutionStatus> {

  private static final Logger LOG = Logger.getLogger(AnalysisCallable.class);

  private final StepAnalysisContext _context;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;

  public AnalysisCallable(StepAnalysisContext context, StepAnalysisDataStore dataStore, StepAnalysisFileStore fileStore) {
    _context = context;
    _dataStore = dataStore;
    _fileStore = fileStore;
  }

  @Override
  public ExecutionStatus call() throws Exception {
    String contextHash = _context.createHash();
    try {
      // update database that the thread is running
      _dataStore.updateExecution(contextHash, ExecutionStatus.RUNNING, new Date(), null, null);

      // create step analysis instance and run
      StepAnalyzer analyzer = StepAnalysisFactoryImpl.getConfiguredAnalyzer(_context, _fileStore);
      OptionallyInvalid<RunnableObj<Step>, Step> stepOptions = _context.getStep().getRunnable();
      if (stepOptions.isLeft()) {
        // step is not runnable; cannot analyze unrunnable step
        LOG.warn("Request made to analyze unrunnable step with ID " + _context.getStep().getStepId());
        return ExecutionStatus.ERROR;
      }

      // otherwise, step is runnable
      ExecutionStatus status = analyzer.runAnalysis(
          AnswerValueFactory.makeAnswer(stepOptions.getLeft()).cloneWithNewPaging(1, -1),
          new StatusLogger(contextHash, _dataStore));

      LOG.info("Analyzer returned without exception and with status: " + status);

      // check to see if DB status changed out from under us (via monitor thread)
      StepAnalysisContext currentContext = _dataStore.getAnalysisById(_context.getAnalysisId(), _fileStore);
      if (currentContext.getStatus().equals(ExecutionStatus.EXPIRED)) {
        // this context was marked expired before it completed running (possibly by another server);
        // must ignore results and go with that determination
        return ExecutionStatus.EXPIRED;
      }

      if (status == null || !status.isTerminal()) {
        // illegal status returned from plugin; set to ERROR
        LOG.error("Step Analysis Plugin " + _context.getStepAnalysis().getName() +
            " returned illegal status " + status + " when running instance with ID " +
            _context.getAnalysisId());
        status = ExecutionStatus.ERROR;
      }

      // status completed successfully or was interrupted
      String charData = (status.equals(ExecutionStatus.COMPLETE) ? analyzer.getPersistentCharData() : "");
      byte[] binData = (status.equals(ExecutionStatus.COMPLETE) ? analyzer.getPersistentBinaryData() : null);
      _dataStore.updateExecution(contextHash, status, new Date(), charData, binData);
      return status;
    }
    catch (Exception e) {
      LOG.error("Step Analysis failed.", e);
      _dataStore.updateExecution(contextHash, ExecutionStatus.ERROR, new Date(), FormatUtil.getStackTrace(e), null);
      return ExecutionStatus.ERROR;
    }
  }
}
