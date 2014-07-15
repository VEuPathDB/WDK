package org.gusdb.wdk.model.user.analysis;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.analysis.StepAnalysisPlugins;

public abstract class StepAnalysisDataStore {

  private static final Logger LOG = Logger.getLogger(StepAnalysisDataStore.class);

  protected static class AnalysisInfo {

    int analysisId;
    int stepId;
    String displayName;
    StepAnalysisState state;
    boolean hasParams;
    String invalidStepReason;
    String contextHash;
    String serializedContext;

    public AnalysisInfo(int analysisId, int stepId, String displayName, StepAnalysisState state,
        boolean hasParams, String invalidStepReason, String contextHash, String serializedContext) {
      this.analysisId = analysisId;
      this.stepId = stepId;
      this.displayName = displayName;
      this.state = state;
      this.hasParams = hasParams;
      this.invalidStepReason = invalidStepReason;
      this.contextHash = contextHash;
      this.serializedContext = serializedContext;
    }

    @Override
    public String toString() {
      return new StringBuilder("AnalysisInfo {").append(NL)
          .append("analysisId: ").append(analysisId).append(NL)
          .append("stepId: ").append(stepId).append(NL)
          .append("displayName: ").append(displayName).append(NL)
          .append("isNew: ").append(state).append(NL)
          .append("hasParams: ").append(hasParams).append(NL)
          .append("invalidStepReason: ").append(invalidStepReason).append(NL)
          .append("contextHash: ").append(contextHash).append(NL)
          .append("serializedContext: ").append(serializedContext).append(NL)
          .append("}").append(NL).toString();
    }
  }

  protected static class AnalysisInfoPlusStatus {
    AnalysisInfo analysisInfo;
    ExecutionStatus status;
    public AnalysisInfoPlusStatus(AnalysisInfo info) {
      analysisInfo = info;
    }
  }

  protected static class ExecutionInfo {
    String contextHash;
    ExecutionStatus status;
    Date startDate;
    Date updateDate;
    public ExecutionInfo(String contextHash, ExecutionStatus status, Date startDate, Date updateDate) {
      this.contextHash = contextHash;
      this.status = status;
      this.startDate = startDate;
      this.updateDate = updateDate;
    }
  }

  // abstract methods to manage analysis information
  public abstract void createAnalysisTableAndSequence() throws WdkModelException;
  public abstract int getNextId() throws WdkModelException;
  public abstract void insertAnalysis(int analysisId, int stepId, String displayName, StepAnalysisState state, boolean hasParams, String invalidStepReason, String contextHash, String serializedContext) throws WdkModelException;
  public abstract void deleteAnalysis(int analysisId) throws WdkModelException;
  public abstract void renameAnalysis(int analysisId, String displayName) throws WdkModelException;
  public abstract void setState(int analysisId, StepAnalysisState state) throws WdkModelException;
  public abstract void setHasParams(int analysisId, boolean hasParams) throws WdkModelException;
  public abstract void updateContext(int analysisId, String contextHash, String serializedContext) throws WdkModelException;
  protected abstract List<Integer> getAnalysisIdsByHash(String contextHash) throws WdkModelException;
  protected abstract List<Integer> getAnalysisIdsByStepId(int stepId) throws WdkModelException;
  protected abstract List<Integer> getAllAnalysisIds() throws WdkModelException;
  // contract is: analysisInfo will be null if ID does not exist; status will be null if execution does not exist
  protected abstract Map<Integer, AnalysisInfoPlusStatus> getAnalysisInfoForIds(List<Integer> analysisIds) throws WdkModelException;

  // abstract methods to manage result/status information
  public abstract void createExecutionTable() throws WdkModelException;
  public abstract void deleteExecutionTable(boolean purge) throws WdkModelException;
  public abstract boolean insertExecution(String contextHash, ExecutionStatus status, Date startDate) throws WdkModelException;  
  public abstract void updateExecution(String contextHash, ExecutionStatus status, Date updateDate, String charData, byte[] binData) throws WdkModelException;
  public abstract void resetStartDate(String contextHash, Date startDate) throws WdkModelException;
  public abstract void deleteExecution(String contextHash) throws WdkModelException;
  public abstract void deleteAllExecutions() throws WdkModelException;
  protected abstract ExecutionStatus getRawExecutionStatus(String contextHash) throws WdkModelException;
  public abstract AnalysisResult getRawAnalysisResult(String contextHash) throws WdkModelException;
  public abstract List<ExecutionInfo> getAllRunningExecutions() throws WdkModelException;
  public abstract String getAnalysisLog(String contextHash) throws WdkModelException;
  public abstract void setAnalysisLog(String contextHash, String str) throws WdkModelException;

  private final WdkModel _wdkModel;

