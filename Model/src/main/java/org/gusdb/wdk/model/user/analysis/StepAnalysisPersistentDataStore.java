package org.gusdb.wdk.model.user.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.ListArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.stream.BlobValueInputStream;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

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
public class StepAnalysisPersistentDataStore implements StepAnalysisDataStore {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(StepAnalysisPersistentDataStore.class);

  private static final String ANALYSIS_TABLE = "STEP_ANALYSIS";
  private static final String ANALYSIS_SEQUENCE_NAME = ANALYSIS_TABLE + "_PKSEQ";
  private static final String EXECUTION_TABLE = "STEP_ANALYSIS_RESULTS";

  // SQL to update and query analysis table
  private String ANALYSIS_SEQUENCE;
  private String CREATE_ANALYSIS_TABLE_SQL;
  private String INSERT_ANALYSIS_SQL;
  private String UPDATE_ANALYSIS_SQL;
  private String SET_STEPS_DIRTY_BY_STEP_ID_SQL;
  private String DELETE_ANALYSIS_BY_ID_SQL;
  private String DELETE_ANALYSIS_BY_STEP_ID_SQL;
  private String GET_ANALYSIS_BY_ID_SQL;
  private String GET_ANALYSES_BY_STEP_ID_SQL;
  private String GET_ANALYSIS_PROPERTIES;
  private String SET_ANALYSIS_PROPERTIES;

  // SQL to update and query execution table
  private String CREATE_EXECUTION_TABLE_SQL;
  private String INSERT_EXECUTION_SQL;
  private String UPDATE_EXECUTION_SQL;
  private String RESET_EXECUTION_SQL;
  private String DELETE_EXECUTION_SQL;
  private String DELETE_ALL_EXECUTIONS_SQL;
  private String GET_EXECUTION_INFO_BY_HASH_SQL;
  private String GET_EXECUTION_RESULTS_BY_HASH_SQL;
  private String GET_RUNNING_EXECUTION_INFOS_SQL;
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

  private static ReentrantLock EXECUTION_INSERTION_LOCK = new ReentrantLock();

