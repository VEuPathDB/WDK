package org.gusdb.wdk.model.user.analysis;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ExecutionConfig;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
import org.gusdb.wdk.model.analysis.ValidationErrors;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.analysis.FutureCleaner.RunningAnalysis;

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
public class StepAnalysisFactoryImpl implements StepAnalysisFactory, EventListener {

  private static Logger LOG = Logger.getLogger(StepAnalysisFactoryImpl.class);

  /* TODO: remove if not needed before 7/31/14
  private static final String TAB_NAME_MACRO = "$$TAB_NAME$$";
  private static final String DUPLICATE_CONTEXT_MESSAGE =
      "<p>You have another analysis tab open with the same parameter values " +
      "as those set below.  Either close this tab and use that tab (named '" +
      TAB_NAME_MACRO + "') or change the parameters below.</p>" +
      "<p>If you revised your strategy and expected to compare the analysis " +
      "from the previous version of the strategy with that of the revised " +
      "version, this is not the correct approach.  Once you revise a " +
      "strategy, all previous analysis results are invalidated.  Instead, " +
      "duplicate the strategy and revise it as necessary.  Then run an " +
      "analysis on each and compare the two results.</p>"; */

  static final boolean USE_DB_PERSISTENCE = true;

  private final ExecutionConfig _execConfig;
  private final StepAnalysisViewResolver _viewResolver;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;
  private ExecutorService _threadPool;
  private Future<Boolean> _threadMonitor;
  private volatile ConcurrentLinkedDeque<RunningAnalysis> _threadResults;

  public StepAnalysisFactoryImpl(WdkModel wdkModel) throws WdkModelException {
    _viewResolver = new StepAnalysisViewResolver(wdkModel.getStepAnalysisPlugins().getViewConfig());
    _execConfig = wdkModel.getStepAnalysisPlugins().getExecutionConfig();
    _dataStore = (USE_DB_PERSISTENCE ?
        new StepAnalysisPersistentDataStore(wdkModel) :
        new StepAnalysisInMemoryDataStore(wdkModel));
    _fileStore = new StepAnalysisFileStore(Paths.get(_execConfig.getFileStoreDirectory()));
    startThreadPool();
    Events.subscribe(this, StepRevisedEvent.class, StepCopiedEvent.class);
  }

  /**
   * Performs actions based on passed events.
   */
  @Override
  public void eventTriggered(Event event) throws Exception {
    if (event instanceof StepRevisedEvent) {
      invalidateResults(((StepRevisedEvent)event).getRevisedStep());
    }
    else if (event instanceof StepCopiedEvent) {
      StepCopiedEvent copyEvent = (StepCopiedEvent)event;
      copyAnalysisInstances(copyEvent.getFromStep(), copyEvent.getToStep());
    }
  }

