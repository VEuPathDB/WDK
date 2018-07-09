package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.Step;

public interface StepAnalysisFactory {

  public List<StepAnalysisInstance> getAllAnalyses() throws WdkModelException;

  public Map<Long, StepAnalysisInstance> getAppliedAnalyses(Step step) throws WdkModelException;

  public boolean hasCompleteAnalyses(Step step) throws WdkModelException;

  public Object getFormViewModel(StepAnalysisInstance instance) throws WdkModelException, WdkUserException;

  public List<String> validateFormParams(StepAnalysisInstance instance) throws WdkModelException, WdkUserException;

  /**
   * Creates a valid new analysis instance (i.e. tab).  If successful,
   * this method will assign an ID to the construinstancestance, save it in the database, and may modify the display 
   * name to ensure a step's tabs have unique names.
   * 
   * @throws WdkModelException if error occurs while instantiating instance or writing to persistent store
   * @throws IllegalAnswerValueException if answer cannot be used to create an analysis with the passed instance
   * @throws WdkUserException if answer is OK but analysis params fail validation in another way
   */
  public StepAnalysisInstance createAnalysisInstance(Step step, StepAnalysis stepAnalysis, String answerValueChecksum) throws WdkModelException, IllegalAnswerValueException, WdkUserException;

  public StepAnalysisInstance copyAnalysisInstance(StepAnalysisInstance instance) throws WdkModelException;

  public StepAnalysisInstance runAnalysis(StepAnalysisInstance instance) throws WdkModelException;

  /**
   * Collects the data associated with a result and returns the aggregating
   * object.  This method is only to be called when a "recent" call to
   * getSavedAnalysisInstance() has status COMPLETE.  No checks are done to ensure that
   * persistent storage mechanisms have not been cleared.
   * 
   * @param instance step analysis instance for this result
   * @return result
   * @throws WdkModelException if inconsistent data is found or other error occurs
   * @throws WdkUserException 
   */
  public AnalysisResult getAnalysisResult(StepAnalysisInstance instance) throws WdkModelException, WdkUserException;

  public StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException;

  public void renameInstance(StepAnalysisInstance instance) throws WdkModelException;

  public StepAnalysisInstance getSavedAnalysisInstance(long analysisId) throws WdkUserException, WdkModelException;

  public StepAnalysisViewResolver getViewResolver();

  public void clearResultsCache() throws WdkModelException;

  public Path getResourcePath(StepAnalysisInstance instance, String relativePath);

  public void shutDown();

  public void expireLongRunningExecutions() throws WdkModelException;

  public void createResultsTable() throws WdkModelException;

  public void clearResultsTable() throws WdkModelException;

  public void dropResultsTable(boolean purge) throws WdkModelException;

  public InputStream getProperties(long analysisId) throws WdkModelException, WdkUserException;

  public void setProperties(long analysisId, InputStream propertyStream) throws WdkModelException, WdkUserException;

  public default InputStream getProperties(StepAnalysisInstance instance) throws WdkModelException {
    try{
      return getProperties(instance.getAnalysisId());
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  public default void setProperties(StepAnalysisInstance instance, InputStream propertiesStream) throws WdkModelException {
    try{
      setProperties(instance.getAnalysisId(), propertiesStream);
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }
}
