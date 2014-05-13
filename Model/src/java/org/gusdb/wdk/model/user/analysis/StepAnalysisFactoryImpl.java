package org.gusdb.wdk.model.user.analysis;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.gusdb.wdk.model.analysis.ValidationErrors;
import org.gusdb.wdk.model.answer.AnswerValue;
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
public class StepAnalysisFactoryImpl implements StepAnalysisFactory {
  
  private static Logger LOG = Logger.getLogger(StepAnalysisFactoryImpl.class);
  
  static final boolean USE_DB_PERSISTENCE = false;
  
  private final ExecutionConfig _execConfig;
  private final StepAnalysisViewResolver _viewResolver;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;
  private ExecutorService _threadPool;
  private Future<Boolean> _threadMonitor;
  private volatile ConcurrentLinkedDeque<Future<ExecutionStatus>> _threadResults;
  
  public StepAnalysisFactoryImpl(WdkModel wdkModel) throws WdkModelException {
    _viewResolver = new StepAnalysisViewResolver(wdkModel.getStepAnalysisPlugins().getViewConfig());
    _execConfig = wdkModel.getStepAnalysisPlugins().getExecutionConfig();
    _dataStore = (USE_DB_PERSISTENCE ?
        new StepAnalysisPersistentDataStore(wdkModel) :
        new StepAnalysisInMemoryDataStore(wdkModel));
    _fileStore = new StepAnalysisFileStore(Paths.get(_execConfig.getFileStoreDirectory()));
    startThreadPool();
  }

  @Override
  public List<StepAnalysisContext> getAllAnalyses() throws WdkModelException {
    return _dataStore.getAllAnalyses(_fileStore);
  }

  @Override
  public Map<Integer, StepAnalysisContext> getAppliedAnalyses(Step step) throws WdkModelException {
    return _dataStore.getAnalysesByStepId(step.getStepId(), _fileStore);
  }

  @Override
  public Object getFormViewModel(StepAnalysisContext context) throws WdkModelException {
    return getConfiguredAnalyzer(context, _fileStore).getFormViewModel();
  }

  @Override
  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException {
    ValidationErrors errors = getConfiguredAnalyzer(context, _fileStore)
        .validateFormParams(context.getFormParams());
    List<String> errorList = new ArrayList<String>();
    if (errors == null || !errors.hasMessages()) return errorList;
    errorList.addAll(errors.getMessages());
    // FIXME: figure out display of these values; for now, translate param errors into strings
    for (Entry<String,List<String>> paramErrors : errors.getParamMessages().entrySet()) {
      for (String message : paramErrors.getValue()) {
        errorList.add(paramErrors.getKey() + ": " + message);
      }
    }
    return errorList;
  }

  @Override
  public StepAnalysisContext createAnalysis(StepAnalysisContext context)
      throws WdkModelException, IllegalAnswerValueException {

    // ensure this is a valid step to analyze
    checkStepForValidity(context);
    
    // analysis valid; write analysis to DB
    return writeNewAnalysisContext(context);
  }

  @Override
  public StepAnalysisContext copyContext(StepAnalysisContext context)
      throws WdkModelException {
    StepAnalysisContext copy = StepAnalysisContext.createCopy(context);
    copy.setStatus(ExecutionStatus.CREATED);
    copy.setNew(true);
    copy = writeNewAnalysisContext(copy);
    return copy;
  }

  @Override
  public void copyAnalysisInstances(Step fromStep, Step toStep) throws WdkModelException {
    LOG.info("Request made to copy analysis instances from step " + fromStep.getStepId() + " to " + toStep.getStepId());
    Map<Integer, StepAnalysisContext> fromContexts = _dataStore.getAnalysesByStepId(fromStep.getStepId(), _fileStore);
    for (StepAnalysisContext fromContext : fromContexts.values()) {
      LOG.info("Copying step analysis with ID " + fromContext.getAnalysisId());
      LOG.trace("TRACE: " + fromContext.getInstanceJson());
      StepAnalysisContext toContext = StepAnalysisContext.createCopy(fromContext);
      toContext.setStep(toStep);
      try {
        checkStepForValidity(toContext);
        toContext.setIsValidStep(true);
      }
      catch (IllegalAnswerValueException e) {
        // if answer value of toStep is not valid for the given analysis, mark as
        // such and save; user will not be shown form and will be unable to run analysis
        toContext.setIsValidStep(false, e.getMessage());
      }
      writeNewAnalysisContext(toContext);
      LOG.info("Wrote new duplicate context with ID " + toContext.getAnalysisId() +
          " for revised step " + toContext.getStep().getStepId());
    }
    LOG.info("Completed copy.");
  }