  private void invalidateResults(Step step) throws WdkModelException, WdkUserException {
    LOG.info("Request made to reassess analyses of step " + step.getStepId());
    Map<Integer, StepAnalysisContext> contexts = _dataStore.getAnalysesByStepId(step.getStepId(), _fileStore);
    for (StepAnalysisContext context : contexts.values()) {
      LOG.info("Reassessing step analysis with ID " + context.getAnalysisId());
      LOG.trace("TRACE: " + context.getInstanceJson());

      // non-new steps that have been revised have invalid results until run again
      if (!(context.getState().equals(StepAnalysisState.NO_RESULTS) ||
            context.getState().equals(StepAnalysisState.INVALID_RESULTS))) {
        context.setState(StepAnalysisState.INVALID_RESULTS);
        _dataStore.setState(context.getAnalysisId(), StepAnalysisState.INVALID_RESULTS);
      }

      // check revised step for validity and update if newly invalid
      try {
        checkStepForValidity(context);
        context.setIsValidStep(true);
      }
      catch (IllegalAnswerValueException e) {
        // if answer value of toStep is not valid for the given analysis, mark as
        // such and save; user will not be shown form and will be unable to run analysis
        context.setIsValidStep(false, e.getMessage());
        _dataStore.setInvalidStepReason(context.getAnalysisId(), context.getInvalidStepReason());
      }
    }
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
  public boolean hasCompleteAnalyses(Step step) throws WdkModelException {
    for (StepAnalysisContext context : getAppliedAnalyses(step).values()) {
      if (context.getStatus().equals(ExecutionStatus.COMPLETE)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object getFormViewModel(StepAnalysisContext context) throws WdkModelException, WdkUserException {
    return getConfiguredAnalyzer(context, _fileStore).getFormViewModel();
  }

  @Override
  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException, WdkUserException {
    List<String> errorList = new ArrayList<String>();

    // this is a unique configuration; validate parameters
    ValidationErrors errors = getConfiguredAnalyzer(context, _fileStore)
        .validateFormParams(context.getFormParams());

    // if no errors present; return empty error list
    if (errors == null || errors.isEmpty()) return errorList;

    // otherwise, add and return messages to client
    errorList.addAll(errors.getMessages());
    // FIXME: figure out display of these values; for now, translate param errors into strings
    for (Entry<String,List<String>> paramErrors : errors.getParamMessages().entrySet()) {
      for (String message : paramErrors.getValue()) {
        errorList.add(paramErrors.getKey() + ": " + message);
      }
    }

    // validation failed; errors present.  Set state to NO_RESULTS so old results are hidden from user
    if (!context.getState().equals(StepAnalysisState.NO_RESULTS)) {
      context.setState(StepAnalysisState.NO_RESULTS);
      _dataStore.setState(context.getAnalysisId(), StepAnalysisState.NO_RESULTS);
    }
    
    return errorList;
  }

  @Override
  public StepAnalysisContext createAnalysis(StepAnalysisContext context)
      throws WdkModelException, IllegalAnswerValueException, WdkUserException {

    // ensure this is a valid step to analyze
    checkStepForValidity(context);

    // analysis valid; write analysis to DB
    return writeNewAnalysisContext(context, true);
  }

  @Override
  public StepAnalysisContext copyContext(StepAnalysisContext context)
      throws WdkModelException {
    StepAnalysisContext copy = StepAnalysisContext.createCopy(context);
    copy.setStatus(ExecutionStatus.CREATED);
    copy.setState(StepAnalysisState.NO_RESULTS);
    copy = writeNewAnalysisContext(copy, true);
    return copy;
  }

  private void copyAnalysisInstances(Step fromStep, Step toStep) throws WdkModelException, WdkUserException {
    LOG.info("Request made to copy analysis instances from step " + fromStep.getStepId() + " to " + toStep.getStepId());
    Map<Integer, StepAnalysisContext> fromContexts = _dataStore.getAnalysesByStepId(fromStep.getStepId(), _fileStore);
    for (StepAnalysisContext fromContext : fromContexts.values()) {
      LOG.info("Copying step analysis with ID " + fromContext.getAnalysisId());
      LOG.trace("TRACE: " + fromContext.getInstanceJson());
      StepAnalysisContext toContext = StepAnalysisContext.createCopy(fromContext);
      toContext.setStep(toStep);
      // non-new steps copied during revise have invalid results until run again
      toContext.setState(fromContext.getState().equals(StepAnalysisState.NO_RESULTS) ?
          StepAnalysisState.NO_RESULTS : StepAnalysisState.INVALID_RESULTS);
      try {
        checkStepForValidity(toContext);
        toContext.setIsValidStep(true);
      }
      catch (IllegalAnswerValueException e) {
        // if answer value of toStep is not valid for the given analysis, mark as
        // such and save; user will not be shown form and will be unable to run analysis
        toContext.setIsValidStep(false, e.getMessage());
      }
      toContext = writeNewAnalysisContext(toContext, false);
      LOG.info("Wrote new duplicate context with ID " + toContext.getAnalysisId() +
          " for revised step " + toContext.getStep().getStepId());
    }
    LOG.info("Completed copy.");
  }

  private StepAnalysisContext writeNewAnalysisContext(StepAnalysisContext context, boolean adjustDisplayName)
      throws WdkModelException {
    // try to help user differentiate between tabs if requested
    if (adjustDisplayName) {
      context.setDisplayName(getAdjustedDisplayName(context));
    }

    // create new execution instance
    int saId = _dataStore.getNextId();
    _dataStore.insertAnalysis(saId, context.getStep().getStepId(), context.getDisplayName(),
        context.getState(), context.hasParams(), context.getInvalidStepReason(), context.createHash(), context.serializeContext());

    // override any previous value for id
    context.setAnalysisId(saId);

    return context;
  }

  private String getAdjustedDisplayName(StepAnalysisContext context) throws WdkModelException {
    Collection<StepAnalysisContext> stepContexts =
        _dataStore.getAnalysesByStepId(context.getStep().getStepId(), _fileStore).values();
    if (stepContexts.isEmpty()) return context.getDisplayName();
    String displayNameToAttempt = context.getDisplayName();
    boolean displayNameUnique = false;
    int index = 1;
    while (!displayNameUnique) {
      displayNameUnique = true;
      LOG.info("Attempting name: " + displayNameToAttempt);
      for (StepAnalysisContext otherContext : stepContexts) {
        LOG.info("Comparing candidate name '" + displayNameToAttempt +
            "' for context " + context.getAnalysisId() + " to analysis " +
            otherContext.getAnalysisId() + " with name " +
            otherContext.getDisplayName());
        if (otherContext.getDisplayName().equals(displayNameToAttempt)) {
          LOG.info("Found context " + otherContext.getAnalysisId() + " that already has name: " + displayNameToAttempt);
          displayNameUnique = false;
          index++;
          displayNameToAttempt = context.getDisplayName() + " (" + index + ")";
          break;
        }
      }
      LOG.info("Result of '" + displayNameToAttempt + "': unique = " + displayNameUnique);
    }
    return displayNameToAttempt;
  }

  private void checkStepForValidity(StepAnalysisContext context)
      throws WdkModelException, IllegalAnswerValueException, WdkUserException {
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
    if (!context.getState().equals(StepAnalysisState.SHOW_RESULTS)) {
      _dataStore.setState(context.getAnalysisId(), StepAnalysisState.SHOW_RESULTS);
      context.setState(StepAnalysisState.SHOW_RESULTS);
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
        StepAnalysisContext statusContext = getSavedContext(context.getAnalysisId());
        context.setStatus(statusContext.getStatus());
      }
      catch (WdkUserException e) {
        throw new WdkModelException("Step Analysis was deleted mid-request!", e);
      }
    }

    // update stored context with param values (must do even if using cached results)
    _dataStore.updateContext(context.getAnalysisId(), hash, context.serializeContext());

    // Run analysis plugin under the following conditions:
    //   1. if new execution was created (i.e. none existed before or cache was cleared)
    //   2. previous run failed due to error, interruption, etc.
    //   3. file system cache has been deleted (by wdkCache or sys admins)
    //
    // NOTE: There is a race condition here in between the check of the store and the creation
    //       (first line inside if).  Use combination of lock and createNewFile() to fix.
    LOG.info("Checking whether to run vs. use previous result. hash = " + hash +
        ", newlyCreated = " + newlyCreated + ", status = " + context.getStatus());
    if (newlyCreated || context.getStatus().requiresRerun()) {

      // ensure empty data and file stores and create dir
      _fileStore.recreateStore(hash);
      if (!newlyCreated) {
        _dataStore.resetExecution(hash, initialStatus);
      }

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
      _threadResults.add(new RunningAnalysis(context.getAnalysisId(),
          _threadPool.submit(new AnalysisCallable(context, _dataStore, _fileStore))));
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
   * @throws WdkUserException 
   */
  @Override
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException, WdkUserException {
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

  static StepAnalyzer getConfiguredAnalyzer(StepAnalysisContext context,
      StepAnalysisFileStore fileStore) throws WdkModelException, WdkUserException {
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
    _threadMonitor = _threadPool.submit(new FutureCleaner(this, _threadResults));
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

  public static boolean isRunExpired(StepAnalysis analysis, long currentTime, Date startTime) {
    long expirationDuration = (long)analysis.getExpirationMinutes() * 60 * 1000;
    long currentDuration = currentTime - startTime.getTime();
    return (currentDuration > expirationDuration);
  }

  @Override
  public void expireLongRunningExecutions() throws WdkModelException {
    _dataStore.expireLongRunningExecutions(_fileStore);
  }

  @Override
  public void createResultsTable() throws WdkModelException {
    _dataStore.createExecutionTable();
    // nothing do do for file store; it is created automatically
  }

  @Override
  public void clearResultsTable() throws WdkModelException {
    _dataStore.deleteAllExecutions();
    _fileStore.deleteAllExecutions();
  }

  @Override
  public void dropResultsTable(boolean purge) throws WdkModelException {
    _dataStore.deleteExecutionTable(purge);
    _fileStore.deleteAllExecutions();
  }
}
