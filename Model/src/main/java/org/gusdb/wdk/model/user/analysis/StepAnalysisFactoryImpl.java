package org.gusdb.wdk.model.user.analysis;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.events.Event;
import org.gusdb.fgputil.events.EventListener;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
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
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.user.InvalidStrategyStructureException;
import org.gusdb.wdk.model.user.NoSuchElementException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.UserCache;
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
 *   - Representation of a single instance of an analysis (with an ID)
 *
 * Things to maintain:
 *   - valid step + analysis name + version + props + params = def to execution
 *   - cache of execDef -> status/results
 */
public class StepAnalysisFactoryImpl implements StepAnalysisFactory, EventListener {

  private static Logger LOG = Logger.getLogger(StepAnalysisFactoryImpl.class);

  // data and files stores and their configuration
  private final ExecutionConfig _execConfig;
  private final StepAnalysisDataStore _dataStore;
  private final StepAnalysisFileStore _fileStore;

  // step analysis thread management
  private ExecutorService _threadPool;
  private Future<Boolean> _threadMonitor;
  private volatile ConcurrentLinkedDeque<RunningAnalysis> _threadResults;

  public StepAnalysisFactoryImpl(WdkModel wdkModel) throws WdkModelException {
    _execConfig = wdkModel.getStepAnalysisPlugins().getExecutionConfig();
    _dataStore = new StepAnalysisPersistentDataStore(wdkModel);
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
      Step revisedStep = ((StepRevisedEvent)event).getRevisedStep();
      if (revisedStep.getStrategyId().isPresent()) {
        // if strategy still present, just invalidate results
        _dataStore.setStepsDirty(revisedStep.getStepId());
      }
      else {
        // this step was removed from its strategy; delete its step analyses
        deleteAnalysesForStep(revisedStep.getStepId());
      }
    }
    else if (event instanceof StepResultsModifiedEvent) {
      List<Long> stepIds = ((StepResultsModifiedEvent)event).getStepIds();
      LOG.debug("StepsModifiedEvent with " + stepIds.size() + " steps: " +
          FormatUtil.arrayToString(stepIds.toArray()));
      _dataStore.setStepsDirty(stepIds.toArray(new Long[0]));
    }
    else if (event instanceof StepCopiedEvent) {
      StepCopiedEvent copyEvent = (StepCopiedEvent)event;
      copyAnalysisInstances(copyEvent.getFromStep(), copyEvent.getToStep());
    }
  }

  @Override
  public ValidationBundle validateStep(RunnableObj<Step> step, StepAnalysis analysis) throws WdkModelException {
    ValidationBundleBuilder validation = ValidationBundle.builder(ValidationLevel.RUNNABLE);
    try {
      // ensure this is a valid step to analyze
      AnswerValue answer = AnswerValueFactory.makeAnswer(step);
      if (answer.getResultSizeFactory().getResultSize() == 0) {
        throw new IllegalAnswerValueException("You cannot analyze a Step with zero results.");
      }
      // don't need a configured analyzer; contract is answer value must be validated without params or storage options set
      analysis.getAnalyzerInstance().validateAnswerValue(answer);
    }
    catch (IllegalAnswerValueException e) {
      // thrown if step is not runnable
      validation.addError(e.getMessage());
    }
    return validation.build();
  }

  public void deleteAnalysesForStep(long stepId) throws WdkModelException {
    _dataStore.deleteAnalysesByStepId(stepId);
  }

  @Override
  public List<StepAnalysisInstance> getAppliedAnalyses(Step step, ValidationLevel level) throws WdkModelException {
    return _dataStore.getInstancesByStep(step, level);
  }

  @Override
  public boolean hasCompleteAnalyses(Step step) throws WdkModelException {
    if (!step.getValidationBundle().getLevel().isGreaterThanOrEqualTo(ValidationLevel.RUNNABLE)) {
      // need to revalidate step and "upgrade" its validation TODO: make this easier and more efficient
      if (step.getStrategy().isEmpty()) {
        step = Step.builder(step).build(new UserCache(step.getUser()), ValidationLevel.RUNNABLE, step.getStrategy());
      }
      else {
        try {
          step = Strategy.builder(step.getStrategy().get()).build(new UserCache(step.getUser()), ValidationLevel.RUNNABLE)
              .findFirstStepOrThrow(withId(step.getStepId()));
        }
        catch (NoSuchElementException | InvalidStrategyStructureException e) {
          return false;
        }
      }
    }
    // now that step is validated at the runnable level, see if it is runnable and if not, return false
    if (!step.isRunnable()) return false;

    List<StepAnalysisInstance> instances = getAppliedAnalyses(step, ValidationLevel.RUNNABLE);
    for (StepAnalysisInstance instance : instances) {
      if (!instance.isRunnable()) continue;
      Optional<ExecutionInfo> info = getExecutionInfo(instance.getRunnable().getLeft());
      if (info.isEmpty()) continue;
      if (info.get().getStatus().equals(ExecutionStatus.COMPLETE)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void saveNewInstance(RunnableObj<StepAnalysisInstance> stepAnalysisInstance) throws WdkModelException {
    writeNewAnalysisInstance(stepAnalysisInstance.get(), true);
  }

  @Override
  public void updateInstance(StepAnalysisInstance instance) throws WdkModelException {
    _dataStore.updateAnalysis(
        instance.getAnalysisId(),
        instance.getDisplayName(),
        instance.getRevisionStatus(),
        instance.getContextJson(),
        instance.getUserNotes());
  }

  private void copyAnalysisInstances(Step fromStep, Step toStep)
      throws WdkModelException {
    LOG.info("Request made to copy analysis instances from step " + fromStep.getStepId() + " to " + toStep.getStepId());
    List<StepAnalysisInstance> fromInstances = _dataStore.getInstancesByStep(fromStep, ValidationLevel.NONE);
    for (StepAnalysisInstance fromInstance : fromInstances) {
      LOG.info("Copying step analysis with ID " + fromInstance.getAnalysisId());
      // RRD 9/19: while copying, should not matter if fromStep is valid;
      //   we can assume toStep is AS VALID as fromStep and thus can copy the
      //   analysis configuration- it will be as valid or invalid as the
      //   existing step+analysis combo
      StepAnalysisInstance toInstance = StepAnalysisInstance.createCopy(fromInstance, toStep);
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

    // assign ID; if ID already present, throw exception
    if (instance.getAnalysisId() != StepAnalysisInstance.UNSAVED_ID) {
      throw new WdkModelException("Attempt made to insert step analysis instance that already has a saved ID (" + instance.getAnalysisId() + ").");
    }
    instance.setAnalysisId(_dataStore.getNextId());

    _dataStore.insertAnalysis(
        instance.getAnalysisId(),
        instance.getStep().getStepId(),
        instance.getDisplayName(),
        instance.getRevisionStatus(),
        instance.getContextJson(),
        instance.getUserNotes());
  }

  private String getAdjustedDisplayName(StepAnalysisInstance instance) throws WdkModelException {
    Collection<StepAnalysisInstance> stepInstances =
        _dataStore.getInstancesByStep(instance.getStep(), ValidationLevel.NONE);
    // no conflict if no other analyses on this step
    if (stepInstances.isEmpty()) {
      return instance.getDisplayName();
    }
    Set<String> takenNames = stepInstances.stream().map(i -> i.getDisplayName()).collect(Collectors.toSet());
    String baseName = instance.getDisplayName();
    // no conflict if no analyses have the current name
    if (!takenNames.contains(baseName)) {
      return baseName;
    }
    // add an increasing index to the back of the given name until no conflict
    int index = 1;
    while (true) {
      String adjustedName = baseName + " (" + index + ")";
      if (!takenNames.contains(adjustedName)) {
        LOG.info("To ensure uniqueness, adjusted display name of analysis from '" + baseName + "' to  " + adjustedName);
        return adjustedName;
      }
      index++;
    }
  }

  @Override
  public ExecutionStatus runAnalysis(RunnableObj<StepAnalysisInstance> runnableInstance)
      throws WdkModelException {
    StepAnalysisInstance instance = runnableInstance.get();
    String hash = StepAnalysisInstance.getContextHash(runnableInstance);

    // mark this analysis as clean (clears new or dirty status)
    _dataStore.updateAnalysis(
        instance.getAnalysisId(),
        instance.getDisplayName(),
        RevisionStatus.STEP_CLEAN,
        instance.getContextJson(),
        instance.getUserNotes());

    // returns an empty optional if new execution inserted; filled with existing if one already there
    Optional<ExecutionInfo> existingExecution = _dataStore.insertExecution(
        hash, ExecutionStatus.PENDING, new Date(),
        instance.getStepAnalysis().get().getExecutionTimeoutThresholdInMinutes());

    boolean resultsDirExists = _fileStore.storageDirExists(hash);

    // Run analysis plugin under the following conditions:
    //   1. new execution was created (i.e. none ever existed before or cache was cleared)
    //   2. previous run present but failed due to error, interruption, etc.
    //   3. results dir does not exist (may have been deleted by cache clear or manually, or "disappeared" during proxy swap)
    //
    // NOTE: There is a race condition here in between the check of the store and the creation
    //       (first line inside if).  Use combination of lock and createNewFile() to fix.
    LOG.info("Checking whether to run vs. use previous result. hash = " + hash +
        ", newlyCreated = " + existingExecution.isEmpty() + ", status = " +
        existingExecution.map(e -> e.getStatus()).orElse(ExecutionStatus.PENDING));

    // if freshly inserted, data dir missing, or requires rerun
    if (existingExecution.isEmpty() || existingExecution.get().getStatus().requiresRerun() || !resultsDirExists) {

      // empty file stores if present and create dir
      _fileStore.recreateStore(hash);

      // reset execution if previously existed
      if (existingExecution.isPresent()) {
        _dataStore.resetExecution(hash, ExecutionStatus.PENDING, new Date());
      }

      // run the analysis
      try {
        _threadResults.add(new RunningAnalysis(runnableInstance,
            _threadPool.submit(new AnalysisCallable(runnableInstance, _dataStore, _fileStore))));
      }
      catch (RejectedExecutionException e) {
        throw new WdkModelException("Unable to create new step analysis execution.  Thread pool exhausted?", e);
      }

      return ExecutionStatus.RUNNING;
    }

    // otherwise, no need to run since result already generated or plugin currently running
    return existingExecution.get().getStatus();
  }

  @Override
  public Optional<ExecutionInfo> getExecutionInfo(RunnableObj<StepAnalysisInstance> instance)
      throws WdkModelException {
    return _dataStore.getAnalysisStatus(StepAnalysisInstance.getContextHash(instance));
  }

  /**
   * Collects the data associated with a result and returns the aggregated
   * object.  This method is only to be called when a "recent" call to
   * getSavedInstance() has status COMPLETE.  No checks are done to ensure that
   * persistent storage mechanisms have not been cleared.
   *
   * TODO: Refactor this method... what are its responsibilities and does it have more than one?
   *
   * @param instance instance for this result
   * @return result
   * @throws WdkModelException if unable to fetch 
   * @throws WdkUserException
   */
  @Override
  public Optional<ExecutionResult> getExecutionResult(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException {
    String hash = StepAnalysisInstance.getContextHash(instance);
    Optional<ExecutionResult> resultOpt = _dataStore.getAnalysisResult(hash);
    if (resultOpt.isEmpty()) return Optional.empty();
    ExecutionResult result = resultOpt.get();
    if (!result.getStatus().equals(ExecutionStatus.COMPLETE)) return Optional.empty();

    LOG.info("Got result back from data store: " + result.getStatus() + ", with results:\n" + result.getStoredString());
    StepAnalyzer analyzer = getConfiguredAnalyzer(instance, _fileStore);
    analyzer.setPersistentCharData(result.getStoredString());
    analyzer.setPersistentBinaryData(result.getStoredBytes());
    result.clearStoredData(); // only care about the view model
    result.setResultJson(analyzer.getResultViewModelJson());
    return Optional.of(result);
  }

  @Override
  public StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    _dataStore.deleteAnalysisById(instance.getAnalysisId());
    return instance;
  }

  @Override
  public Optional<StepAnalysisInstance> getInstanceById(long analysisId, Step step, ValidationLevel validationLevel) throws WdkModelException {
    return _dataStore.getInstanceById(analysisId, step, validationLevel);
  }

  static StepAnalyzer getConfiguredAnalyzer(RunnableObj<StepAnalysisInstance> instance,
      StepAnalysisFileStore fileStore) throws WdkModelException {
    StepAnalyzer analyzer = instance.get().getStepAnalysis().get().getAnalyzerInstance();
    analyzer.setStorageDirectory(fileStore.getStorageDirPath(StepAnalysisInstance.getContextHash(instance)));
    analyzer.setFormParamValues(instance.get().getFormParams());
    analyzer.setAnswerValue(StepAnalysisInstance.getAnswerValue(instance));
    return analyzer;
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
  public Path getResourcePath(RunnableObj<StepAnalysisInstance> instance, String relativePath) throws WdkModelException, WdkUserException {
    if (relativePath.startsWith(System.getProperty("file.separator"))) {
      relativePath = relativePath.substring(1);
    }
    return Paths.get(_fileStore.getStorageDirPath(StepAnalysisInstance.getContextHash(instance)).toString(), relativePath);
  }

  private void startThreadPool() {
    _threadPool = Executors.newFixedThreadPool(_execConfig.getThreadPoolSize() + 1);
    _threadResults = new ConcurrentLinkedDeque<>();
    _threadMonitor = _threadPool.submit(new FutureCleaner(this, _dataStore, _threadResults));
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
  public ExecutionStatus calculateStatus(StepAnalysisInstance instance) throws WdkModelException {

    // check if passed instance validated at runnable level
    if (!instance.getValidationBundle().getLevel().isGreaterThanOrEqualTo(ValidationLevel.RUNNABLE)) {
      throw new WdkModelException("This method must be called with an analysis validated at the runnable level.");
    }

    // if not runnable after runnable validation, return invalid
    if (!instance.isRunnable()) {
      return ExecutionStatus.INVALID;
    }

    // look up execution evidence for this instance
    RunnableObj<StepAnalysisInstance> runnableInstance = instance.getRunnable().getLeft();
    Optional<ExecutionInfo> executionInfo = getExecutionInfo(runnableInstance);
    boolean resultsDirExists = _fileStore.storageDirExists(StepAnalysisInstance.getContextHash(runnableInstance));

    // If unable to find execution for this hash OR file store at this hash
    // cannot be found, then one of the following conditions is true:
    //   1. this analysis has never been run
    //   2. step has been revised since the last run despite clean revision status
    //         (this is possible because of timestamp params and also possibly dependent steps TODO: check)
    //   3. cache has been cleared
    if (executionInfo.isEmpty() || !resultsDirExists) {
      switch(instance.getRevisionStatus()) {
        case NEW:        return ExecutionStatus.CREATED;
        case STEP_DIRTY: return ExecutionStatus.STEP_REVISED;
        case STEP_CLEAN: return ExecutionStatus.OUT_OF_DATE;
        default:
          throw new WdkModelException("New RevisionStatus value added but not handled here.");
      }
    }

    // if db cache and file store are intact, then return status of execution as is
    return executionInfo.get().getStatus();
  }
}
