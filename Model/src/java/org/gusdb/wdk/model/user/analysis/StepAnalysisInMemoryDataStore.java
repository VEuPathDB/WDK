package org.gusdb.wdk.model.user.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class StepAnalysisInMemoryDataStore extends StepAnalysisDataStore {

  private static final Logger LOG = Logger.getLogger(StepAnalysisInMemoryDataStore.class);
  
  /**
   * Eventual table will have:
   *   analysisId(PK), stepId, displayName, isNew, contextHash, context CLOB
   */
  // will map stepId -> List<analysisId>
  private static Map<Integer, List<Integer>> STEP_ANALYSIS_MAP = new HashMap<>();
  // will map analysisId -> AnalysisInfo
  private static Map<Integer, AnalysisInfo> ANALYSIS_INFO_MAP = new HashMap<>();

  /**
   * Eventual table will have:
   *   contextHash(PK), status, log CLOB, data CLOB, data BLOB
   */
  // will map contextHash -> AnalysisResult
  private static Map<String, AnalysisResult> RESULT_INFO_MAP = new LinkedHashMap<>();

  private static AtomicInteger ID_SEQUENCE = new AtomicInteger(0);
  
  public StepAnalysisInMemoryDataStore(WdkModel wdkModel) {
    super(wdkModel);
  }

  @Override
  public int getNextId() throws WdkModelException {
    return ID_SEQUENCE.incrementAndGet();
  }

  @Override
  public void insertAnalysis(int analysisId, int stepId, String displayName,
      String contextHash, String serializedContext) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (!STEP_ANALYSIS_MAP.containsKey(stepId)) {
        STEP_ANALYSIS_MAP.put(stepId, new ArrayList<Integer>());
      }
      STEP_ANALYSIS_MAP.get(stepId).add(analysisId);
      AnalysisInfo info = new AnalysisInfo(analysisId, stepId, displayName, true, contextHash, serializedContext);
      ANALYSIS_INFO_MAP.put(analysisId, info);
      LOG.info("Inserted analysis with ID " + analysisId + " on step " + stepId +
          "; now " + STEP_ANALYSIS_MAP.get(stepId).size() + " analyses for this step.");
    }
  }
  
  @Override
  public void deleteAnalysis(int analysisId) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (!ANALYSIS_INFO_MAP.containsKey(analysisId)) {
        LOG.info("Unable to find value for analysis ID " + analysisId);
        throw new WdkModelException("Analysis ID to be deleted [ " + analysisId + " ] does not exist.");
      }
      int stepId = ANALYSIS_INFO_MAP.get(analysisId).stepId;
      ANALYSIS_INFO_MAP.remove(analysisId);
      
      // remove reference to this analysis in step map
      List<Integer> idsForStep = STEP_ANALYSIS_MAP.get(stepId);
      idsForStep.remove((Integer)analysisId);
      
      // remove record for step if no analyses remain
      if (idsForStep.isEmpty()) {
        STEP_ANALYSIS_MAP.remove(stepId);
      }
    }
  }

  @Override
  public void renameAnalysis(int analysisId, String displayName) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (ANALYSIS_INFO_MAP.containsKey(analysisId)) {
        ANALYSIS_INFO_MAP.get(analysisId).displayName = displayName;
        return;
      }
      throw new WdkModelException("No analysis exists with id: " + analysisId);
    }
  }

  @Override
  public void setNewFlag(int analysisId, boolean isNew) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (ANALYSIS_INFO_MAP.containsKey(analysisId)) {
        ANALYSIS_INFO_MAP.get(analysisId).isNew = isNew;
        return;
      }
      throw new WdkModelException("No analysis exists with id: " + analysisId);
    }
  }

  @Override
  public void updateContext(int analysisId, String contextHash, String serializedContext) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (ANALYSIS_INFO_MAP.containsKey(analysisId)) {
        ANALYSIS_INFO_MAP.get(analysisId).contextHash = contextHash;
        ANALYSIS_INFO_MAP.get(analysisId).serializedContext = serializedContext;
        return;
      }
      throw new WdkModelException("No analysis exists with id: " + analysisId);
    }
  }

  @Override
  protected List<Integer> getAnalysisIdsByStepId(int stepId) throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      if (STEP_ANALYSIS_MAP.containsKey(stepId)) {
        return STEP_ANALYSIS_MAP.get(stepId);
      }
      return new ArrayList<Integer>();
    }
  }

  @Override
  protected List<Integer> getAllAnalysisIds() throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      return new ArrayList<Integer>(ANALYSIS_INFO_MAP.keySet());
    }
  }

  @Override
  protected Map<Integer, AnalysisInfoPlusStatus> getAnalysisInfoForIds(List<Integer> analysisIds)
      throws WdkModelException {
    synchronized(ANALYSIS_INFO_MAP) {
      synchronized(RESULT_INFO_MAP) {
        Map<Integer, AnalysisInfoPlusStatus> map = new HashMap<>();
        for (Integer analysisId : analysisIds) {
          AnalysisInfoPlusStatus aips = new AnalysisInfoPlusStatus();
          aips.analysisInfo = ANALYSIS_INFO_MAP.get(analysisId);
          if (RESULT_INFO_MAP.containsKey(aips.analysisInfo.contextHash)) {
            aips.status = RESULT_INFO_MAP.get(aips.analysisInfo.contextHash).getStatus();
          }
          map.put(analysisId, aips);
        }
        return map;
      }
    }
  }
  
  @Override
  public boolean insertExecution(String contextHash, ExecutionStatus initialStatus)
      throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      if (RESULT_INFO_MAP.containsKey(contextHash)) {
        return false;
      }
      RESULT_INFO_MAP.put(contextHash, new AnalysisResult(initialStatus, null, null, null));
      return true;
    }
  }

  @Override
  public void updateExecution(String contextHash, ExecutionStatus status, String charData, byte[] binData) throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      if (RESULT_INFO_MAP.containsKey(contextHash)) {
        AnalysisResult info = RESULT_INFO_MAP.get(contextHash);
        info.setStatus(status);
        info.setStoredString(charData);
        info.setStoredBytes(binData);
        LOG.info("Updated result record for hash[" + contextHash + "], status=" + status + ", charData =\n" + charData);
        return;
      }
      throw new WdkModelException("Step Analysis Execution for hash [" + contextHash + "] does not exist.");
    }
  }

  @Override
  public void deleteExecution(String hash) throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      RESULT_INFO_MAP.remove(hash);
    }
  }

  @Override
  public void deleteAllExecutions() throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      RESULT_INFO_MAP.clear();
    }
  }

  @Override
  protected ExecutionStatus getRawExecutionStatus(String contextHash) throws WdkModelException {
    AnalysisResult result = getRawAnalysisResult(contextHash);
    return (result == null ? null : result.getStatus());
  }

  @Override
  public AnalysisResult getRawAnalysisResult(String contextHash) throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      if (RESULT_INFO_MAP.containsKey(contextHash)) {
        return RESULT_INFO_MAP.get(contextHash);
      }
      return null;
    }
  }

  @Override
  public String getAnalysisLog(String contextHash) throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      if (RESULT_INFO_MAP.containsKey(contextHash)) {
        AnalysisResult result = RESULT_INFO_MAP.get(contextHash);
        return result.getStatusLog();
      }
      throw new WdkModelException("No analysis execution with context hash value: " + contextHash);
    }
  }

  @Override
  public void setAnalysisLog(String contextHash, String str) throws WdkModelException {
    synchronized(RESULT_INFO_MAP) {
      if (RESULT_INFO_MAP.containsKey(contextHash)) {
        RESULT_INFO_MAP.get(contextHash).setStatusLog(str);
        return;
      }
      throw new WdkModelException("No analysis execution with context hash value: " + contextHash);
    }
  }
}
