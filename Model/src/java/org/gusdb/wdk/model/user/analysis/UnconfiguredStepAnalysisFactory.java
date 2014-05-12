package org.gusdb.wdk.model.user.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;

public class UnconfiguredStepAnalysisFactory implements StepAnalysisFactory {

  private static final String UNSUPPORTED_MESSAGE =
      "Step Analysis Plugins must be configured in the WDK model to perform this operation.";
  
  @Override
  public List<StepAnalysisContext> getAllAnalyses() throws WdkModelException {
    return new ArrayList<StepAnalysisContext>();
  }

  @Override
  public Map<Integer, StepAnalysisContext> getAppliedAnalyses(Step step) throws WdkModelException {
    return new HashMap<Integer, StepAnalysisContext>();
  }

  @Override
  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisContext createAnalysis(StepAnalysisContext context) throws WdkModelException,
      IllegalAnswerValueException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisContext copyContext(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void copyAnalysisInstances(Step fromStep, Step toStep) throws WdkModelException {
    // unable to move analysis instances, but cannot throw exception here
  }

  @Override
  public StepAnalysisContext runAnalysis(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisContext deleteAnalysis(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void renameContext(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public StepAnalysisContext getSavedContext(int analysisId) throws WdkUserException, WdkModelException {
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
  public Path getResourcePath(StepAnalysisContext context, String relativePath) {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public Object getFormViewModel(StepAnalysisContext context) throws WdkModelException {
    throw new UnsupportedOperationException(UNSUPPORTED_MESSAGE);
  }

  @Override
  public void shutDown() {
    // nothing to shut down
  }

  @Override
  public void createResultsTable() throws WdkModelException {
    // do nothing; cache not configured but don't want to disrupt other cache operations
  }

  @Override
  public void clearResultsTable() throws WdkModelException {
    // do nothing; cache not configured but don't want to disrupt other cache operations
  }

  @Override
  public void dropResultsTable(boolean purge) throws WdkModelException {
    // do nothing; cache not configured but don't want to disrupt other cache operations
  }
}