  private StepAnalysisContext writeNewAnalysisContext(StepAnalysisContext context)
      throws WdkModelException {
    // create new execution instance
    int saId = _dataStore.getNextId();
    _dataStore.insertAnalysis(saId, context.getStep().getStepId(), context.getDisplayName(),
        context.isNew(), context.hasParams(), context.getInvalidStepReason(), context.createHash(), context.serializeContext());
    
    // override any previous value for id
    context.setAnalysisId(saId);
    return context;
  }

  private void checkStepForValidity(StepAnalysisContext context)
      throws WdkModelException, IllegalAnswerValueException {
    // ensure this is a valid step to analyze
    AnswerValue answer = context.getStep().getAnswerValue();
    if (answer.getResultSize() == 0) {
      throw new IllegalAnswerValueException("You cannot analyze a Step with zero results.");
    }
    getConfiguredAnalyzer(context, _fileStore).validateAnswerValue(answer);
  }
  
  @Override
  public StepAnalysisContext runAnalysis(StepAnalysisContext context)
      throws WdkModelException {
    ExecutionStatus initialStatus = ExecutionStatus.PENDING;
    String hash = context.createHash();
    boolean newlyCreated = _dataStore.insertExecution(hash, initialStatus, new Date());
    boolean contextModified = false;

    // now that user has run this analysis, set 'not new' if still new
    if (context.isNew()) {
      _dataStore.setNewFlag(context.getAnalysisId(), false);
      context.setNew(false);
      contextModified = true;
    }
    
    // now that this analysis has params, set 'has params' if not yet set
    if (!context.hasParams()) {
      _dataStore.setHasParams(context.getAnalysisId(), true);
      context.setHasParams(true);
      contextModified = true;
    }
    
    // if context was modified, recheck status
    //   TODO: fix logic here to be less ugly/costly
    if (contextModified) {
      try {
        context = getSavedContext(context.getAnalysisId());
      }
      catch (WdkUserException e) {
        throw new WdkModelException("Step Analysis was deleted mid-request!", e);
      }
    }
    
    // Run analysis plugin under the following conditions:
    //   1. if new execution was created (i.e. none existed before or cache was cleared)
    //   2. previous run failed due to error, interruption, etc.
    //   3. file system cache has been deleted (by wdkCache or sys admins)
    //
    // NOTE: There is a race condition here in between the check of the store and the creation
    //       (first line inside if).  Use combination of lock and createNewFile() to fix.
    LOG.info("Checking whether to run vs. use previous result. newlyCreated = " +
        newlyCreated + ", status = " + context.getStatus());
    if (newlyCreated || context.getStatus().requiresRerun()) {

      // ensure empty data and file stores and create dir
      _fileStore.recreateStore(hash);
      if (!newlyCreated) {
        _dataStore.resetExecution(hash, initialStatus);
      }
      
      // update stored context with param values
      _dataStore.updateContext(context.getAnalysisId(), hash, context.serializeContext());
      
      // set initial status on in-memory context
      context.setStatus(initialStatus);
      
      // run the analysis
      return executeAnalysis(context);
    }

    // otherwise, no need to run since result already generated or plugin currently running;
    LOG.info("Attempt made to run analysis, but it was unnecessary.  Here's the context at that time:\n" + context.getInstanceJson());
    return context;
  }

  private StepAnalysisContext executeAnalysis(StepAnalysisContext context) throws WdkModelException {
    try {
      _threadResults.add(_threadPool.submit(new AnalysisCallable(context, _dataStore, _fileStore)));
      return context;
    }
    catch (RejectedExecutionException e) {
      throw new WdkModelException("Unable to create new step analysis execution.  Thread pool exhausted?", e);
    }
  }
  
  /**
   * Collects the data associated with a result and returns the aggregating
   * object.  This method is only to be called when a "recent" call to
   * getSavedContext() has status COMPLETE.  No checks are done to ensure that
   * persistent storage mechanisms have not been cleared.
   * 
   * @param context context for this result
   * @return result
   * @throws WdkModelException if inconsistent data is found or other error occurs
   */
  @Override
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException {
    String hash = context.createHash();
    AnalysisResult result = _dataStore.getRawAnalysisResult(hash);
    if (result == null) {
      throw new WdkModelException("Result record not found for context-generated hash: " + hash);
    }
    
    LOG.info("Got result back from data store: " + result.getStatus() + ", with results:\n" + result.getStoredString());
    StepAnalyzer analyzer = getConfiguredAnalyzer(context, _fileStore);
    analyzer.setPersistentCharData(result.getStoredString());
    analyzer.setPersistentBinaryData(result.getStoredBytes());
    result.setResultViewModel(analyzer.getResultViewModel());
    result.clearStoredData(); // only care about the view model
    return result;
  }

