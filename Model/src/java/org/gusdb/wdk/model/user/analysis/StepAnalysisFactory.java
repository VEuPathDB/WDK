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
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ExecutionConfig;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.user.Step;

/**
 * Static/Stateless Data:
 *   StepAnalysisPlugins: XML wrapper
 *   StepAnalysisXml: XML wrapper
 *   StepAnalysisRef: XML wrapper
 *   StepAnalysis: interface for loaded analysis refs
 *   StepAnalyzer: interface for worker class
 *
 * StepAnalysisFactory maintains:
 *   - StepAnalysisContexts in UserDB
 *   - Threadpool for executions
 *   - Results cache
 *
 * StepAnalysisContext:
 *
 * Things to maintain:
 *   - valid step + analysis name + version + props + params = def to execution
 *   - cache of execDef -> status/results
 */
public class StepAnalysisFactory {
  
  private static Logger LOG = Logger.getLogger(StepAnalysisFactory.class);
  
  private static final boolean USE_PERSISTENCE = false;
  
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
  
  private final ExecutionConfig _execConfig;
  private final StepAnalysisViewResolver _viewResolver;
  private final StepAnalysisDataStore _dataStore;
  private final ExecutorService _threadPool;
  private final Future<Boolean> _threadMonitor;
  private volatile ConcurrentLinkedDeque<Future<ExecutionStatus>> _threadResults;
  
  public StepAnalysisFactory(WdkModel wdkModel) {
    _viewResolver = new StepAnalysisViewResolver(wdkModel.getStepAnalysisPlugins().getViewConfig());
    _execConfig = wdkModel.getStepAnalysisPlugins().getExecutionConfig();
    _dataStore = (USE_PERSISTENCE ?
        new StepAnalysisPersistentDataStore(wdkModel) :
        new StepAnalysisInMemoryDataStore(wdkModel));
    _threadPool = Executors.newFixedThreadPool(_execConfig.getThreadPoolSize());
    _threadResults = new ConcurrentLinkedDeque<>();
    _threadMonitor = _threadPool.submit(new FutureCleaner(_threadResults));
  }

  public List<StepAnalysisContext> getAllAnalyses() throws WdkModelException {
    return _dataStore.getAllAnalyses();
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

  public StepAnalysisContext createAnalysis(StepAnalysisContext context) throws WdkModelException {
    // create new execution instance
    int saId = _dataStore.getNextId();
    _dataStore.insertAnalysis(saId, context.getStep().getStepId(),
        context.getDisplayName(), context.serializeContext());
    // override any previous values for id and status; this is a -new- analysis
    context.setAnalysisId(saId);
    context.setStatus(ExecutionStatus.CREATED);
    return context;
  }
  
  public StepAnalysisContext runAnalysis(StepAnalysisContext context)
      throws WdkModelException {
    ExecutionStatus initialStatus = ExecutionStatus.PENDING;
    // first, update stored context with params
    _dataStore.updateContext(context.getAnalysisId(), context.serializeContext());
    boolean created = _dataStore.insertExecution(context.createHash(), initialStatus);
    if (context.getStatus().equals(ExecutionStatus.CREATED)) {
      // only need to set new flag if it has not already been set
      _dataStore.setNewFlag(context.getAnalysisId(), false);
    }
    if (created) {
      // no previous results record exists; need to run analysis
      context.setStatus(initialStatus);
      context = executeAnalysis(context);
    }
    else {
      // result is being or has already been generated; get current status
      context.setStatus(_dataStore.getExecutionStatus(context.createHash()));
    }
    return context;
  }

  private StepAnalysisContext executeAnalysis(StepAnalysisContext context) throws WdkModelException {
    try {
      _threadResults.add(_threadPool.submit(new AnalysisCallable(context, _dataStore)));
      return context;
    }
    catch (RejectedExecutionException e) {
      throw new WdkModelException("Unable to create new step analysis execution.  Thread pool exhausted?", e);
    }
  }
  
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException {
    AnalysisResult result = _dataStore.getAnalysisResult(context.createHash());
    if (result == null) {
      LOG.info("No result could be found.  Probably does not yet exist; creating a 'dummy' result.");
      result = new AnalysisResult(ExecutionStatus.CREATED, null, null);
    }
    else {
      LOG.info("Got result back from data store: " + result.status + ", with results:\n" + result.serializedResult);
      StepAnalyzer analyzer = context.getStepAnalysis().getAnalyzerInstance();
      analyzer.deserializeResult(result.serializedResult);
      result.analysisViewModel = analyzer.getAnalysisViewModel();
      result.serializedResult = null;
    }
    return result;
  }

  public void deleteAnalysis(StepAnalysisContext context) throws WdkModelException {
    _dataStore.deleteAnalysis(context.getAnalysisId());
  }

  public void renameContext(StepAnalysisContext context) throws WdkModelException {
    _dataStore.renameAnalysis(context.getAnalysisId(), context.getDisplayName());
  }

  public StepAnalysisContext getSavedContext(int analysisId) throws WdkUserException, WdkModelException{
    StepAnalysisContext context = _dataStore.getAnalysisById(analysisId);
    if (context == null) {
      throw new WdkUserException("No analysis exists with id: " + analysisId);
    }
    return context;
  }
  
  public StepAnalysisViewResolver getViewResolver() {
    return _viewResolver;
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
        String result = (status.equals(ExecutionStatus.COMPLETE) ? analyzer.serializeResult() : "");
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
