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
 * database.  This class manages two tables: one for step analysis instances
 * and one for analysis executions.  Instance records may exist that don't yet
 * have an execution, and executions can be shared across instances.  The
 * context_hash column in the instance can map to the primary key of the
 * execution table, but it is not a strict foreign key because there's no
 * guarantee of a mapping (execution may not yet exist).  For table definitions
 * see documentation of the createUserSql() and createAppSql() methods below.
 * This class also depends on a primary key sequence for the analysis instance
 * table.
 * 
 * @author rdoherty
 */
public class StepAnalysisPersistentDataStore extends StepAnalysisDataStore {

  private static final String ANALYSIS_TABLE = "STEP_ANALYSIS";
  private static final String ANALYSIS_SEQUENCE = ANALYSIS_TABLE + "_PKSEQ";
  private static final String EXECUTION_TABLE = "STEP_ANALYSIS_RESULTS";
  
  // SQL to update and query analysis table
  private String CREATE_ANALYSIS_TABLE_SQL;
  private String INSERT_ANALYSIS_SQL;
  private String DELETE_ANALYSIS_SQL;
  private String UPDATE_NAME_SQL;
  private String UPDATE_NEW_FLAG_SQL;
  private String UPDATE_CONTEXT_SQL;
  private String GET_ANALYSIS_IDS_BY_STEP_SQL;
  private String GET_ALL_ANALYSIS_IDS_SQL;
  private String GET_ANALYSES_BY_IDS_SQL;
  
  // SQL to update and query execution table
  private String CREATE_EXECUTION_SQL;
  private String INSERT_EXECUTION_SQL;
  private String DELELE_EXECUTION_SQL;
  private String DELETE_ALL_EXECUTIONS_SQL;
  private String UPDATE_EXECUTION_SQL;
  private String SET_EXECUTION_LOG_SQL;
  private String GET_EXECUTION_LOG_SQL;
  private String GET_STATUS_BY_HASH_SQL;
  private String GET_RESULTS_BY_HASH_SQL;
  
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
  
  /**
   * Create SQL statements and queries for the analysis instance table:
   *
   * UserDB.STEP_ANALYSIS {
   *   int analysis_id (PK)
   *   int step_id
   *   varchar display_name
   *   bool is_new
   *   bool is_valid_step
   *   varchar context_hash
   *   CLOB context
   * }
   */
  private void createUserSql(String schema) {
    String table = schema + ANALYSIS_TABLE;
    CREATE_ANALYSIS_TABLE_SQL = "";
    INSERT_ANALYSIS_SQL = "";
    DELETE_ANALYSIS_SQL = "";
    UPDATE_NAME_SQL = "";
    UPDATE_NEW_FLAG_SQL = "";
    UPDATE_CONTEXT_SQL = "";
    GET_ANALYSIS_IDS_BY_STEP_SQL = "";
    GET_ALL_ANALYSIS_IDS_SQL = "";
    GET_ANALYSES_BY_IDS_SQL = "";
  }

  /**
   * Create SQL statements and queries for the analysis execution table:
   *
   * AppDB.STEP_ANALYSIS_RESULTS {
   *   varchar context_hash (PK)
   *   varchar status
   *   date start_time
   *   date end_Time
   *   CLOB log
   *   CLOB char_data
   *   BLOB bin_data
   * }
   */
  private void createAppSql(String schema) {
    String table = schema + EXECUTION_TABLE;
    CREATE_EXECUTION_SQL = "";
    INSERT_EXECUTION_SQL = "";
    DELELE_EXECUTION_SQL = "";
    DELETE_ALL_EXECUTIONS_SQL = "";
    UPDATE_EXECUTION_SQL = "";
    SET_EXECUTION_LOG_SQL = "";
    GET_EXECUTION_LOG_SQL = "";
    GET_STATUS_BY_HASH_SQL = "";
    GET_RESULTS_BY_HASH_SQL = "";
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
  public void insertAnalysis(int analysisId, int stepId, String displayName, boolean isNew,
      String invalidStepReason, String contextHash, String serializedContext) throws WdkModelException {
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