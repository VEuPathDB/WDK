package org.gusdb.wdk.model.user.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkResourceChecker;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ExecutionConfig;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ViewConfig;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.user.Step;

/**
Static/Stateless Data:
  StepAnalysisPlugins XML
  StepAnalysisXml XML
  StepAnalysisRef XML
  StepAnalysis Interface for loaded Model- defines analyses than can be run
  StepAnalyzer worker interface

StepAnalysisFactory maintains:
  - StepAnalysisConfigs in UserDB
  - Current threadpool for executions
  - Results cache

StepAnalysisConfig ref to analysis and params, passed to StepAnalyzer
  
Things to maintain:
  - valid step + analysis name + version + props + params = def to execution
  - cache of execDef -> status/results
*/
public class StepAnalysisFactory {
  
  private static Logger LOG = Logger.getLogger(StepAnalysisFactory.class);
  
  public static class AnalysisResult {
    public ExecutionStatus status;
    public String serializedResult;
    public String statusLog;
    public Object analysisViewModel;
    public AnalysisResult(ExecutionStatus status, String serializedResult, String statusLog) {
      this.status = status;
      this.serializedResult = serializedResult;
      this.statusLog = statusLog;
    }
  }
  
  private final ViewConfig _viewConfig;
  private final ExecutionConfig _execConfig;
  private final StepAnalysisDataStore _dataStore;
  private final ExecutorService _threadPool;
  private final Future<Boolean> _threadMonitor;
  private volatile ConcurrentLinkedDeque<Future<ExecutionStatus>> _threadResults;
  
  public StepAnalysisFactory(WdkModel wdkModel) {
    _viewConfig = wdkModel.getStepAnalysisPlugins().getViewConfig();
    _execConfig = wdkModel.getStepAnalysisPlugins().getExecutionConfig();
    _dataStore = new StepAnalysisInMemoryDataStore(wdkModel);
    _threadPool = Executors.newFixedThreadPool(_execConfig.getThreadPoolSize());
    _threadResults = new ConcurrentLinkedDeque<>();
    _threadMonitor = _threadPool.submit(new FutureCleaner(_threadResults));
  }

  public Map<Integer, StepAnalysisContext> getAppliedAnalyses(Step step) throws WdkModelException {
    return _dataStore.getAnalysesByStepId(step.getStepId());
  }

  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException {
    List<String> errors = context.getStepAnalysis().getAnalyzerInstance()
        .validateFormParams(context.getFormParams());
    if (errors == null) errors = new ArrayList<String>();
    return errors;
  }
  
  public void applyAnalysis(StepAnalysisContext context) throws WdkModelException {
    // create new execution instance
    int saId = _dataStore.getNextId();
    _dataStore.insertAnalysis(saId, context.getStep().getStepId(),
        context.getDisplayName(), context.serializeContext());
    context.setAnalysisId(saId);
    boolean created = _dataStore.insertExecution(context.createHash());
    if (!created) {
      // result is being or has already been generated
      return;
    }
    executeAnalysis(context);
  }

  private void executeAnalysis(final StepAnalysisContext context) throws WdkModelException {
    try {
      _threadResults.add(_threadPool.submit(new AnalysisCallable(context, _dataStore)));
    }
    catch (RejectedExecutionException e) {
      throw new WdkModelException("Unable to create new step analysis execution.  Thread pool exhausted?", e);
    }
  }
  
  public AnalysisResult getAnalysisResult(int analysisId) throws WdkUserException, WdkModelException {
    StepAnalysisContext context = _dataStore.getAnalysisById(analysisId);
    if (context == null) {
      throw new WdkUserException("Cannot find context associated with analysis ID " + analysisId + ".");
    }
    String hash = context.createHash();
    AnalysisResult result = _dataStore.getAnalysisResult(hash);
    if (result == null) {
      throw new WdkModelException("Cannot find analysis result associated with hash " + hash + ".");
    }
    StepAnalyzer analyzer = context.getStepAnalysis().getAnalyzerInstance();
    analyzer.deserializeResults(result.serializedResult);
    result.analysisViewModel = analyzer.getAnalysisViewModel();
    result.serializedResult = null;
    return result;
  }

  public void deleteAnalysis(int analysisId) throws WdkModelException {
    _dataStore.deleteAnalysis(analysisId);
  }

  public String resolveFormView(WdkResourceChecker resourceChecker, StepAnalysisContext context) throws WdkModelException {
    // first try to resolve name with prefix/suffix
    return resolveView(resourceChecker, context, context.getStepAnalysis().getFormViewName(), "form");
  }

  public String resolveResultsView(WdkResourceChecker resourceChecker, StepAnalysisContext context) throws WdkModelException {
    // first try to resolve name with prefix/suffix
    return resolveView(resourceChecker, context, context.getStepAnalysis().getAnalysisViewName(), "analysis");
  }
  
