package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;

public interface StepAnalysisFactory {

  public List<StepAnalysisContext> getAllAnalyses() throws WdkModelException;

  public Map<Long, StepAnalysisContext> getAppliedAnalyses(Step step) throws WdkModelException;

  public boolean hasCompleteAnalyses(Step step) throws WdkModelException;

  public Object getFormViewModel(StepAnalysisContext context) throws WdkModelException, WdkUserException;

  public List<String> validateFormParams(StepAnalysisContext context) throws WdkModelException, WdkUserException;

  /**
   * Validates the passed context, then uses it to creates a new analysis instance (i.e. tab).  If successful,
   * this method will assign an ID to the passed context, and may modify the display name to ensure a step's
   * tabs have unique names.
   * 
   * @param context context to validate and use to create new analysis instance
   * @throws WdkModelException if error occurs while instantiating context or writing to persistent store
   * @throws IllegalAnswerValueException if answer cannot be used to create an analysis with the passed context
   * @throws WdkUserException if answer is OK but analysis params fail validation in another way
   */
  public void createAnalysis(StepAnalysisContext context) throws WdkModelException, IllegalAnswerValueException, WdkUserException;

  public StepAnalysisContext copyContext(StepAnalysisContext context) throws WdkModelException;

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
   * @throws WdkUserException 
   */
  public AnalysisResult getAnalysisResult(StepAnalysisContext context) throws WdkModelException, WdkUserException;

  public StepAnalysisContext deleteAnalysis(StepAnalysisContext context) throws WdkModelException;

  public void renameContext(StepAnalysisContext context) throws WdkModelException;

  public void setUserNotesContext(StepAnalysisContext context) throws WdkModelException;

  public StepAnalysisContext getSavedContext(long analysisId) throws WdkUserException, WdkModelException;

  public StepAnalysisViewResolver getViewResolver();

  public void clearResultsCache() throws WdkModelException;

  public Path getResourcePath(StepAnalysisContext context, String relativePath);

  public void shutDown();

  public void expireLongRunningExecutions() throws WdkModelException;

  public void createResultsTable() throws WdkModelException;

  public void clearResultsTable() throws WdkModelException;

  public void dropResultsTable(boolean purge) throws WdkModelException;

  public InputStream getProperties(long analysisId) throws WdkModelException, WdkUserException;

  public void setProperties(long analysisId, InputStream propertyStream) throws WdkModelException, WdkUserException;

  public default InputStream getProperties(StepAnalysisContext context) throws WdkModelException {
    try{
      return getProperties(context.getAnalysisId());
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  public default void setProperties(StepAnalysisContext context, InputStream propertiesStream) throws WdkModelException {
    try{
      setProperties(context.getAnalysisId(), propertiesStream);
    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }
}
