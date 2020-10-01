package org.gusdb.wdk.model.user.analysis;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;

public interface StepAnalysisDataStore {

  // methods to manage analysis information
  void createAnalysisTableAndSequence() throws WdkModelException;
  long getNextId() throws WdkModelException;
  void insertAnalysis(long analysisId, long stepId, String displayName, RevisionStatus revisionStatus, String serializedContext, String userNotes) throws WdkModelException;
  void updateAnalysis(long analysisId, String displayName, RevisionStatus revisionStatus, String serializedContext, String userNotes) throws WdkModelException;
  void setStepsDirty(Long... stepIds) throws WdkModelException;
  void deleteAnalysisById(long analysisId) throws WdkModelException;
  void deleteAnalysesByStepId(long stepId) throws WdkModelException;

  // searches for analysis instances
  Optional<StepAnalysisInstance> getInstanceById(long analysisId, Step step, ValidationLevel level) throws WdkModelException;
  List<StepAnalysisInstance> getInstancesByStep(Step step, ValidationLevel level) throws WdkModelException;

  // methods to manage properties
  InputStream getProperties(long analysisId) throws WdkModelException;
  boolean setProperties(long analysisId, InputStream propertiesStream) throws WdkModelException;

  // methods to manage result/status information (cache)
  void createExecutionTable() throws WdkModelException;
  void deleteExecutionTable(boolean purge) throws WdkModelException;

  /**
   * Inserts a step analysis results record for the given analysis hash if one
   * does not already exist.
   *
   * @param contextHash Step Analysis Instance hash used for matching or for the
   *                    creation of a new record
   * @param status      Execution status, used if a new record is created
   * @param startDate   Execution start date, used if a new record is created
   * @param timeoutMinutes number of minutes this execution should be allowed to run before it is expire
   *
   * @return optional containing an exe info if a record matching the given hash
   * already exists, empty optional if a new record was inserted
   */
  Optional<ExecutionInfo> insertExecution(String contextHash, ExecutionStatus status, Date startDate, int timeoutMinutes) throws WdkModelException;  
  void updateExecution(String contextHash, ExecutionStatus status, Date modifiedDate, String charData, byte[] binData) throws WdkModelException;
  void resetExecution(String contextHash, ExecutionStatus status, Date newStartDate) throws WdkModelException;
  void deleteExecution(String contextHash) throws WdkModelException;
  void deleteAllExecutions() throws WdkModelException;
  List<ExecutionInfo> getAllRunningExecutions() throws WdkModelException;

  // methods to query execution status
  Optional<ExecutionInfo> getAnalysisStatus(String contextHash) throws WdkModelException;
  Optional<ExecutionResult> getAnalysisResult(String contextHash) throws WdkModelException;

  // methods to manage execution logs
  String getAnalysisLog(String contextHash) throws WdkModelException;
  void setAnalysisLog(String contextHash, String str) throws WdkModelException;

}