  public StepAnalysisPersistentDataStore(WdkModel wdkModel) {

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
    ANALYSIS_SEQUENCE = _userSchema + ANALYSIS_SEQUENCE_NAME;
    String table = _userSchema + ANALYSIS_TABLE;
    String idType = _userPlatform.getNumberDataType(12);
    String intType = _userPlatform.getNumberDataType(2);
    String userStringType = _userPlatform.getStringDataType(1024);
    String userBigStringType = _userPlatform.getStringDataType(4000);
    String hashType = _userPlatform.getStringDataType(96);
    String boolType = _userPlatform.getBooleanDataType();
    String clobType = _userPlatform.getClobDataType();
    CREATE_ANALYSIS_TABLE_SQL =
        "CREATE TABLE " + table + " (" +
        "  ANALYSIS_ID          " + idType + " NOT NULL," +
        "  STEP_ID              " + idType + "," +
        "  DISPLAY_NAME         " + userStringType + "," +
        "  IS_NEW               " + intType + "," +        // repurposed to store revision status
        "  HAS_PARAMS           " + boolType + "," +       // deprecated
        "  INVALID_STEP_REASON  " + userStringType + "," + // deprecated
        "  CONTEXT_HASH         " + hashType + "," +       // deprecated
        "  CONTEXT              " + clobType + "," +
        "  PROPERTIES           " + clobType + "," +
        "  USER_NOTES           " + userBigStringType + "," +
        "  PRIMARY KEY (ANALYSIS_ID)" +
        ")";
    INSERT_ANALYSIS_SQL =
        "INSERT INTO " + table +
        " (ANALYSIS_ID, STEP_ID, DISPLAY_NAME, IS_NEW, HAS_PARAMS," +
        "  INVALID_STEP_REASON, CONTEXT_HASH, CONTEXT, USER_NOTES)" +
        " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    UPDATE_ANALYSIS_SQL =
        "UPDATE " + table +
        " SET DISPLAY_NAME = ?, IS_NEW = ?, CONTEXT = ?, USER_NOTES = ?" +
        " WHERE ANALYSIS_ID = ?";
    SET_STEPS_DIRTY_BY_STEP_ID_SQL =
        "UPDATE " + table +
        " SET IS_NEW = " + RevisionStatus.STEP_DIRTY.getDbValue() +
        " WHERE STEP_ID = ?";
    DELETE_ANALYSIS_BY_ID_SQL =
        "DELETE FROM " + table + " WHERE ANALYSIS_ID = ?";
    DELETE_ANALYSIS_BY_STEP_ID_SQL =
        "DELETE FROM " + table + " WHERE STEP_ID = ?";
    GET_ANALYSIS_BY_ID_SQL =
        "SELECT ANALYSIS_ID, STEP_ID, DISPLAY_NAME, USER_NOTES, IS_NEW, CONTEXT" +
        " FROM " + table +
        " WHERE ANALYSIS_ID = ?";
    GET_ANALYSES_BY_STEP_ID_SQL =
        "SELECT ANALYSIS_ID, STEP_ID, DISPLAY_NAME, USER_NOTES, IS_NEW, CONTEXT" +
        " FROM " + table +
        " WHERE STEP_ID = ? ORDER BY ANALYSIS_ID ASC";
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
    String intType = _appPlatform.getNumberDataType(10);
    String clobType = _appPlatform.getClobDataType();
    String blobType = _appPlatform.getBlobDataType();
    CREATE_EXECUTION_TABLE_SQL =
        "CREATE TABLE " + table + " (" +
        "  CONTEXT_HASH  " + hashType + " NOT NULL," +
        "  STATUS        " + statusType + "," +
        "  START_DATE    " + timestampType + "," +
        "  UPDATE_DATE   " + timestampType + "," +
        "  TIMEOUT_MINS  " + intType + "," +
        "  LOG           " + clobType + "," +
        "  CHAR_DATA     " + clobType + "," +
        "  BIN_DATA      " + blobType + "," +
        "  PRIMARY KEY (CONTEXT_HASH)" +
        ")";
    INSERT_EXECUTION_SQL =
        "INSERT INTO " + table +
        " (CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE, TIMEOUT_MINS)" +
        " VALUES (?, ?, ?, ?, ?)";
    UPDATE_EXECUTION_SQL =
        "UPDATE " + table +
        " SET STATUS = ?, UPDATE_DATE = ?, CHAR_DATA = ?, BIN_DATA = ?" +
        " WHERE CONTEXT_HASH = ?";
    RESET_EXECUTION_SQL =
        "UPDATE " + table +
        " SET STATUS = ?, START_DATE = ?, UPDATE_DATE = ?, CHAR_DATA = null, BIN_DATA = null, LOG = null" +
        " WHERE CONTEXT_HASH = ?";
    DELETE_EXECUTION_SQL =
        "DELETE FROM " + table + " WHERE CONTEXT_HASH = ?";
    DELETE_ALL_EXECUTIONS_SQL =
        "DELETE FROM " + table;
    GET_EXECUTION_INFO_BY_HASH_SQL =
        "SELECT CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE, TIMEOUT_MINS" +
        " FROM " + table + " WHERE CONTEXT_HASH = ?";
    GET_EXECUTION_RESULTS_BY_HASH_SQL =
        "SELECT CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE, TIMEOUT_MINS, CHAR_DATA, BIN_DATA, LOG" +
        " FROM " + table + " WHERE CONTEXT_HASH = ?";
    GET_RUNNING_EXECUTION_INFOS_SQL =
        "SELECT CONTEXT_HASH, STATUS, START_DATE, UPDATE_DATE, TIMEOUT_MINS" +
        " FROM " + table +
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
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void insertAnalysis(long analysisId, long stepId, String displayName,
      RevisionStatus revisionStatus, String serializedContext, String userNotes) throws WdkModelException {
    try {
      new SQLRunner(_userDs, INSERT_ANALYSIS_SQL, "insert-step-analysis").executeStatement(
        new Object[] {
          analysisId,
          stepId,
          displayName,
          revisionStatus.getDbValue(),
          _userPlatform.convertBoolean(true),
          "",
          "",
          serializedContext,
          userNotes
        },
        new Integer[] { Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.INTEGER,
          _userBoolType, Types.VARCHAR, Types.VARCHAR, Types.CLOB, Types.VARCHAR });
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void updateAnalysis(long analysisId, String displayName, RevisionStatus revisionStatus,
      String serializedContext, String userNotes) throws WdkModelException {
    try {
      int changed = new SQLRunner(_userDs, UPDATE_ANALYSIS_SQL, "update-step-analysis").executeUpdate(
        new Object[] {
          displayName,
          revisionStatus.getDbValue(),
          serializedContext,
          userNotes,
          analysisId
        },
        new Integer[] { Types.VARCHAR, Types.INTEGER, Types.CLOB, Types.VARCHAR, Types.BIGINT });
      if (changed == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void setStepsDirty(Long... stepIds) throws WdkModelException {
    try {
      ListArgumentBatch idBatch = new ListArgumentBatch()
        .setBatchSize(100)
        .setParameterTypes(new Integer[]{ Types.BIGINT });
      Arrays.stream(stepIds)
        .map(id -> new Object[]{ id })
        .forEach(arr -> idBatch.add(arr));
      new SQLRunner(_userDs, SET_STEPS_DIRTY_BY_STEP_ID_SQL, "set-analysis-steps-dirty")
        .executeStatementBatch(idBatch);
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void deleteAnalysisById(long analysisId) throws WdkModelException {
    try {
      int deleted = new SQLRunner(_userDs, DELETE_ANALYSIS_BY_ID_SQL, "delete-analysis-by-id")
          .executeUpdate(new Object[] { analysisId }, new Integer[] { Types.BIGINT });
      if (deleted == 0) {
        throw new WdkModelException("Could not find analysis with id " + analysisId);
      }
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void deleteAnalysesByStepId(long stepId) throws WdkModelException {
    try {
      new SQLRunner(_userDs, DELETE_ANALYSIS_BY_STEP_ID_SQL, "delete-analyses-by-step-id")
          .executeUpdate(new Object[] { stepId }, new Integer[] { Types.BIGINT });
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public Optional<StepAnalysisInstance> getInstanceById(User requestingUser, long analysisId, ValidationLevel level)
      throws WdkModelException {
    try {
      return new SQLRunner(_userDs, GET_ANALYSIS_BY_ID_SQL, "find-analysis-by-id")
        .executeQuery(new Object[] { analysisId }, new Integer[] { Types.BIGINT }, rs -> {
          try {
            if (!rs.next()) {
              // no rows found
              return Optional.empty();
            }
            StepAnalysisInstance instance = readInstance(rs, requestingUser, level);
            if (rs.next()) {
              // more than one row
              throw new SQLRunnerException("Found more than one row for analysisId " +
                  analysisId + ". This should be prevented by PK constraint.");
            }
            return Optional.of(instance);
          }
          catch (WdkModelException e) {
            throw new SQLRunnerException(e);
          }
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  @Override
  public List<StepAnalysisInstance> getInstancesByStep(Step step, ValidationLevel level)
      throws WdkModelException {
    try {
      return new SQLRunner(_userDs, GET_ANALYSES_BY_STEP_ID_SQL, "find-analysis-by-step-id")
        .executeQuery(new Object[] { step.getStepId() }, new Integer[] { Types.BIGINT }, rs -> {
          try {
            List<StepAnalysisInstance> instanceList = new ArrayList<>();
            while(rs.next()) {
              instanceList.add(readInstance(rs, step.getRequestingUser(), level));
            }
            return instanceList;
          }
          catch (WdkModelException e) {
            throw new SQLRunnerException(e);
          }
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  // SELECT ANALYSIS_ID, STEP_ID, DISPLAY_NAME, USER_NOTES, IS_NEW, CONTEXT
  // WdkModel wdkModel, long analysisId, long stepId, RevisionStatus revisionStatus, String displayName, String userNotes, String serializedInstance, ValidationLevel validationLevel
  private StepAnalysisInstance readInstance(ResultSet rs, User user, ValidationLevel level)
      throws WdkModelException, SQLException {
    return StepAnalysisInstance.createFromStoredData(
        user,
        rs.getLong(1),
        rs.getLong(2),
        RevisionStatus.valueOf(rs.getInt(5)),
        rs.getString(3),
        rs.getString(4),
        _userPlatform.getClobData(rs, "CONTEXT"),
        level);
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
        return new BlobValueInputStream(conn, stmt, rs, "PROPERTIES");
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
  public void createExecutionTable() throws WdkModelException {
    try {
      new SQLRunner(_appDs, CREATE_EXECUTION_TABLE_SQL, "create-step-analysis-cache-table").executeStatement();
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
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
  public Optional<ExecutionInfo> insertExecution(String contextHash, ExecutionStatus status, Date startDate, int timeoutMinutes) throws WdkModelException {
    try {
      // lock to avoid multiple insertion race condition; DB will save
      //    us with a PK violation, but this is much cleaner
      EXECUTION_INSERTION_LOCK.lock();

      // check to see if execution already exists for this context hash; if so, return it
      Optional<ExecutionInfo> existingStatus = getAnalysisStatus(contextHash);

      if (existingStatus.isPresent()) {
        return existingStatus;
      }

      // execution does not exist; insert empty run and return an empty optional
      new SQLRunner(_appDs, INSERT_EXECUTION_SQL, "insert-step-analysis-run").executeStatement(
          new Object[]{ contextHash,  status.name(), getTimestamp(startDate), getTimestamp(startDate), timeoutMinutes },
          new Integer[]{ Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP, Types.INTEGER });

      return Optional.empty();
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
    finally {
      EXECUTION_INSERTION_LOCK.unlock();
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
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void resetExecution(String contextHash, ExecutionStatus status, Date newStartDate)
      throws WdkModelException {
    try {
      Timestamp newTimestamp = getTimestamp(newStartDate);
      int changed = new SQLRunner(_appDs, RESET_EXECUTION_SQL, "reset-step-analysis-run").executeUpdate(
          new Object[]{ status.name(), newTimestamp, newTimestamp, contextHash },
          new Integer[]{ Types.VARCHAR, Types.TIMESTAMP, Types.TIMESTAMP, Types.VARCHAR });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void deleteExecution(String contextHash) throws WdkModelException {
    try {
      int changed = new SQLRunner(_appDs, DELETE_EXECUTION_SQL, "delete-step-analysis-run")
          .executeUpdate(new Object[] { contextHash }, new Integer[] { Types.VARCHAR });
      if (changed == 0) {
        throw new WdkModelException("Unable to find execution with context hash " + contextHash);
      }
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public void deleteAllExecutions() throws WdkModelException {
    try {
      new SQLRunner(_appDs, DELETE_ALL_EXECUTIONS_SQL, "delete-all-step-analysis-runs").executeUpdate();
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public Optional<ExecutionInfo> getAnalysisStatus(String contextHash) throws WdkModelException {
    try {
      return new SQLRunner(_appDs, GET_EXECUTION_INFO_BY_HASH_SQL, "select-step-analysis-status")
        .executeQuery(new Object[]{ contextHash }, new Integer[]{ Types.VARCHAR }, rs -> {
          if (!rs.next()) {
            return Optional.empty();
          }
          ExecutionInfo info = parseInfo(rs);
          if (rs.next()) {
            // more than one row
            throw new SQLRunnerException("Found more than one row for contextHash " +
                contextHash + ". This should be prevented by PK constraint.");
          }
          return Optional.of(info);
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  @Override
  public Optional<ExecutionResult> getAnalysisResult(String contextHash) throws WdkModelException {
    try {
      return new SQLRunner(_appDs, GET_EXECUTION_RESULTS_BY_HASH_SQL, "select-step-analysis-results")
        .executeQuery(new Object[]{ contextHash }, new Integer[]{ Types.VARCHAR }, rs -> {
          try {
            if (!rs.next()) {
              return Optional.empty();
            }
            ExecutionResult result = parseResult(rs);
            if (rs.next()) {
              // more than one row
              throw new SQLRunnerException("Found more than one row for contextHash " +
                  contextHash + ". This should be prevented by PK constraint.");
            }
            return Optional.of(result);
          }
          catch (IOException e) {
            throw new SQLException("Unable to read data from DB over stream field", e);
          }
        }
      );
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  @Override
  public List<ExecutionInfo> getAllRunningExecutions() throws WdkModelException {
    try {
      return new SQLRunner(_appDs, GET_RUNNING_EXECUTION_INFOS_SQL, "select-all-running-analysis-executions")
        .executeQuery(rs -> {
          List<ExecutionInfo> results = new ArrayList<>();
          while (rs.next()) {
            results.add(parseInfo(rs));
          }
          return results;
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  private static ExecutionInfo parseInfo(ResultSet rs) throws SQLException {
    return new ExecutionInfo(
      rs.getString(1),
      ExecutionStatus.valueOf(rs.getString(2)),
      new Date(rs.getTimestamp(3).getTime()),
      new Date(rs.getTimestamp(4).getTime()),
      rs.getInt(5));
  }

  private static ExecutionResult parseResult(ResultSet rs) throws IOException, SQLException {
    // parse fields common to ExecutionInfo class
    ExecutionInfo info = parseInfo(rs);
    // read extra result fields
    String charData = IoUtil.readAllChars(rs.getCharacterStream(6));
    byte[] binData = IoUtil.readAllBytes(rs.getBinaryStream(7));
    String log = IoUtil.readAllChars(rs.getCharacterStream(8));
    return new ExecutionResult(
      info.getContextHash(),
      info.getStatus(),
      info.getStartDate(),
      info.getUpdateDate(),
      info.getTimeoutMins(),
      charData,
      binData,
      log
    );
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
      WdkModelException.unwrap(e);
    }
  }

  @Override
  public String getAnalysisLog(String contextHash) throws WdkModelException {
    try {
      return new SQLRunner(_appDs, GET_EXECUTION_LOG_SQL, "get-step-analysis-log-clob")
        .executeQuery(new Object[]{ contextHash }, new Integer[]{ Types.VARCHAR }, rs -> {
          if (rs.next()) {
            try {
              return IoUtil.readAllChars(rs.getCharacterStream(1));
            }
            catch (IOException e) {
              throw new SQLException("Unable to read data from DB over stream field", e);
            }
          }
          return null;
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  private static Timestamp getTimestamp(Date date) {
    return new Timestamp(date.getTime());
  }

}
