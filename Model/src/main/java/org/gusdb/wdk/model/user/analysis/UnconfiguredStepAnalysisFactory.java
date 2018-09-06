package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
    _dataStore = (StepAnalysisFactoryImpl.USE_DB_PERSISTENCE ?
        new StepAnalysisPersistentDataStore(wdkModel) :
        new StepAnalysisInMemoryDataStore(wdkModel));
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
  public List<StepAnalysisInstance> getAllAnalyses() throws WdkModelException {
    return new ArrayList<>();
  }

  @Override
  public Map<Long, StepAnalysisInstance> getAppliedAnalyses(Step step) throws WdkModelException {
    return new HashMap<>();
  }

  @Override
  public boolean hasCompleteAnalyses(Step step) throws WdkModelException {
    return false;
  }

  @Override
  public List<String> validateFormParams(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance createAnalysisInstance(Step step, StepAnalysis stepAnalysis, String answerValueChecksum) throws WdkModelException,
      IllegalAnswerValueException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance copyAnalysisInstance(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance runAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public AnalysisResult getAnalysisResult(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void renameInstance(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisInstance getSavedAnalysisInstance(long analysisId) throws WdkUserException, WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisViewResolver getViewResolver() {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void clearResultsCache() throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Path getResourcePath(StepAnalysisInstance instance, String relativePath) {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Object getFormViewModel(StepAnalysisInstance instance) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void shutDown() {
    // nothing to shut down
  }

  @Override
  public void expireLongRunningExecutions() throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
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
  public void setFormParams(StepAnalysisInstance instance)
      throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }
}
