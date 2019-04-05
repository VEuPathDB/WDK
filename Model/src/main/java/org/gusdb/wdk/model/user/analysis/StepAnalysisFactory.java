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

  List<StepAnalysisInstance> getAllAnalyses() throws WdkModelException;

  Map<Long, StepAnalysisInstance> getAppliedAnalyses(Step step) throws WdkModelException;

  boolean hasCompleteAnalyses(Step step) throws WdkModelException;

  /**
   * Validates the form params for a given Step Analysis Instance and returns
   * a list of validation errors.
   *
   * @return Form param validation errors.
   */
  List<String> validateFormParams(StepAnalysisInstance instance) throws WdkModelException, WdkUserException;

  /**
   * Creates a valid new analysis instance (i.e. tab).  If successful,
   * this method will assign an ID to the constructed instance, save it in the
   * database, and may modify the display name to ensure a step's tabs have
   * unique names.
   *
   * @throws WdkModelException if error occurs while instantiating instance or writing to persistent store
   * @throws IllegalAnswerValueException if answer cannot be used to create an analysis with the passed instance
   * @throws WdkUserException if answer is OK but analysis params fail validation in another way
   */
  StepAnalysisInstance createAnalysisInstance(Step step,
      StepAnalysis stepAnalysis, String answerValueChecksum) throws WdkModelException, IllegalAnswerValueException, WdkUserException;

  StepAnalysisInstance copyAnalysisInstance(StepAnalysisInstance instance) throws WdkModelException;

  StepAnalysisInstance runAnalysis(StepAnalysisInstance instance) throws WdkModelException;

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
  AnalysisResult getAnalysisResult(StepAnalysisInstance instance) throws WdkModelException, WdkUserException;

  StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException;

  void renameInstance(StepAnalysisInstance instance) throws WdkModelException;

  StepAnalysisInstance getSavedAnalysisInstance(long analysisId) throws WdkUserException, WdkModelException;

  void setUserNotesContext(StepAnalysisInstance context) throws WdkModelException;

  StepAnalysisViewResolver getViewResolver();

  void clearResultsCache() throws WdkModelException;

  Path getResourcePath(StepAnalysisInstance instance, String relativePath);

  void shutDown();

  void expireLongRunningExecutions() throws WdkModelException;

  void createResultsTable() throws WdkModelException;

  void clearResultsTable() throws WdkModelException;

  void dropResultsTable(boolean purge) throws WdkModelException;

  InputStream getProperties(long analysisId) throws WdkModelException, WdkUserException;

  void setProperties(long analysisId, InputStream propertyStream) throws WdkModelException, WdkUserException;

  default InputStream getProperties(StepAnalysisInstance instance) throws WdkModelException {
    try{
      return getProperties(instance.getAnalysisId());
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  default void setProperties(StepAnalysisInstance instance,
      InputStream propertiesStream) throws WdkModelException {
    try{
      setProperties(instance.getAnalysisId(), propertiesStream);
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  void setFormParams(StepAnalysisInstance instance) throws WdkModelException;
}
