package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.Step;

public interface StepAnalysisFactory {

  List<StepAnalysisInstance> getAppliedAnalyses(Step step, ValidationLevel level) throws WdkModelException;

  boolean hasCompleteAnalyses(Step step) throws WdkModelException;

  ValidationBundle validateStep(RunnableObj<Step> step, StepAnalysis analysis) throws WdkModelException;

  /**
   * Assigns a (sequence-generated) ID to the instance passed and save it in the
   * database.  May also modify the display name to ensure a step's tabs have
   * unique names.
   *
   * @param instance instance to be inserted
   * @throws WdkModelException if error occurs while interacting with the database
   */
  void saveNewInstance(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException;

  /**
   * Passed instance must have a valid ID.  Display name will not be adjusted,
   * but all "meta" fields (and params) will be saved.
   * 
   * @param instance instance to be saved
   * @throws WdkModelException
   */
  void updateInstance(StepAnalysisInstance instance) throws WdkModelException;

  ExecutionStatus runAnalysis(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException;

  Optional<ExecutionInfo> getExecutionInfo(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException;

  /**
   * Collects the data associated with a result and returns the aggregating
   * object.  This method is only to be called when a "recent" call to
   * getSavedAnalysisInstance() has status COMPLETE.  No checks are done to ensure that
   * persistent storage mechanisms have not been cleared.
   *
   * @param instance step analysis instance for this result
   * @return result
   * @throws WdkModelException if error occurs
   */
  Optional<ExecutionResult> getExecutionResult(RunnableObj<StepAnalysisInstance> instance) throws WdkModelException;

  ExecutionStatus calculateStatus(StepAnalysisInstance instance) throws WdkModelException;

  StepAnalysisInstance deleteAnalysis(StepAnalysisInstance instance) throws WdkModelException;

  Optional<StepAnalysisInstance> getInstanceById(long analysisId, Step step, ValidationLevel validationLevel) throws WdkModelException;

  void clearResultsCache() throws WdkModelException;

  Path getResourcePath(RunnableObj<StepAnalysisInstance> instance, String relativePath) throws WdkModelException, WdkUserException;

  void shutDown();

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
}