  @Override
  public StepAnalysisContext deleteAnalysis(StepAnalysisContext context) throws WdkModelException {
    _dataStore.deleteAnalysis(context.getAnalysisId());
    return context;
  }

  @Override
  public void renameContext(StepAnalysisContext context) throws WdkModelException {
    _dataStore.renameAnalysis(context.getAnalysisId(), context.getDisplayName());
  }

  @Override
  public StepAnalysisContext getSavedContext(int analysisId) throws WdkUserException, WdkModelException {
    StepAnalysisContext context = _dataStore.getAnalysisById(analysisId, _fileStore);
    if (context == null) {
      throw new WdkUserException("No analysis exists with id: " + analysisId);
    }
    return context;
  }

  private static StepAnalyzer getConfiguredAnalyzer(StepAnalysisContext context,
      StepAnalysisFileStore fileStore) throws WdkModelException {
    StepAnalyzer analyzer = context.getStepAnalysis().getAnalyzerInstance();
    analyzer.setStorageDirectory(fileStore.getStorageDirPath(context.createHash()));
    analyzer.setFormParams(context.getFormParams());
    analyzer.setAnswerValue(context.getStep().getAnswerValue());
    return analyzer;
  }
  
  @Override
  public StepAnalysisViewResolver getViewResolver() {
    return _viewResolver;
  }
  
  @Override
  public void clearResultsCache() throws WdkModelException {

    // first shut down running threads
    shutDownRunningThreads();
    
    // remove execution results from database
    _dataStore.deleteAllExecutions();
    
    // remove execution data from file store
    _fileStore.deleteAllExecutions();

    // restart threadpool
    startThreadPool();
  }

  @Override
  public Path getResourcePath(StepAnalysisContext context, String relativePath) {
    if (relativePath.startsWith(System.getProperty("file.separator"))) {
      relativePath = relativePath.substring(1);
    }
    return Paths.get(_fileStore.getStorageDirPath(context.createHash()).toString(), relativePath);
  }

  private void startThreadPool() {
    _threadPool = Executors.newFixedThreadPool(_execConfig.getThreadPoolSize() + 1);
    _threadResults = new ConcurrentLinkedDeque<>();
    _threadMonitor = _threadPool.submit(new FutureCleaner(_threadResults));
  }
  
  @Override
  public void shutDown() {
    // shut down any running threads
    shutDownRunningThreads();
  }
  
  private void shutDownRunningThreads() {
    try {
      // FIXME: need to figure out what all is needed to shut down these threads
      //   Currently just calling shutdownNow(), which may or may not be sufficient
      _threadPool.shutdownNow();
      /*
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
      */
    } catch (Exception e) {
      LOG.error("Unable to cleanly shut down Step Analysis Factory", e);
    }
  }
  
  @SuppressWarnings("unused")
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
            List<Future<ExecutionStatus>> futuresToRemove = new ArrayList<>();
            for (Future<ExecutionStatus> future : _threadResults) {
              if (future.isDone() || future.isCancelled()) {
                try {
                  LOG.info("Step Analysis completed with status: " + future.get());
                }
                catch (ExecutionException | CancellationException | InterruptedException e) {
                  LOG.error("Exception thrown while retrieving step analysis status (on completion)", e);
                }
                futuresToRemove.add(future);
              }
            }
            // remove futures after collecting them so as not to interfere with iterator above
            for (Future<ExecutionStatus> future : futuresToRemove) {
              _threadResults.remove(future);
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
        // update database that we are running
        _dataStore.updateExecution(contextHash, ExecutionStatus.RUNNING, new Date(), null, null);
    
        // create step analysis instance and run
        StepAnalyzer analyzer = getConfiguredAnalyzer(_context, _fileStore);
        ExecutionStatus status = analyzer.runAnalysis(
            _context.getStep().getAnswerValue(), new StatusLogger(contextHash, _dataStore));
      
        LOG.info("Analyzer returned without exception and with status: " + status);
        
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

  @Override
  public void createResultsTable() throws WdkModelException {
    _dataStore.createExecutionTable();
  }

  @Override
  public void clearResultsTable() throws WdkModelException {
    _dataStore.deleteAllExecutions();
  }

  @Override
  public void dropResultsTable(boolean purge) throws WdkModelException {
    _dataStore.deleteExecutionTable(purge);
  }
}
