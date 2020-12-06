package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.Step;

/**
 * This class is used as a stand-in for a true factory implementation when no
 * plugins or their associated configuration are present in the model.  It only
 * supports cache operations to add, clear, and delete the results table.  These
 * operations are independent of plugin configuration since they only depend on
 * the presence of an AppDB.  All other operations either return empty results
 * or throw UnsupportedOperationExceptions.
 *
 * @author rdoherty
 */
public class UnconfiguredStepAnalysisFactory implements StepAnalysisFactory {

  private static final Logger LOG = Logger.getLogger(UnconfiguredStepAnalysisFactory.class);

  private static final String UNSUPPORTED_MESSAGE =
      "Step Analysis Plugins must be configured in the WDK model to perform this operation.";

  private final StepAnalysisDataStore _dataStore;

  public UnconfiguredStepAnalysisFactory(WdkModel wdkModel) {
    _dataStore = new StepAnalysisPersistentDataStore(wdkModel);
  }

  @Override
  public void createResultsTable() throws WdkModelException {
    _dataStore.createExecutionTable();
  }

  @Override
  public void clearResultsTable() throws WdkModelException {
    _dataStore.deleteAllExecutions();
    LOG.warn("Note: no file storage configured for step analysis; only DB data purged.");
  }

  @Override
  public void dropResultsTable(boolean purge) throws WdkModelException {
    _dataStore.deleteExecutionTable(purge);
    LOG.warn("Note: no file storage configured for step analysis; only DB data purged.");
  }

  @Override
  public boolean hasCompleteAnalyses(Step step) throws WdkModelException {
    return false;
  }

  @Override
  public void saveNewInstance(RunnableObj<StepAnalysisInstance> stepAnalysisInstance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void clearResultsCache() throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void shutDown() {
    // nothing to shut down
  }

  @Override
  public InputStream getProperties(long analysisId) throws WdkModelException, WdkUserException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void setProperties(long analysisId, InputStream propertyStream) {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public List<StepAnalysisInstance> getAppliedAnalyses(Step step, ValidationLevel level)
      throws WdkModelException {
    return Collections.emptyList();
  }

  @Override
  public ValidationBundle validateStep(RunnableObj<Step> step, StepAnalysis analysis)
      throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void updateInstance(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public ExecutionStatus runAnalysis(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Optional<ExecutionInfo> getExecutionInfo(RunnableObj<StepAnalysisInstance> instance)
      throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Optional<ExecutionResult> getExecutionResult(RunnableObj<StepAnalysisInstance> instance)
      throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public ExecutionStatus calculateStatus(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Optional<StepAnalysisInstance> getInstanceById(long analysisId, WdkModel wdkModel,
      ValidationLevel validationLevel) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Path getResourcePath(RunnableObj<StepAnalysisInstance> instance, String relativePath)
      throws WdkModelException, WdkUserException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }
}