  // constructor requires WdkModel
  protected StepAnalysisDataStore(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  protected WdkModel getWdkModel() {
    return _wdkModel;
  }

  // helper methods that contain only business logic
  public void resetExecution(String contextHash, ExecutionStatus status) throws WdkModelException {
    Date newStartDate = new Date();
    resetStartDate(contextHash, newStartDate);
    updateExecution(contextHash, status, newStartDate, null, null);
  }

  public StepAnalysisContext getAnalysisById(int analysisId, StepAnalysisFileStore fileStore) throws WdkModelException {
    Map<Integer, AnalysisInfoPlusStatus> rawValues = getAnalysisInfoForIds(Arrays.asList(new Integer[]{ analysisId }));
    if (rawValues.get(analysisId).analysisInfo == null) throw new WdkModelException("Did not find exactly" +
        " one record for analysis ID " + analysisId + "; found " + rawValues.size());
    return getContexts(rawValues, fileStore).iterator().next();
  }

  public List<StepAnalysisContext> getContextsByHash(String contextHash, StepAnalysisFileStore fileStore) throws WdkModelException {
    return getContexts(getAnalysisInfoForIds(getAnalysisIdsByHash(contextHash)), fileStore);
  }

  public Map<Integer,StepAnalysisContext> getAnalysesByStepId(int stepId,
      StepAnalysisFileStore fileStore) throws WdkModelException {
    List<StepAnalysisContext> values = getContexts(
        getAnalysisInfoForIds(getAnalysisIdsByStepId(stepId)), fileStore);
    Map<Integer,StepAnalysisContext> contextMap = new LinkedHashMap<>();
    for (StepAnalysisContext context : values) {
      contextMap.put(context.getAnalysisId(), context);
    }
    return contextMap;
  }

  public List<StepAnalysisContext> getAllAnalyses(StepAnalysisFileStore fileStore)
      throws WdkModelException {
    return getContexts(getAnalysisInfoForIds(getAllAnalysisIds()), fileStore);
  }

  private List<StepAnalysisContext> getContexts(Map<Integer, AnalysisInfoPlusStatus> dataMap, 
      StepAnalysisFileStore fileStore) throws WdkModelException {
    List<StepAnalysisContext> contextList = new ArrayList<StepAnalysisContext>();
    for (Entry<Integer, AnalysisInfoPlusStatus> entry : dataMap.entrySet()) {
      if (entry.getValue().analysisInfo == null) {
        throw new WdkModelException("Unable to find record for analysis ID: " + entry.getKey());
      }
      StepAnalysisContext context = convertToContext(entry.getValue(), fileStore);
      // skip null contexts
      if (context != null) contextList.add(context);
    }
    return contextList;
  }

  private StepAnalysisContext convertToContext(AnalysisInfoPlusStatus data,
      StepAnalysisFileStore fileStore) throws WdkModelException {

    AnalysisInfo info = data.analysisInfo;
    StepAnalysisContext context;
    try {
      context = StepAnalysisContext.createFromStoredData(
          _wdkModel, info.analysisId, info.stepId, info.state, info.hasParams,
          info.invalidStepReason, info.displayName, info.serializedContext);
    }
    catch (DeprecatedAnalysisException e) {
      LOG.warn("Previously stored step analysis with ID " + info.analysisId +
          " could not be loaded", e);
      return null;
    }

    // if analysis was created from a step its analyzer did not approve, then status is always INVALID
    if (!context.getIsValidStep()) {
      context.setStatus(ExecutionStatus.INVALID);
      return context;
    }

    // if analysis is new or invalid, don't care about cache; just set appropriate status and return
    switch(info.state) {
      case INVALID_RESULTS:
        context.setStatus(ExecutionStatus.STEP_REVISED);
        return context;
      case NO_RESULTS:
        context.setStatus(ExecutionStatus.CREATED);
        return context;
      default:
        // pass on to the code below
    }

    // check status of our data caches
    boolean dataCacheIntact = (data.status != null);
    boolean fileCacheIntact = fileStore.storageDirExists(info.contextHash);

    // if both are intact, then return status of execution as is
    if (dataCacheIntact && fileCacheIntact) {
      context.setStatus(data.status);
      return context;
    }

    // if got here then the analysis has been run but one or both caches are bad
    context.setStatus(ExecutionStatus.OUT_OF_DATE);
    return context;
  }

  // go through all running/pending analysis runs in DB, and mark expired if expired
  public void expireLongRunningExecutions(StepAnalysisFileStore fileStore) throws WdkModelException {
    long currentTime = System.currentTimeMillis();
    List<ExecutionInfo> execList = getAllRunningExecutions();
    StepAnalysisPlugins plugins = _wdkModel.getStepAnalysisPlugins();
    for (ExecutionInfo exec : execList) {
      String analysisName = getAnalysisNameForHash(exec.contextHash, fileStore);
      // skip runs that no longer have analysis records referencing them
      if (analysisName == null) continue;
      StepAnalysis analysis = plugins.getStepAnalysis(analysisName);
      if (StepAnalysisFactoryImpl.isRunExpired(analysis, currentTime, exec.startDate)) {
        updateExecution(exec.contextHash, ExecutionStatus.EXPIRED, new Date(), null, null);
      }
    }
  }

  // returns null if unable to find analysis record for this hash
  private String getAnalysisNameForHash(String contextHash, StepAnalysisFileStore fileStore)
      throws WdkModelException {
    List<StepAnalysisContext> contextList = getContextsByHash(contextHash, fileStore);
    if (contextList.isEmpty()) return null;
    return contextList.iterator().next().getStepAnalysis().getName();
  }
}
