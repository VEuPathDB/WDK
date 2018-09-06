package org.gusdb.wdk.model.user.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.db.DatabaseResultStream;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;

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

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(StepAnalysisPersistentDataStore.class);

  private static final String ANALYSIS_TABLE = "STEP_ANALYSIS";
  private static final String ANALYSIS_SEQUENCE_NAME = ANALYSIS_TABLE + "_PKSEQ";
  private static final String EXECUTION_TABLE = "STEP_ANALYSIS_RESULTS";
  private static final String IN_CLAUSE_KEY = "${SOME_VALUES}";

  // SQL to update and query analysis table
  private String ANALYSIS_SEQUENCE;
  private String CREATE_ANALYSIS_TABLE_SQL;
  private String INSERT_ANALYSIS_SQL;
  private String UPDATE_NAME_SQL;
  private String UPDATE_USERNOTES_SQL;
  private String UPDATE_NEW_FLAG_SQL;
  private String UPDATE_HAS_PARAMS_FLAG_SQL;
  private String UPDATE_INVALID_STEP_REASON;
  private String UPDATE_CONTEXT_SQL;
  private String DELETE_ANALYSIS_SQL;
  private String GET_ANALYSIS_IDS_BY_STEP_SQL;
  private String GET_ANALYSIS_IDS_BY_HASH_SQL;
  private String GET_ALL_ANALYSIS_IDS_SQL;
  private String GET_ANALYSES_BY_IDS_SQL;
  private String GET_ANALYSIS_PROPERTIES;
  private String SET_ANALYSIS_PROPERTIES;

  // SQL to update and query execution table
  private String CREATE_EXECUTION_TABLE_SQL;
  private String FIND_EXECUTION_SQL;
  private String INSERT_EXECUTION_SQL;
  private String UPDATE_EXECUTION_SQL;
  private String RESET_START_DATE_SQL;
  private String DELETE_EXECUTION_SQL;
  private String DELETE_ALL_EXECUTIONS_SQL;
  private String GET_STATUS_BY_HASH_SQL;
  private String GET_STATUSES_BY_HASHES_SQL;
  private String GET_RESULTS_BY_HASH_SQL;
  private String GET_RUNNING_EXECUTIONS_SQL;
  private String SET_EXECUTION_LOG_SQL;
  private String GET_EXECUTION_LOG_SQL;

  private final DatabaseInstance _userDb;
  private final DBPlatform _userPlatform;
  private final DataSource _userDs;
  private final String _userSchema;
  private final int _userBoolType;

  private final DatabaseInstance _appDb;
  private final DBPlatform _appPlatform;
  private final DataSource _appDs;

  private static ReentrantLock CONTEXT_INSERTION_LOCK = new ReentrantLock();
  
  public StepAnalysisPersistentDataStore(WdkModel wdkModel) {
    super(wdkModel);
    
    _userDb = wdkModel.getUserDb();
    _userPlatform = _userDb.getPlatform();
    _userDs = _userDb.getDataSource();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    _userBoolType = _userPlatform.getBooleanType();
    createUserSql();
    
    _appDb = wdkModel.getAppDb();
    _appPlatform = _appDb.getPlatform();
    _appDs = _appDb.getDataSource();
    createAppSql();
  }
  
  /**
   * Create SQL statements and queries for the analysis instance table:
   *
   * UserDB.STEP_ANALYSIS {
   *   int analysis_id (PK)
   *   int step_id
   *   varchar display_name
   *   bool is_new
   *   bool has_params
   *   varchar invalid_step_reason
   *   varchar context_hash
   *   CLOB context
   *   PROPERTIES CLOB
   *   USER_NOTES VARCHAR2(4000)
   * }
   */
  private void createUserSql() {
    String table = _userSchema + ANALYSIS_TABLE;
    String idType = _userPlatform.getNumberDataType(12);
    String userStringType = _userPlatform.getStringDataType(1024);
    String hashType = _userPlatform.getStringDataType(96);
    String boolType = _userPlatform.getBooleanDataType();
    String clobType = _userPlatform.getClobDataType();
    CREATE_ANALYSIS_TABLE_SQL =
        "CREATE TABLE " + table + " (" +
        "  ANALYSIS_ID          " + idType + " NOT NULL," +
        "  STEP_ID              " + idType + "," +
        "  DISPLAY_NAME         " + userStringType + "," +
        "  IS_NEW               " + boolType + "," +
        "  HAS_PARAMS           " + boolType + "," +
        "  INVALID_STEP_REASON  " + userStringType + "," +
        "  CONTEXT_HASH         " + hashType + "," +
        "  CONTEXT              " + clobType + "," +
        "  PROPERTIES           " + clobType + "," +
        "  USER_NOTES           " + userStringType + "," +
        "  PRIMARY KEY (ANALYSIS_ID)" +
        ")";
    ANALYSIS_SEQUENCE = _userSchema + ANALYSIS_SEQUENCE_NAME;
    INSERT_ANALYSIS_SQL =
        "INSERT INTO " + table +
        " (ANALYSIS_ID, STEP_ID, DISPLAY_NAME, IS_NEW, HAS_PARAMS," +
        "  INVALID_STEP_REASON, CONTEXT_HASH, CONTEXT, USER_NOTES)" +
        " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    UPDATE_NAME_SQL =
        "UPDATE " + table + " SET DISPLAY_NAME = ? WHERE ANALYSIS_ID = ?";
    UPDATE_USERNOTES_SQL =
        "UPDATE " + table + " SET USER_NOTES = ? WHERE ANALYSIS_ID = ?";
    UPDATE_NEW_FLAG_SQL =
        "UPDATE " + table + " SET IS_NEW = ? WHERE ANALYSIS_ID = ?";
    UPDATE_HAS_PARAMS_FLAG_SQL =
        "UPDATE " + table + " SET HAS_PARAMS = ? WHERE ANALYSIS_ID = ?";
    UPDATE_INVALID_STEP_REASON =
        "UPDATE " + table + " SET INVALID_STEP_REASON = ? WHERE ANALYSIS_ID = ?";
    UPDATE_CONTEXT_SQL =
        "UPDATE " + table + " SET CONTEXT_HASH = ?, CONTEXT = ? WHERE ANALYSIS_ID = ?";
    DELETE_ANALYSIS_SQL =
        "DELETE FROM " + table + " WHERE ANALYSIS_ID = ?";
    GET_ANALYSIS_IDS_BY_STEP_SQL =
        "SELECT ANALYSIS_ID FROM " + table + " WHERE STEP_ID = ? ORDER BY ANALYSIS_ID ASC";
    GET_ANALYSIS_IDS_BY_HASH_SQL =
        "SELECT ANALYSIS_ID FROM " + table + " WHERE CONTEXT_HASH = ? ORDER BY ANALYSIS_ID ASC";
    GET_ALL_ANALYSIS_IDS_SQL =
        "SELECT ANALYSIS_ID FROM " + table;
    GET_ANALYSES_BY_IDS_SQL =
        "SELECT ANALYSIS_ID, STEP_ID, DISPLAY_NAME, USER_NOTES, IS_NEW, HAS_PARAMS," +
        " INVALID_STEP_REASON, CONTEXT_HASH, CONTEXT FROM " + table +
        " WHERE ANALYSIS_ID IN (" + IN_CLAUSE_KEY + ")";
    GET_ANALYSIS_PROPERTIES =
        "SELECT PROPERTIES FROM " + table + " WHERE ANALYSIS_ID = ?";
    SET_ANALYSIS_PROPERTIES =
        "UPDATE " + table + " SET PROPERTIES = ? WHERE ANALYSIS_ID = ?";
    
  }

  /**
   * Create SQL statements and queries for the analysis execution table:
   *
   * AppDB.STEP_ANALYSIS_RESULTS {
   *   varchar context_hash (PK)
   *   varchar status
   *   date start_date
   *   date update_date
   *   CLOB log
   *   CLOB char_data
   *   BLOB bin_data
   * }
   */
  private void createAppSql() {
    String table = EXECUTION_TABLE;
    String hashType = _appPlatform.getStringDataType(96);
    String statusType = _appPlatform.getStringDataType(96);
    String timestampType = _appPlatform.getDateDataType();
    String clobType = _appPlatform.getClobDataType();
    String blobType = _appPlatform.getBlobDataType();
    CREATE_EXECUTION_TABLE_SQL =
        "CREATE TABLE " + table + " (" +
        "  CONTEXT_HASH  " + hashType + " NOT NULL," +
        "  STATUS        " + statusType + "," +
        "  START_DATE    " + timestampType + "," +
        "  UPDATE_DATE   " + timestampType + "," +
        "  LOG           " + clobType + "," +
        "  CHAR_DATA     " + clobType + "," +
        "  BIN_DATA      " + blobType + "," +
        "  PRIMARY KEY (CONTEXT_HASH)" +
        ")";
    FIND_EXECUTION_SQL =
        "SELECT 1 FROM " + table + " WHERE EXISTS" +
        " (SELECT 1 FROM " + table + " WHERE CONTEXT_HASH = ?)";
    INSERT_EXECUTION_SQL =
        "INSERT INTO " + table +
        " (CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE)" +
        " VALUES (?, ?, ?, ?)";
    UPDATE_EXECUTION_SQL =
        "UPDATE " + table +
        " SET STATUS = ?, UPDATE_DATE = ?, CHAR_DATA = ?, BIN_DATA = ?" +
        " WHERE CONTEXT_HASH = ?";
    RESET_START_DATE_SQL =
        "UPDATE " + table + " SET START_DATE = ? WHERE CONTEXT_HASH = ?";
    DELETE_EXECUTION_SQL =
        "DELETE FROM " + table + " WHERE CONTEXT_HASH = ?";
    DELETE_ALL_EXECUTIONS_SQL =
        "DELETE FROM " + table;
    GET_STATUS_BY_HASH_SQL =
        "SELECT STATUS FROM " + table + " WHERE CONTEXT_HASH = ?";
    GET_STATUSES_BY_HASHES_SQL =
        "SELECT CONTEXT_HASH, STATUS FROM " + table +
        " WHERE CONTEXT_HASH IN ( " + IN_CLAUSE_KEY + " )";
    GET_RESULTS_BY_HASH_SQL =
        "SELECT STATUS, START_DATE, UPDATE_DATE, LOG, CHAR_DATA, BIN_DATA" +
        " FROM " + table + " WHERE CONTEXT_HASH = ?";
    GET_RUNNING_EXECUTIONS_SQL =
        "SELECT CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE FROM " + table +
        " WHERE STATUS = 'PENDING' OR STATUS = 'RUNNING'";
    SET_EXECUTION_LOG_SQL =
        "UPDATE " + table + " SET LOG = ? WHERE CONTEXT_HASH = ?";
    GET_EXECUTION_LOG_SQL =
        "SELECT LOG FROM " + table + " WHERE CONTEXT_HASH = ?";
  }

  @Override
  public long getNextId() throws WdkModelException {
    try {
      return _userPlatform.getNextId(_userDs, _userSchema, ANALYSIS_TABLE);
    }
    catch (SQLException ex) {
      throw new WdkModelException("Unable to get next ID for table " + ANALYSIS_TABLE, ex);
    }
  }

  @Override
  public void createAnalysisTableAndSequence() throws WdkModelException {
    try {
      new SQLRunner(_userDs, CREATE_ANALYSIS_TABLE_SQL, "create-step-analysis-table").executeStatement();
      _userPlatform.createSequence(_userDs, ANALYSIS_SEQUENCE, 1, 1);
    }
    catch (SQLRunnerException|SQLException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void insertAnalysis(long analysisId, long stepId, String displayName, 
      StepAnalysisState state, boolean hasParams, String invalidStepReason,
		  String contextHash, String serializedContext, String userNotes) throws WdkModelException {
    try {
      new SQLRunner(_userDs, INSERT_ANALYSIS_SQL, "insert-step-analysis").executeStatement(
          new Object[] { analysisId, stepId, displayName, state.getDbValue(), hasParams,
              invalidStepReason, contextHash, serializedContext, userNotes },
          new Integer[] { Types.INTEGER, Types.INTEGER, Types.VARCHAR, _userBoolType,
              _userBoolType, Types.VARCHAR, Types.VARCHAR, Types.CLOB, Types.VARCHAR });
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void renameAnalysis(long analysisId, String displayName) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_NAME_SQL, "update-step-analysis-name")
          .executeUpdate(new Object[] { displayName, analysisId });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

 @Override
  public void setUserNotes(long analysisId, String userNotes) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_USERNOTES_SQL, "update-step-analysis-usernotes")
          .executeUpdate(new Object[] { userNotes, analysisId });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void setHasParams(long analysisId, boolean hasParams) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_HAS_PARAMS_FLAG_SQL, "update-step-analysis-param-flag")
          .executeUpdate( new Object[] { hasParams, analysisId }, new Integer[] { _userBoolType, Types.INTEGER });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void setInvalidStepReason(long analysisId, String invalidStepReason) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_INVALID_STEP_REASON, "update-step-analysis-invalid-step-reason").executeUpdate(
          new Object[] { invalidStepReason, analysisId }, new Integer[] { Types.VARCHAR, Types.INTEGER });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void setState(long analysisId, StepAnalysisState state) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_NEW_FLAG_SQL, "update-step-analysis-new-flag").executeUpdate(
          new Object[] { state.getDbValue(), analysisId }, new Integer[] { _userBoolType, Types.INTEGER });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void updateInstance(long analysisId, String contextHash, String serializedContext)
      throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_CONTEXT_SQL, "update-step-analysis-context").executeUpdate(
          new Object[] { contextHash, serializedContext, analysisId },
          new Integer[] { Types.VARCHAR, Types.CLOB, Types.INTEGER });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  /**
   * Returns the properties CLOB as an InputStream if analysis with given ID is found, or null if not found
   */
  @Override
  public InputStream getProperties(long analysisId) throws WdkModelException {
    // NOTE: Cannot use SQLRunner here because we actually DON'T want to close connection, etc. in the
    //   success case. Must assume connection will be closed by the DatabaseResultStream after it is read.
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      conn = _userDs.getConnection();
      stmt = conn.prepareStatement(GET_ANALYSIS_PROPERTIES);
      stmt.setLong(1, analysisId);
      rs = stmt.executeQuery();
      if (rs.next()) {
        // successfully retrieved properties; underlying connection will be closed by caller
        return new DatabaseResultStream(conn, stmt, rs, "PROPERTIES");
      }
      // could not find row for this analysis ID; close resources and return null
      SqlUtils.closeQuietly(rs, stmt, conn);
      return null;
    }
    catch (Exception e) {
      // close only in failure case (not finally); if success, caller must close
      SqlUtils.closeQuietly(rs, stmt, conn);
      throw new WdkModelException(e);
    }
  }

  @Override
  public boolean setProperties(long analysisId, InputStream propertiesStream) throws WdkModelException {
    int rowsAffected = new SQLRunner(_userDs, SET_ANALYSIS_PROPERTIES, "set-step-analysis-props").executeUpdate(
        new Object[] { propertiesStream, analysisId },
        new Integer[] { Types.CLOB, Types.BIGINT });
    return rowsAffected > 0;
  }

  @Override
  public void deleteAnalysis(long analysisId) throws WdkModelException {
    try {
      int deleted = new SQLRunner(_userDs, DELETE_ANALYSIS_SQL, "delete-step-analysis")
          .executeUpdate(new Object[] { analysisId });
      if (deleted == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
    
  }

  @Override
  protected List<Long> getAnalysisIdsByStepId(long stepId) throws WdkModelException {
    try {
      List<Long> ids = new ArrayList<>();
      new SQLRunner(_userDs, GET_ANALYSIS_IDS_BY_STEP_SQL, "select-step-analysis-ids-by-step")
        .executeQuery(new Object[] { stepId }, rs -> { while (rs.next()) ids.add(rs.getLong(1)); });
      return ids;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  protected List<Long> getAnalysisIdsByHash(String contextHash) throws WdkModelException {
    try {
      List<Long> ids = new ArrayList<>();
      new SQLRunner(_userDs, GET_ANALYSIS_IDS_BY_HASH_SQL, "select-step-analyses-by-hash")
        .executeQuery(new Object[] { contextHash }, rs -> { while (rs.next()) ids.add(rs.getLong(1)); });
      return ids;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  protected List<Long> getAllAnalysisIds() throws WdkModelException {
    try {
      final List<Long> ids = new ArrayList<>();
      new SQLRunner(_userDs, GET_ALL_ANALYSIS_IDS_SQL, "select-all-step-analysis-ids")
        .executeQuery(rs -> { while (rs.next()) ids.add(rs.getLong(1)); });
      return ids;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  protected Map<Long, AnalysisInfoPlusStatus> getAnalysisInfoForIds(List<Long> analysisIds)
      throws WdkModelException {
    try {
      // data structures to build result
      final Map<String, List<Long>> hashToIdsMap = new LinkedHashMap<>();
      final Map<Long, AnalysisInfoPlusStatus> result = new LinkedHashMap<>();

      // don't query DB if no IDs passed
      if (analysisIds.isEmpty()) return result;
      
      // read data about analysis instances from user DB
      String valuesForIn = FormatUtil.join(analysisIds.toArray(), ", ");
      String sql = GET_ANALYSES_BY_IDS_SQL.replace(IN_CLAUSE_KEY, valuesForIn);
      new SQLRunner(_userDs, sql, "select-step-analysis-by-id").executeQuery(rs -> {
        while (rs.next()) {
          try {
            // read result row into an AnalysisInfo object
            Reader contextReader = rs.getCharacterStream(9);
            AnalysisInfo info = new AnalysisInfo(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                StepAnalysisState.valueOf(rs.getBoolean(5) ? 1 : 0), rs.getBoolean(6), rs.getString(7), rs.getString(8),
                contextReader == null ? null : IoUtil.readAllChars(contextReader));

            // add to result map
            result.put(info.analysisId, new AnalysisInfoPlusStatus(info));

            // add analysisId to list we'll later use to populate statuses
            List<Long> idListForContext = hashToIdsMap.get(info.contextHash);
            if (idListForContext == null) {
              idListForContext = new ArrayList<>();
              hashToIdsMap.put(info.contextHash, idListForContext);
            }
            idListForContext.add(info.analysisId);
          }
          catch (IllegalArgumentException | IOException ioe) {
            throw new SQLException("Unable to read context value.", ioe);
          }
        }
      });

      if (result.isEmpty()) return result;

      // read data about status from app DB (results cache table) and add to result
      valuesForIn = "'" + FormatUtil.join(hashToIdsMap.keySet().toArray(), "','") + "'";
      sql = GET_STATUSES_BY_HASHES_SQL.replace(IN_CLAUSE_KEY, valuesForIn);
      new SQLRunner(_appDs, sql, "select-step-analysis-by-ids").executeQuery(rs -> {
        while (rs.next()) {
          String hash = rs.getString(1);
          ExecutionStatus status = parseStatus(rs.getString(2), hash);
          for (long analysisId : hashToIdsMap.get(hash)) {
            result.get(analysisId).status = status;
          }
        }
      });

      // any statuses we couldn't find have been purged somehow; null is the
      //   appropriate value in that case and has already been set (by default)
      return result;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void createExecutionTable() throws WdkModelException {
    try {
      new SQLRunner(_appDs, CREATE_EXECUTION_TABLE_SQL, "create-step-analysis-cache-table").executeStatement();
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void deleteExecutionTable(boolean purge) throws WdkModelException {
    try {
      _appPlatform.dropTable(_appDs, null, EXECUTION_TABLE, purge);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public boolean insertExecution(String contextHash, ExecutionStatus status, Date startDate) throws WdkModelException {
    try {
      // lock to avoid multiple insertion race condition; DB will save
      //    us with a PK violation, but this is much cleaner
      CONTEXT_INSERTION_LOCK.lock();

      // check to see if execution already exists for this context hash; if so, return false
      BasicResultSetHandler result = new BasicResultSetHandler();
      new SQLRunner(_appDs, FIND_EXECUTION_SQL, "select-step-analysis-run").executeQuery(new Object[] { contextHash }, result);
      if (result.getNumRows() > 0) return false;

      // execution does not exist; create and return true
      new SQLRunner(_appDs, INSERT_EXECUTION_SQL, "insert-step-analysis-run").executeStatement(
          new Object[]{ contextHash,  status.name(), getTimestamp(startDate), getTimestamp(startDate) },
          new Integer[]{ Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP });
      return true;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
    finally {
      CONTEXT_INSERTION_LOCK.unlock();
    }
  }

  @Override
  public void updateExecution(String contextHash, ExecutionStatus status, Date updateDate, String charData, byte[] binData)
      throws WdkModelException {
    try {
      int changed = new SQLRunner(_appDs, UPDATE_EXECUTION_SQL, "update-step-analysis-run").executeUpdate(
          new Object[]{ status.name(), getTimestamp(updateDate), charData, binData, contextHash },
          new Integer[]{ Types.VARCHAR, Types.TIMESTAMP, Types.CLOB, _appPlatform.getBlobSqlType(), Types.VARCHAR });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
    
  }

  @Override
  public void resetStartDate(String contextHash, Date startDate) throws WdkModelException {
    try {
      int changed = new SQLRunner(_appDs, RESET_START_DATE_SQL, "update-step-analysis-run-start").executeUpdate(
          new Object[]{ getTimestamp(startDate), contextHash }, new Integer[]{ Types.TIMESTAMP, Types.VARCHAR });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void deleteExecution(String contextHash) throws WdkModelException {
    try {
      int changed = new SQLRunner(_appDs, DELETE_EXECUTION_SQL, "delete-step-analysis-run")
          .executeUpdate(new Object[] { contextHash });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
    
  }

  @Override
  public void deleteAllExecutions() throws WdkModelException {
    try {
      new SQLRunner(_appDs, DELETE_ALL_EXECUTIONS_SQL, "delete-all-step-analysis-runs").executeUpdate();
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
    
  }

  @Override
  protected ExecutionStatus getRawExecutionStatus(String contextHash) throws WdkModelException {
    try {
      BasicResultSetHandler result = new BasicResultSetHandler();
      new SQLRunner(_appDs, GET_STATUS_BY_HASH_SQL, "select-step-analysis-status").executeQuery(new Object[]{ contextHash }, result);
      if (result.getNumRows() == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
      return parseStatus((String)result.getResults().get(0).values().iterator().next(), contextHash);
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public AnalysisResult getRawAnalysisResult(final String contextHash) throws WdkModelException {
    try {
      final AnalysisResult[] resultContainer = new AnalysisResult[1];
      new SQLRunner(_appDs, GET_RESULTS_BY_HASH_SQL, "select-step-analysis-run").executeQuery(
        new Object[]{ contextHash }, new Integer[]{ Types.VARCHAR }, rs -> {
          try {
            if (rs.next()) {
              // parse values retrieved from database
              ExecutionStatus status = parseStatus(rs.getString(1), contextHash);
              Date startDate = new Date(rs.getTimestamp(2).getTime());
              Date updateDate = new Date(rs.getTimestamp(3).getTime());
              String log = IoUtil.readAllChars(rs.getCharacterStream(4));
              String charData = IoUtil.readAllChars(rs.getCharacterStream(5));
              byte[] binData = IoUtil.readAllBytes(rs.getBinaryStream(6));

              // construct result object, place in container and return
              resultContainer[0] = new AnalysisResult(status, startDate, updateDate, charData, binData, log);
            }
          }
          catch (IOException e) {
            throw new SQLException("Unable to read data from DB over stream field", e);
          }
        }
      );
      // return object retrieved, or null if not found
      return resultContainer[0];
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public List<ExecutionInfo> getAllRunningExecutions() throws WdkModelException {
    try {
      final List<ExecutionInfo> results = new ArrayList<>();
      new SQLRunner(_appDs, GET_RUNNING_EXECUTIONS_SQL, "select-all-step-analysis-runs").executeQuery(rs -> {
        while (rs.next()) {
          // parse values retrieved from database
          String contextHash = rs.getString(1);
          ExecutionStatus status = parseStatus(rs.getString(2), contextHash);
          Date startDate = new Date(rs.getTimestamp(3).getTime());
          Date updateDate = new Date(rs.getTimestamp(4).getTime());
          results.add(new ExecutionInfo(contextHash, status, startDate, updateDate));
        }
      });
      return results;
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public void setAnalysisLog(String contextHash, String str) throws WdkModelException {
    try {
      int changed = new SQLRunner(_appDs, SET_EXECUTION_LOG_SQL, "update-step-analysis-log").executeUpdate(
          new Object[]{ str, contextHash }, new Integer[]{ Types.CLOB, Types.VARCHAR });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  @Override
  public String getAnalysisLog(String contextHash) throws WdkModelException {
    try {
      final String[] resultContainer = new String[1];
      new SQLRunner(_appDs, GET_EXECUTION_LOG_SQL, "get-step-analysis-log-clob")
        .executeQuery(new Object[]{ contextHash }, new Integer[]{ Types.VARCHAR }, rs -> {
          if (rs.next()) {
            try {
              resultContainer[0] = IoUtil.readAllChars(rs.getCharacterStream(1));
            }
            catch (IOException e) {
              throw new SQLException("Unable to read data from DB over stream field", e);
            }
          }
        });
      return resultContainer[0];
    }
    catch (SQLRunnerException e) {
      throw new WdkModelException("Unable to complete operation.", e);
    }
  }

  private Timestamp getTimestamp(Date date) {
    return new Timestamp(date.getTime());
  }

  private ExecutionStatus parseStatus(String status, String hash) {
    if (status == null) return null;
    try {
      return ExecutionStatus.valueOf(status);
    }
    catch (IllegalArgumentException e) {
      throw new WdkRuntimeException("Status value [" + status + "] of hash [" +
          hash + "] is not a valid status value.  DB must be patched.");
    }
  }
}
