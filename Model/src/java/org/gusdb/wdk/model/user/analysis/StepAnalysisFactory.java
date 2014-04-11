package org.gusdb.wdk.model.user.analysis;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;

public interface StepAnalysisFactory {

  public List<StepAnalysisContext> getAllAnalyses() throws WdkModelException;

  public Map<Integer, StepAnalysisContext> getAppliedAnalyses(Step step) throws WdkModelException;

  public Object getFormViewModel(StepAnalysisContext context) throws WdkModelException;

  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException;

  public StepAnalysisContext createAnalysis(StepAnalysisContext context) throws WdkModelException, IllegalAnswerValueException;

  public StepAnalysisContext copyContext(StepAnalysisContext context) throws WdkModelException;

  public void moveAnalysisInstances(Step fromStep, Step toStep) throws WdkModelException;

  public StepAnalysisContext runAnalysis(StepAnalysisContext context) throws WdkModelException;

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
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException;

  public StepAnalysisContext deleteAnalysis(StepAnalysisContext context) throws WdkModelException;

  public void renameContext(StepAnalysisContext context) throws WdkModelException;

  public StepAnalysisContext getSavedContext(int analysisId) throws WdkUserException, WdkModelException;

  public StepAnalysisViewResolver getViewResolver();

  public void clearResultsCache() throws WdkModelException;

  public Path getResourcePath(StepAnalysisContext context, String relativePath);

  public void shutDown();

}
