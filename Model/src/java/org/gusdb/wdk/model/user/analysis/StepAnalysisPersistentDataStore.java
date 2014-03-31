package org.gusdb.wdk.model.user.analysis;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Implementation of StepAnalysisDataStore that stores information in the
 * database.  This class manages two tables:
 * 
 * UserDB.STEP_ANALYSIS {
 *   int analysisId (PK)
 *   int stepId
 *   varchar displayName
 *   bool isNew
 *   CLOB context
 * }
 * 
 * AppDB.STEP_ANALYSIS_RESULTS {
 *   varchar contextHash (PK)
 *   varchar status
 *   CLOB log
 *   CLOB data
 * }
 * 
 * It also depends on a primary key sequence named UserDB.STEP_ANALYSIS_PKSEQ.
 * 
 * @author rdoherty
 */
public class StepAnalysisPersistentDataStore extends StepAnalysisDataStore {

  private static final String ANALYSIS_TABLE = "STEP_ANALYSIS";
  private static final String ANALYSIS_RESULTS_TABLE = "STEP_ANALYSIS_RESULTS";
  
  private String INSERT_ANALYSIS_SQL;
  private String DELETE_ANALYSIS_SQL;
  private String UPDATE_NAME_SQL;
  private String UPDATE_NEW_FLAG_SQL;
  private String UPDATE_CONTEXT_SQL;
  private String GET_ANALYSIS_BY_ID_SQL;
  private String GET_ANALYSES_BY_STEP_SQL;
  private String GET_ALL_ANALYSES_SQL;
  
  private String INSERT_EXECUTION_SQL;
  private String UPDATE_EXECUTION_SQL;
  private String SET_EXECUTION_LOG_SQL;
  private String GET_EXECUTION_LOG_SQL;
  private String GET_STATUS_BY_HASH_SQL;
  private String GET_RESULTS_BY_HASH_SQL;
  private String DELETE_ALL_EXECUTIONS_SQL;
  
  private final DatabaseInstance _userDb;
  private final DBPlatform _userDbPlatform;
  private final DataSource _userDbDs;
  
  private final DatabaseInstance _appDb;
  private final DBPlatform _appDbPlatform;
  private final DataSource _appDbDs;
  
  public StepAnalysisPersistentDataStore(WdkModel wdkModel) {
    super(wdkModel);
    _userDb = wdkModel.getUserDb();
    _userDbPlatform = _userDb.getPlatform();
    _userDbDs = _userDb.getDataSource();
    createUserSql(_userDb.getDefaultSchema());
    _appDb = wdkModel.getUserDb();
    _appDbPlatform = _appDb.getPlatform();
    _appDbDs = _appDb.getDataSource();
    createAppSql(_appDb.getDefaultSchema());
  }
  
  private void createUserSql(String schema) {
    String table = schema + ANALYSIS_TABLE;
    
  }
  
  private void createAppSql(String schema) {
    String table = schema + ANALYSIS_RESULTS_TABLE;
    
  }

  @Override
  public int getNextId() throws WdkModelException {
    try {
      return _userDbPlatform.getNextId(_userDbDs, _userDb.getDefaultSchema(), ANALYSIS_TABLE);
    }
    catch (SQLException ex) {
      throw new WdkModelException("Unable to get next ID for table " + ANALYSIS_TABLE);
    }
  }

  @Override
  public void insertAnalysis(int analysisId, int stepId, String displayName, String contextHash,
      String serializedContext) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteAnalysis(int analysisId) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void renameAnalysis(int analysisId, String displayName) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setNewFlag(int analysisId, boolean isNew) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateContext(int analysisId, String contextHash, String serializedContext)
      throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected List<Integer> getAnalysisIdsByStepId(int stepId) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected List<Integer> getAllAnalysisIds() throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Map<Integer, AnalysisInfoPlusStatus> getAnalysisInfoForIds(List<Integer> analysisIds)
      throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean insertExecution(String contextHash, ExecutionStatus status) throws WdkModelException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void updateExecution(String contextHash, ExecutionStatus status, String charData, byte[] binData)
      throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteExecution(String contextHash) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteAllExecutions() throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected ExecutionStatus getRawExecutionStatus(String contextHash) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AnalysisResult getRawAnalysisResult(String contextHash) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setAnalysisLog(String contextHash, String str) throws WdkModelException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getAnalysisLog(String contextHash) throws WdkModelException {
    // TODO Auto-generated method stub
    return null;
  }
}