  private String resolveView(WdkResourceChecker resourceChecker, StepAnalysisContext context,
      String viewName, String viewType) throws WdkModelException {

    String fixedName =
        (_viewConfig.getPrefix() != null ? _viewConfig.getPrefix() : "") +
        viewName +
        (_viewConfig.getSuffix() != null ? _viewConfig.getSuffix() : "");
    
    String resolvedView =
        resourceChecker.wdkResourceExists(fixedName) ? fixedName :
        resourceChecker.wdkResourceExists(viewName) ? viewName : null;
    
    if (resolvedView == null) {
      throw new WdkModelException("StepAnalysis " + viewType + " view [" +
          context.getStepAnalysis().getAnalysisViewName() + "] configured for step " +
          "analysis plugin [" + context.getStepAnalysis().getName() + "] for question [" +
          context.getQuestion().getFullName() + "] cannot be resolved.");
    }
    
    return resolvedView;
  }
  
  public void shutDown() {
    try {
      _threadPool.shutdown();
      LOG.info("Attempting to shut down Step Analysis threads...");
      cancelThread(_threadMonitor, "Step Analysis Thread Monitor");
      for (Future<?> future : _threadResults) {
        cancelThread(future, "Step Analysis Execution");
      }
      LOG.info("Cancelled all step analysis threads.  Waiting for all to complete.");
      // wait 5 more seconds for everything to complete, then move on
      //try {
        for (int i=0; i<25; i++) {
          //Thread.sleep(200);
          if (_threadPool.isTerminated()) {
            LOG.info("All step analysis threads have shut down cleanly.");
            return;
          }
        }
      //} catch(InterruptedException e) {}
      LOG.warn("Not all step analysis threads have shut down cleanly before timeout.");
    } catch (Exception e) {
      LOG.error("Unable to cleanly shut down Step Analysis Factory", e);
    }
  }
  
  private void cancelThread(Future<?> thread, String name) {
    //try {
      _threadMonitor.cancel(true);
      //_threadMonitor.get(10, TimeUnit.MILLISECONDS);
      LOG.info("Successfully shut down " + name);
    //}
    //catch (InterruptedException | ExecutionException | TimeoutException e) {
      // can't really do anything here; just trying to shut down as many as possible
      //LOG.warn("Unable to shut down " + name + " within a reasonable timeout.", e);
    //}
  }
  
  private static class FutureCleaner implements Callable<Boolean> {

    private static final int FUTURE_CLEANUP_INTERVAL_SECS = 20;
    private static final int FUTURE_CLEANER_SLEEP_SECS = 2;
    
    private volatile ConcurrentLinkedDeque<Future<ExecutionStatus>> _threadResults;
    
    public FutureCleaner(ConcurrentLinkedDeque<Future<ExecutionStatus>> threadResults) {
      _threadResults = threadResults;
    }
    
    @Override
    public Boolean call() throws Exception {
      try {
        LOG.info("Step Analysis Thread Monitor initialized and running.");
        int waitSecs = 0;
        while (true) {
          if (waitSecs > FUTURE_CLEANUP_INTERVAL_SECS) {
            for (Future<ExecutionStatus> future : _threadResults) {
              if (future.isDone() || future.isCancelled()) {
                try {
                  LOG.info("Step Analysis completed with status: " + future.get());
                }
                catch (ExecutionException | CancellationException | InterruptedException e) {
                  LOG.error("Exception thrown while retrieving step analysis status (on completion)", e);
                }
                _threadResults.remove(future);
              }
            }
            waitSecs = 0;
          }
          Thread.sleep(FUTURE_CLEANER_SLEEP_SECS * 1000);
          if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
          waitSecs += FUTURE_CLEANER_SLEEP_SECS;
        }
      }
      catch (InterruptedException e) {
        LOG.info("Step Analysis Future cleaner interrupted.  Shutting down.");
        return true;
      }
    }
  }
  
  private static class AnalysisCallable implements Callable<ExecutionStatus> {
    
    private final StepAnalysisContext _context;
    private final StepAnalysisDataStore _dataStore;
    
    public AnalysisCallable(StepAnalysisContext context, StepAnalysisDataStore dataStore) {
      _context = context;
      _dataStore = dataStore;
    }
    
    @Override
    public ExecutionStatus call() throws Exception {
      String contextHash = _context.createHash();
      try {
        // update database that we are running
        _dataStore.updateExecution(contextHash, ExecutionStatus.RUNNING, "");
    
        // create step analysis instance and run
        StepAnalyzer analyzer = _context.getStepAnalysis().getAnalyzerInstance();
        analyzer.setFormParams(_context.getFormParams());
        ExecutionStatus status = analyzer.runAnalysis(
            _context.getStep().getAnswerValue(), new StatusLogger(contextHash, _dataStore));
      
        // status completed successfully or was interrupted
        String result = (status.equals(ExecutionStatus.COMPLETE) ? analyzer.serializeResults() : "");
        _dataStore.updateExecution(contextHash, status, result);
        return status;
      }
      catch (Exception e) {
        LOG.error("Step Analysis failed.", e);
        _dataStore.updateExecution(contextHash,ExecutionStatus.ERROR, FormatUtil.getStackTrace(e));
        return ExecutionStatus.ERROR;
      }
    }
  }
}
