package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.events.StepCopiedEvent;
import org.gusdb.wdk.events.StepImportedEvent;
import org.gusdb.wdk.events.StepResultsModifiedEvent;
import org.gusdb.wdk.events.StepRevisedEvent;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins.ExecutionConfig;
import org.gusdb.wdk.model.analysis.StepAnalyzer;
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
 *   - StepAnalysisInstances in UserDB
 *   - Threadpool for executions
 *   - Results cache
 *
 * StepAnalysisInstance:
 *
 * Things to maintain:
 *   - valid step + analysis name + version + props + params = def to execution
 *   - cache of execDef -> status/results
 */
public class StepAnalysisFactoryImpl implements StepAnalysisFactory, EventListener {

  private static Logger LOG = Logger.getLogger(StepAnalysisFactoryImpl.class);

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
    Events.subscribe(this, StepResultsModifiedEvent.class,
        StepRevisedEvent.class, StepCopiedEvent.class, StepImportedEvent.class);
  }

  /**
   * Performs actions based on passed events.
   */
  @Override
  public void eventTriggered(Event event) throws Exception {
    if (event instanceof StepRevisedEvent) {
      invalidateResults(((StepRevisedEvent)event).getRevisedStep().getStepId());
    }
    else if (event instanceof StepResultsModifiedEvent) {
      List<Long> stepIds = ((StepResultsModifiedEvent)event).getStepIds();
      LOG.debug("StepsModifiedEvent with " + stepIds.size() + " steps: " +
          FormatUtil.arrayToString(stepIds.toArray()));
      for (long stepId : stepIds) {
        invalidateResults(stepId);
      }
    }
    else if (event instanceof StepCopiedEvent) {
      StepCopiedEvent copyEvent = (StepCopiedEvent)event;
      copyAnalysisInstances(copyEvent.getFromStep(), copyEvent.getToStep());
    }
  }

  private void invalidateResults(long stepId) throws WdkModelException, WdkUserException {
    LOG.debug("Request made to reassess analyses of step " + stepId);
    Map<Long, StepAnalysisInstance> instances = _dataStore.getAnalysesByStepId(stepId, _fileStore);
    for (StepAnalysisInstance instance : instances.values()) {
      LOG.info("Reassessing step analysis with ID " + instance.getAnalysisId());
      LOG.trace("TRACE: " + instance.getJsonSummary());

      // non-new steps that have been revised have invalid results until run again
      if (!(instance.getState().equals(StepAnalysisState.NO_RESULTS) ||
            instance.getState().equals(StepAnalysisState.INVALID_RESULTS))) {
        instance.setState(StepAnalysisState.INVALID_RESULTS);
        _dataStore.setState(instance.getAnalysisId(), StepAnalysisState.INVALID_RESULTS);
      }

      // check revised step for validity and update if newly invalid
      try {
        checkStepForValidity(instance);
        instance.setIsValidStep(true);
      }
      catch (IllegalAnswerValueException e) {
        // if answer value of toStep is not valid for the given analysis, mark as
        // such and save; user will not be shown form and will be unable to run analysis
        instance.setIsValidStep(false, e.getMessage());
        _dataStore.setInvalidStepReason(instance.getAnalysisId(), instance.getInvalidStepReason());
      }
    }
  }

  @Override
  public List<StepAnalysisInstance> getAllAnalyses() throws WdkModelException {
    return _dataStore.getAllAnalyses(_fileStore);
  }

  @Override
  public Map<Long, StepAnalysisInstance> getAppliedAnalyses(Step step) throws WdkModelException {
    return _dataStore.getAnalysesByStepId(step.getStepId(), _fileStore);
  }

  @Override
  public boolean hasCompleteAnalyses(Step step) throws WdkModelException {
    for (StepAnalysisInstance instance : getAppliedAnalyses(step).values()) {
      if (instance.getStatus().equals(ExecutionStatus.COMPLETE)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object getFormViewModel(StepAnalysisInstance instance) throws WdkModelException, WdkUserException {
    return getConfiguredAnalyzer(instance, _fileStore).getFormViewModel();
  }

  @Override
  public ValidationBundle validateFormParams(StepAnalysisInstance instance)
      throws WdkModelException, WdkUserException {
    // this is a unique configuration; validate parameters
    return getConfiguredAnalyzer(instance, _fileStore)
        .validateFormParamValues(instance.getFormParams());
  }

  @Override
  public StepAnalysisInstance createAnalysisInstance(Step step, StepAnalysis stepAnalysis, String answerValueChecksum)
      throws WdkModelException, IllegalAnswerValueException, WdkUserException {

    StepAnalysisInstance stepAnalysisInstance = StepAnalysisInstance.createNewInstance(step, stepAnalysis, answerValueChecksum);

    // ensure this is a valid step to analyze
    checkStepForValidity(stepAnalysisInstance);

    // analysis valid; write analysis to DB
    writeNewAnalysisInstance(stepAnalysisInstance, true);

    return stepAnalysisInstance;
  }

  @Override
  public StepAnalysisInstance copyAnalysisInstance(StepAnalysisInstance instance)
      throws WdkModelException {
    StepAnalysisInstance copy = StepAnalysisInstance.createCopy(instance);
    copy.setStatus(ExecutionStatus.CREATED);
    copy.setState(StepAnalysisState.NO_RESULTS);
    writeNewAnalysisInstance(copy, true);
    copyProperties(instance, copy);
    return copy;
  }

  private void copyAnalysisInstances(Step fromStep, Step toStep)
      throws WdkModelException, WdkUserException {
    LOG.info("Request made to copy analysis instances from step " + fromStep.getStepId() + " to " + toStep.getStepId());
    Map<Long, StepAnalysisInstance> fromInstances = _dataStore.getAnalysesByStepId(fromStep.getStepId(), _fileStore);
    for (StepAnalysisInstance fromInstance : fromInstances.values()) {
      LOG.info("Copying step analysis with ID " + fromInstance.getAnalysisId());
      LOG.trace("TRACE: " + fromInstance.getJsonSummary());
      StepAnalysisInstance toInstance = StepAnalysisInstance.createCopy(fromInstance);
      toInstance.setStep(toStep);
      // non-new steps copied during revise have invalid results until run again
      // non-new steps copied during import should always have no_results
      toInstance.setState(StepAnalysisState.NO_RESULTS);
      try {
        checkStepForValidity(toInstance);
        toInstance.setIsValidStep(true);
      }
      catch (IllegalAnswerValueException e) {
        // if answer value of toStep is not valid for the given analysis, mark as
        // such and save; user will not be shown form and will be unable to run analysis
        toInstance.setIsValidStep(false, e.getMessage());
      }
      writeNewAnalysisInstance(toInstance, false);
      LOG.info("Wrote new duplicate context with ID " + toInstance.getAnalysisId() +
          " for revised step " + toInstance.getStep().getStepId() + ".  Copying properties...");
      // copy properties of old context to new and make sure to close- not closing is a connection leak!
      copyProperties(fromInstance, toInstance);
    }
    LOG.info("Completed copy.");
  }

  private void copyProperties(StepAnalysisInstance fromInstance, StepAnalysisInstance toInstance)
      throws WdkModelException {
    // copy properties of old context to new and make sure to close- not closing is a connection leak!
    InputStream propertyStream = null;
    try {
      propertyStream = getProperties(fromInstance.getAnalysisId());
      if (propertyStream != null) {
        setProperties(toInstance.getAnalysisId(), propertyStream);
        LOG.info("Properties copied.");
      }
    }
    catch (WdkUserException e) {
      // IDs should be valid here since the exist in contexts (not passed in by service)
      throw new WdkModelException("Passed context contains invalid step analysis ID.", e);
    }
    finally {
      IoUtil.closeQuietly(propertyStream);
    }
  }

  private void writeNewAnalysisInstance(StepAnalysisInstance instance, boolean adjustDisplayName)
      throws WdkModelException {
    // try to help user differentiate between tabs if requested
    if (adjustDisplayName) {
      instance.setDisplayName(getAdjustedDisplayName(instance));
    }

    // create new execution instance
    long saId = _dataStore.getNextId();
    _dataStore.insertAnalysis(saId, instance.getStep().getStepId(), instance.getDisplayName(),
        instance.getState(), instance.hasParams(), instance.getInvalidStepReason(), instance.createHash(), instance.serializeInstance(), instance.getUserNotes());

    // override any previous value for id
    instance.setAnalysisId(saId);
  }

  private String getAdjustedDisplayName(StepAnalysisInstance instance) throws WdkModelException {
    Collection<StepAnalysisInstance> stepInstances =
        _dataStore.getAnalysesByStepId(instance.getStep().getStepId(), _fileStore).values();
    if (stepInstances.isEmpty()) return instance.getDisplayName();
    String displayNameToAttempt = instance.getDisplayName();
    boolean displayNameUnique = false;
    int index = 1;
    while (!displayNameUnique) {
      displayNameUnique = true;
      LOG.info("Attempting name: " + displayNameToAttempt);
      for (StepAnalysisInstance otherInstance : stepInstances) {
        LOG.info("Comparing candidate name '" + displayNameToAttempt +
            "' for instance " + instance.getAnalysisId() + " to analysis " +
            otherInstance.getAnalysisId() + " with name " +
            otherInstance.getDisplayName());
        if (otherInstance.getDisplayName().equals(displayNameToAttempt)) {
          LOG.info("Found instance " + otherInstance.getAnalysisId() + " that already has name: " + displayNameToAttempt);
          displayNameUnique = false;
          index++;
          displayNameToAttempt = instance.getDisplayName() + " (" + index + ")";
          break;
        }
      }
      LOG.info("Result of '" + displayNameToAttempt + "': unique = " + displayNameUnique);
    }
    return displayNameToAttempt;
  }

  private void checkStepForValidity(StepAnalysisInstance instance)
      throws WdkModelException, IllegalAnswerValueException, WdkUserException {
    // ensure this is a valid step to analyze
    AnswerValue answer = instance.getAnswerValue();
    if (answer.getResultSizeFactory().getResultSize() == 0) {
      throw new IllegalAnswerValueException("You cannot analyze a Step with zero results.");
    }
    getConfiguredAnalyzer(instance, _fileStore).validateAnswerValue(answer);
  }

  @Override
  public StepAnalysisInstance runAnalysis(StepAnalysisInstance instance)
      throws WdkModelException {
    ExecutionStatus initialStatus = ExecutionStatus.PENDING;
    String hash = instance.createHash();
    boolean newlyCreated = _dataStore.insertExecution(hash, initialStatus, new Date());
    boolean instanceModified = false;

    // now that user has run this analysis, set 'not new' if still new
    if (!instance.getState().equals(StepAnalysisState.SHOW_RESULTS)) {
      _dataStore.setState(instance.getAnalysisId(), StepAnalysisState.SHOW_RESULTS);
      instance.setState(StepAnalysisState.SHOW_RESULTS);
      instanceModified = true;
    }

    // if instance was modified, recheck status
    //   TODO: fix logic here to be less ugly/costly
    if (instanceModified) {
      try {
        StepAnalysisInstance statusInstance = getSavedAnalysisInstance(instance.getAnalysisId());
        instance.setStatus(statusInstance.getStatus());
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
    LOG.info("Checking whether to run vs. use previous result. hash = " + hash +
        ", newlyCreated = " + newlyCreated + ", status = " + instance.getStatus());
    if (newlyCreated || instance.getStatus().requiresRerun()) {

      // ensure empty data and file stores and create dir
      _fileStore.recreateStore(hash);
      if (!newlyCreated) {
        _dataStore.resetExecution(hash, initialStatus);
      }

      // set initial status on in-memory instance
      instance.setStatus(initialStatus);

      // run the analysis
      return executeAnalysis(instance);
    }

    // otherwise, no need to run since result already generated or plugin currently running;
    LOG.info("Attempt made to run analysis, but it was unnecessary.  Here's the instance at that time:\n" + instance.getJsonSummary());
    return instance;
  }

  private StepAnalysisInstance executeAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    try {
      _threadResults.add(new RunningAnalysis(instance.getAnalysisId(),
          _threadPool.submit(new AnalysisCallable(instance, _dataStore, _fileStore))));
      return instance;
    }
    catch (RejectedExecutionException e) {
      throw new WdkModelException("Unable to create new step analysis execution.  Thread pool exhausted?", e);
    }
  }

  /**
   * Collects the data associated with a result and returns the aggregating
   * object.  This method is only to be called when a "recent" call to
   * getSavedInstance() has status COMPLETE.  No checks are done to ensure that
   * persistent storage mechanisms have not been cleared.
   *
   * TODO: Refactor this method... what are its responsibilities and does it have more than one?
   *
   * @param instance instance for this result
   * @return result
   * @throws WdkModelException if inconsistent data is found or other error occurs
   * @throws WdkUserException
   */
  @Override
  public AnalysisResult getAnalysisResult(StepAnalysisInstance instance) throws WdkModelException, WdkUserException {
    String hash = instance.createHash();
    AnalysisResult result = _dataStore.getRawAnalysisResult(hash);
    if (result == null) {
      throw new WdkModelException("Result record not found for instance-generated hash: " + hash);
    }

    LOG.info("Got result back from data store: " + result.getStatus() + ", with results:\n" + result.getStoredString());
    StepAnalyzer analyzer = getConfiguredAnalyzer(instance, _fileStore);
    analyzer.setPersistentCharData(result.getStoredString());
    analyzer.setPersistentBinaryData(result.getStoredBytes());
    result.setResultViewModel(analyzer.getResultViewModel());
    result.setResultViewModelJson(analyzer.getResultViewModelJson());
    result.clearStoredData(); // only care about the view model
    return result;
  }

  @Override
  public StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    _dataStore.deleteAnalysis(instance.getAnalysisId());
    return instance;
  }

  @Override
  public void renameInstance(StepAnalysisInstance instance) throws WdkModelException {
    _dataStore.renameAnalysis(instance.getAnalysisId(), instance.getDisplayName());
  }

 @Override
  public void setUserNotesContext(StepAnalysisInstance instance) throws WdkModelException {
    _dataStore.setUserNotes(instance.getAnalysisId(), instance.getUserNotes());
  }

  @Override
  public StepAnalysisInstance getSavedAnalysisInstance(long analysisId) throws WdkUserException, WdkModelException {
    StepAnalysisInstance instance = _dataStore.getAnalysisById(analysisId, _fileStore);
    if (instance == null) {
      throw new WdkUserException("No analysis exists with id: " + analysisId);
    }
    return instance;
  }

  static StepAnalyzer getConfiguredAnalyzer(StepAnalysisInstance instance,
      StepAnalysisFileStore fileStore) throws WdkModelException, WdkUserException {
    StepAnalyzer analyzer = instance.getStepAnalysis().getAnalyzerInstance();
    analyzer.setStorageDirectory(fileStore.getStorageDirPath(instance.createHash()));
    analyzer.setFormParamValues(instance.getFormParams());
    analyzer.setAnswerValue(instance.getAnswerValue());
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
  public Path getResourcePath(StepAnalysisInstance instance, String relativePath) {
    if (relativePath.startsWith(System.getProperty("file.separator"))) {
      relativePath = relativePath.substring(1);
    }
    return Paths.get(_fileStore.getStorageDirPath(instance.createHash()).toString(), relativePath);
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
    long expirationDuration = (long)analysis.getExecutionTimeoutThresholdInMinutes() * 60 * 1000;
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

  @Override
  public InputStream getProperties(long analysisId) throws WdkModelException, WdkUserException {
    InputStream result = _dataStore.getProperties(analysisId);
    if (result == null) {
      throw new WdkUserException("No analysis found with ID " + analysisId);
    }
    return result;
  }

  @Override
  public void setProperties(long analysisId, InputStream propertyStream) throws WdkModelException, WdkUserException {
    if (!_dataStore.setProperties(analysisId, propertyStream)) {
      throw new WdkUserException("No analysis found with ID " + analysisId);
    }
  }

  @Override
  public void setFormParams(StepAnalysisInstance instance)
      throws WdkModelException {
    if (!instance.hasParams()) {
      _dataStore.setHasParams(instance.getAnalysisId(), true);
      instance.setHasParams(true);
    }

    _dataStore.updateInstance(instance.getAnalysisId(), instance.createHash(),
        instance.serializeInstance());
  }
}
