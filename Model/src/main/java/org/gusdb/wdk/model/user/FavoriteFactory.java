package org.gusdb.wdk.model.user;

import static java.util.Arrays.asList;
import static org.gusdb.fgputil.ArrayUtil.concatenate;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.ListBuilder.asList;
import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.mapToListWithIndex;
import static org.gusdb.wdk.model.Utilities.COLUMN_PK_PREFIX;
import static org.gusdb.wdk.model.user.UserFactory.USER_SCHEMA_MACRO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;

public class FavoriteFactory {

  private static final Logger logger = Logger.getLogger(FavoriteFactory.class);

  private static final String TABLE_FAVORITES = "favorites";
  private static final String COLUMN_FAVORITE_ID = "favorite_id";
  private static final String COLUMN_USER_ID = "user_id";
  private static final String COLUMN_PROJECT_ID = "project_id";
  private static final String COLUMN_RECORD_CLASS = "record_class";
  private static final String COLUMN_RECORD_NOTE = "record_note";
  private static final String COLUMN_RECORD_GROUP = "record_group";
  private static final String COLUMN_IS_DELETED = "is_deleted";

  private static final int BATCH_SIZE = 100;

  private static final String PK_COLUMNS_MACRO = "$$PK_COLUMNS_MACROS$$";
  private static final String PK_WILDCARDS_MACRO = "$$PK_WILDCARDS_MACRO$$";
  private static final String PK_PREDICATE_MACRO = "$$PK_PREDICATE_MACRO$$";
  private static final String IS_DELETED_SETTER_VALUE_MACRO = "$$IS_DELETED_SETTER_VALUE$$";
  private static final String IS_DELETED_CONDITIONAL_VALUE_MACRO = "$$IS_DELETED_CONDITIONAL_VALUE$$";

  private static final String SELECT_FAVORITES_SQL =
      "SELECT * FROM " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " WHERE " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?" +
      "   AND " + COLUMN_IS_DELETED + " = " + IS_DELETED_CONDITIONAL_VALUE_MACRO +
      " ORDER BY " + COLUMN_RECORD_GROUP;

  private static final String SELECT_FAVORITE_BY_ID_SQL =
      "SELECT * FROM " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " WHERE " + COLUMN_FAVORITE_ID + "= ?" +
      "   AND " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?";

  private static final String SELECT_FAVORITE_BY_PK_VALUES_SQL =
      "SELECT * FROM " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " WHERE " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?" +
      "   AND " + COLUMN_RECORD_CLASS + " = ?" +
      PK_PREDICATE_MACRO;

  private static final String DELETE_ALL_FAVORITES_SQL =
      "UPDATE " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " SET " + COLUMN_IS_DELETED + " = " + IS_DELETED_SETTER_VALUE_MACRO +
      " WHERE " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?";

  private static final String SET_IS_DELETED_BY_ID_SQL =
      "UPDATE " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " SET " + COLUMN_IS_DELETED + " = " + IS_DELETED_SETTER_VALUE_MACRO +
      " WHERE " + COLUMN_FAVORITE_ID + "= ?" +
      "   AND " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?" +
      "   AND " + COLUMN_IS_DELETED + " = " + IS_DELETED_CONDITIONAL_VALUE_MACRO;

  private static final String UPDATE_FAVORITE_BY_ID_SQL =
      "UPDATE "  + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " SET " + COLUMN_RECORD_NOTE + " = ?, " + COLUMN_RECORD_GROUP + " = ? " +
      " WHERE " + COLUMN_FAVORITE_ID + "= ?" +
      "   AND " + COLUMN_USER_ID + " = ?" +
      "   AND " + COLUMN_PROJECT_ID + " = ?" +
      "   AND " + COLUMN_IS_DELETED + " = " + IS_DELETED_CONDITIONAL_VALUE_MACRO;

  private static final String INSERT_FAVORITE_SQL =
      "INSERT INTO " + USER_SCHEMA_MACRO + TABLE_FAVORITES +
      " ( " + COLUMN_FAVORITE_ID +
      " , " + COLUMN_USER_ID +
      " , " + COLUMN_PROJECT_ID  +
      " , " + COLUMN_RECORD_CLASS +
      " , " + COLUMN_RECORD_NOTE +
      " , " + COLUMN_RECORD_GROUP +
      " , " + COLUMN_IS_DELETED +
      PK_COLUMNS_MACRO +
      " ) VALUES ( ?, ?, ?, ?, ?, ?, " + IS_DELETED_SETTER_VALUE_MACRO + PK_WILDCARDS_MACRO + " )";

  public static interface NoteAndGroup {
    public String getNote();
    public String getGroup();
  }

  private final WdkModel _wdkModel;
  private final DatabaseInstance _userDb;
  private final String _userSchema;

  public FavoriteFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _userDb = wdkModel.getUserDb();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  /**
   * Returns a list of all undeleted favorites (i.e., the is_deleted flag is not raised)
   * for the user provided
   *
   * @param user
   * @return - list of favorites
   * @throws WdkModelException
   */
  public List<Favorite> getAllFavorites(User user) throws WdkModelException {
    try {
      String sql = SELECT_FAVORITES_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_DELETED_CONDITIONAL_VALUE_MACRO, _userDb.getPlatform().convertBoolean(false).toString());
      return new SQLRunner(_userDb.getDataSource(), sql, "select-undeleted-favorites").executeQuery(
        new Object[]{ user.getUserId(), _wdkModel.getProjectId() },
        new Integer[]{ Types.BIGINT, Types.VARCHAR },
        resultSet -> {
          try {
            ListBuilder<Favorite> favorites = new ListBuilder<>();
            while (resultSet.next()) {
              Favorite favorite = loadFavorite(user, resultSet);
              favorites.addIf(fav -> fav != null, favorite);
            }
            return favorites.toList();
          }
          catch (WdkModelException e) {
            throw new SQLRunnerException(e);
          }
        }
      );
    }
    catch(SQLRunnerException sre) {
      return WdkModelException.unwrap(sre, List.class);
    }
    catch(Exception e) {
      throw new WdkModelException("Unable to load favorites for user " + user.getUserId(), e);
    }
  }

  /**
   * Loads favorite from the current row of the result set, or null if favorite contains an invalid
   * recordclass.
   *
   * @param user
   * @param resultSet
   * @return
   * @throws SQLException
   * @throws WdkModelException
   */
  private Favorite loadFavorite(User user, ResultSet resultSet)
      throws SQLException, WdkModelException {
    // Need to avoid showing favorite for defunct (per current wdk model) record class sets or record classes
    RecordClass recordClass;
    if ((recordClass = getRecordClassOrNull(resultSet.getString(COLUMN_RECORD_CLASS))) == null) {
      return null;
    }
    Map<String,Object> primaryKeys = loadPrimaryKeys(recordClass, resultSet);
    PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), primaryKeys);
    Long id = resultSet.getLong(COLUMN_FAVORITE_ID);
    Favorite favorite = new Favorite(user, recordClass, pkValue, id);
    favorite.setNote(resultSet.getString(COLUMN_RECORD_NOTE));
    favorite.setGroup(resultSet.getString(COLUMN_RECORD_GROUP));
    favorite.setDeleted(_userDb.getPlatform().getBooleanValue(resultSet, COLUMN_IS_DELETED, false));
    return favorite;
  }

  private static Map<String, Object> loadPrimaryKeys(RecordClass recordClass, ResultSet resultSet) throws SQLException {
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
    for (int i = 1; i <= pkColumns.length; i++) {
      String value = resultSet.getString(COLUMN_PK_PREFIX + i);
      primaryKeys.put(pkColumns[i - 1], value);
    }
    return primaryKeys;
}

  private RecordClass getRecordClassOrNull(String recordClassReference) {
    try {
      return _wdkModel.getRecordClass(recordClassReference);
    }
    catch (WdkModelException e) {
      return null;
    }
  }

  /**
   * Gets an undeleted favorite by favorite id if owned by the given user.  Returns null if favorite does not
   * exist or cannot be used for some other reason (e.g. recordclass no longer valid).
   *
   * @param user
   * @param favoriteId
   * @return
   * @throws WdkModelException
   */
  public Favorite getFavorite(User user, Long favoriteId) throws WdkModelException {
    return getFavorite(user, favoriteId, false);
  }

  private Favorite getFavorite(User user, Long favoriteId, boolean includeDeleted) throws WdkModelException {
    long userId = user.getUserId();
    try {
      String selectFavoriteSql = SELECT_FAVORITE_BY_ID_SQL.replace(USER_SCHEMA_MACRO, _userSchema);
      return new SQLRunner(_userDb.getDataSource(), selectFavoriteSql, "select-favorite-by-id").executeQuery(
        new Object[]{ favoriteId, userId, _wdkModel.getProjectId() },
        new Integer[]{ Types.BIGINT, Types.BIGINT, Types.VARCHAR },
        resultSet -> loadFavorite(resultSet, user, includeDeleted)
      );
    }
    catch(SQLRunnerException sre) {
      return WdkModelException.unwrap(sre, Favorite.class);
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }

  private Favorite loadFavorite(ResultSet resultSet, User user, boolean includeDeleted) throws SQLException {
    try {
      if (resultSet.next()) {
        Favorite favorite = loadFavorite(user, resultSet);
        if (favorite != null && (includeDeleted || !favorite.isDeleted())) {
          return favorite;
        }
      }
      return null;
    }
    catch (WdkModelException e) {
      throw new SQLRunnerException(e);
    }
  }

  /**
   * Returns favorite if found, or null if not found
   *
   * @param user owner of the desired favorite
   * @param recordClass recordclass of the desired favorite
   * @param recordId primary key values of the desired favorite
   * @return favorite if found or null if not
   * @throws WdkModelException if error occurs
   */
  public Favorite getFavorite(User user, RecordClass recordClass, Map<String, Object> recordId) throws WdkModelException {
    return getFavorite(user, recordClass, recordId, false);
  }

  private Favorite getFavorite(User user, RecordClass recordClass, Map<String, Object> recordId, boolean includeDeleted) {

    // create PK-dependent values
    List<String> pkColumns = asList(recordClass.getPrimaryKeyDefinition().getColumnRefs());
    String pkPredicate = join(mapToListWithIndex(pkColumns,
        (column, index) -> (" AND " + COLUMN_PK_PREFIX + (index + 1) + " = ?")),"");
    Object[] pkValues = mapToList(pkColumns, col -> recordId.get(col)).toArray();
    Integer[] pkTypes = mapToList(pkColumns, col -> Types.VARCHAR).toArray(new Integer[0]);

    // build SQL
    String sql = SELECT_FAVORITE_BY_PK_VALUES_SQL
        .replace(USER_SCHEMA_MACRO, _userSchema)
        .replace(PK_PREDICATE_MACRO, pkPredicate);

    // fetch favorite and return
    return new SQLRunner(_userDb.getDataSource(), sql, "wdk-favorite-instance-query")
      .executeQuery(
        concatenate(new Object[]{ user.getUserId(), _wdkModel.getProjectId(), recordClass.getFullName() }, pkValues),
        concatenate(new Integer[]{ Types.BIGINT, Types.VARCHAR, Types.VARCHAR }, pkTypes),
        resultSet -> loadFavorite(resultSet, user, includeDeleted)
      );
  }

  /**
   * Deletes all of a given user's favorites (i.e. is_deleted flag is raised)
   *
   * @param user
   * @return - number of deletions
   * @throws WdkModelException
   */
  public int deleteAllFavorites(User user) throws WdkModelException {
    try {
      String sql = DELETE_ALL_FAVORITES_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_DELETED_SETTER_VALUE_MACRO, _userDb.getPlatform().convertBoolean(true).toString());
      return new SQLRunner(_userDb.getDataSource(), sql, "delete-all-favorites").executeUpdate(
          new Object[]{ user.getUserId(), _wdkModel.getProjectId() },
          new Integer[]{ Types.BIGINT, Types.VARCHAR });
    }
    catch(SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Transaction-safe batch operation to delete multiple favorites.  Only those favorites owned by the user
   * and set on the current project will be deleted (i.e. is_deleted set to true).
   *
   * @param user owner of the favorites
   * @param favoriteIds list of favorite IDs to delete
   * @return number of favorites affected
   * @throws WdkModelException
   */
  public int deleteFavorites(User user, List<Long> favoriteIds) throws WdkModelException {
    return setDeletedFlag(user, favoriteIds, true);
  }

  /**
   * Transaction-safe batch operation to undelete multiple favorites.  Only those favorites owned by the user
   * and set on the current project will be undeleted (i.e. is_deleted set to false).
   *
   * @param user owner of the favorites
   * @param favoriteIds list of favorite IDs to delete
   * @return number of favorites affected
   * @throws WdkModelException
   */
  public int undeleteFavorites(User user, List<Long> favoriteIds) throws WdkModelException {
    return setDeletedFlag(user, favoriteIds, false);
  }

  private int setDeletedFlag(User user, List<Long> favoriteIds, boolean isDeletedValue) throws WdkModelException {
    Connection conn = null;
    try {
      conn = _userDb.getDataSource().getConnection();
      Connection finalConn = conn;
      Wrapper<Integer> updateCountWrapper = new Wrapper<>();
      String sql = SET_IS_DELETED_BY_ID_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_DELETED_SETTER_VALUE_MACRO, _userDb.getPlatform().convertBoolean(isDeletedValue).toString())
          .replace(IS_DELETED_CONDITIONAL_VALUE_MACRO, _userDb.getPlatform().convertBoolean(!isDeletedValue).toString());
      SqlUtils.performInTransaction(conn, () -> {
        BasicArgumentBatch batch = new BasicArgumentBatch();
        batch.setBatchSize(BATCH_SIZE);
        batch.setParameterTypes(new Integer[]{ Types.BIGINT, Types.BIGINT, Types.VARCHAR });
        for (Long favoriteId : favoriteIds) {
          batch.add(new Object[] { favoriteId, user.getUserId(), _wdkModel.getProjectId() });
        }
        updateCountWrapper.set(new SQLRunner(finalConn, sql, "delete-favorites-by-id").executeUpdateBatch(batch));
      });
      return updateCountWrapper.get();
    }
    catch (SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception ex) {
      throw new WdkModelException(ex);
    }
    finally {
      SqlUtils.closeQuietly(conn);
    }
  }

  /**
   * Updates the favorite's note and group to the passed values
   *
   * @param user owner of the favorite
   * @param favoriteId favorite ID
   * @param note new note
   * @param group new group
   * @return true if data written, else false (perhaps did not find the favorite)
   * @throws WdkModelException
   */
  public boolean editFavorite(User user, long favoriteId, String note, String group) throws WdkModelException {
    try {
      String sql = UPDATE_FAVORITE_BY_ID_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_DELETED_CONDITIONAL_VALUE_MACRO, _userDb.getPlatform().convertBoolean(false).toString());
      int affectedRows = new SQLRunner(_userDb.getDataSource(), sql, "edit-favorite").executeUpdate(
          new Object[]{ note, group, favoriteId, user.getUserId(), _wdkModel.getProjectId() },
          new Integer[]{ Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.BIGINT, Types.VARCHAR });
      if (affectedRows > 1) {
        throw new WdkModelException("More than one favorite exists for ID " + favoriteId + " and user " + user.getUserId());
      }
      return affectedRows > 0;
    }
    catch (SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception ex) {
      throw new WdkModelException(ex);
    }
  }

  public Favorite addToFavorites(User user, RecordClass recordClass, Map<String, Object> recordId) throws WdkModelException {
    try {

      // get favorite for these PK values if it exists and use, even if is_deleted is true
      Favorite favorite = getFavorite(user, recordClass, recordId, true);
      if (favorite != null) {
        if (favorite.isDeleted()) {
          undeleteFavorites(user, asList(favorite.getFavoriteId()));
          favorite.setDeleted(false);
        }
        return favorite;
      }

      // create PK-dependent values
      List<String> pkColumns = asList(recordClass.getPrimaryKeyDefinition().getColumnRefs());
      String pkColumnsText = join(mapToListWithIndex(pkColumns,
          (column, index) -> (" , " + COLUMN_PK_PREFIX + (index + 1))),"");
      String pkWildcardText = join(mapToList(pkColumns, col -> ", ?"),"");
      Object[] pkValues = mapToList(pkColumns, col -> recordId.get(col)).toArray();
      Integer[] pkTypes = mapToList(pkColumns, col -> Types.VARCHAR).toArray(new Integer[0]);

      // generate SQL
      String sql = INSERT_FAVORITE_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(PK_COLUMNS_MACRO, pkColumnsText)
          .replace(IS_DELETED_SETTER_VALUE_MACRO, _userDb.getPlatform().convertBoolean(false).toString())
          .replace(PK_WILDCARDS_MACRO, pkWildcardText);

      // set up non-PK values
      DataSource userDs = _userDb.getDataSource();
      long favoriteId = _userDb.getPlatform().getNextId(userDs, _userSchema, TABLE_FAVORITES);
      String initialNote = createInitialNote(user, recordClass, recordId);
      String initialGroup = "";
      Object[] paramValues = new Object[]{
          favoriteId,
          user.getUserId(),
          _wdkModel.getProjectId(),
          recordClass.getFullName(),
          initialNote,
          initialGroup
      };
      Integer[] paramTypes = new Integer[]{
          Types.BIGINT, Types.BIGINT, Types.VARCHAR,
          Types.VARCHAR, Types.VARCHAR, Types.VARCHAR
      };

      // insert new favorite
      new SQLRunner(userDs, sql, "insert-new-favorite").executeUpdate(
          concatenate(paramValues, pkValues),
          concatenate(paramTypes, pkTypes)
      );

      // create a favorite object to return
      PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), recordId);
      favorite = new Favorite(user, recordClass, pkValue, favoriteId);
      favorite.setNote(initialNote);
      favorite.setGroup(initialGroup);
      return favorite;
    }
    catch(SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }

  private static String createInitialNote(User user, RecordClass recordClass, Map<String,Object> pkValues) throws WdkModelException, WdkUserException {
    //get the default favorite note
    AttributeField noteField = recordClass.getFavoriteNoteField();
    String note = null;
    if (noteField != null) {
      RecordInstance instance = new DynamicRecordInstance(user, recordClass, pkValues);
      AttributeValue noteValue = instance.getAttributeValue(noteField.getName());
      Object value = noteValue.getValue();
      note = (value != null) ? value.toString() : "";
    }
    return note;
  }

  /**
   * @param user
   * @param recordClass
   * @param recordIds
   *          a list of primary key values. the inner map is a primary-key
   *          column-value map.
   * @throws WdkUserException
   */
  @Deprecated // pending struts removal
  public void addToFavorite(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds) throws WdkModelException, WdkUserException {
    for (Map<String,Object> pkSet : recordIds) {
      // this is not very efficient but will do since only going for backward compatibility; all
      // actual operations should be performed by the service now
      addToFavorites(user, recordClass, pkSet);
    }
  }

  @Deprecated // pending struts removal
  public void removeFromFavorite(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlDelete = "DELETE FROM " + _userSchema + TABLE_FAVORITES + " WHERE "
        + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
        + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlDelete += " AND " + COLUMN_PK_PREFIX + i + " = ?";
    }

    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psDelete = null;
    try {
      psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
      int count = 0;
      for (Map<String, Object> recordId : recordIds) {
        setParams(psDelete, userId, projectId, rcName, pkColumns, recordId, 1);
        psDelete.addBatch();
        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psDelete.executeBatch();
          QueryLogger.logEndStatementExecution(sqlDelete,
              "wdk-favorite-delete", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psDelete.executeBatch();
        QueryLogger.logEndStatementExecution(sqlDelete, "wdk-favorite-delete",
            -start);
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not remove favorite(s) for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  @Deprecated // pending struts removal
  public int getFavoriteCounts(User user) throws WdkModelException {
    // load the unique counts
    String sql = "SELECT count(*) AS fav_size FROM " + _userSchema + TABLE_FAVORITES +
        " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_IS_DELETED +
        " = " + _userDb.getPlatform().convertBoolean(false);
    DataSource ds = _userDb.getDataSource();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int count = 0;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setLong(1, user.getUserId());
      ps.setString(2, _wdkModel.getProjectId());
      rs = ps.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-favorite-count", start);
      if (rs.next()) {
        count = rs.getInt("fav_size");
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not get favorite counts for user "
          + user.getUserId(), e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs, ps);
    }
    return count;
  }

  @Deprecated // pending struts removal
  public int getFavoriteCount(User user, List<Map<String, Object>> records, RecordClass recordClass) {
    int count = 0;
    for (Map<String, Object> item : records) {
      boolean inFavs = isInFavorite(user, recordClass, item);
      if (logger.isDebugEnabled()) {
        logger.debug("Is " + convert(item) + " in favorites? " + inFavs);
      }
      if (inFavs) {
        count++;
      }
    }
    return count;
  }

  private static String convert(Map<String, Object> item) {
    StringBuilder sb = new StringBuilder("Map { ");
    for (String s : item.keySet()) {
      sb.append("{ ").append(s).append(", ").append(item.get(s)).append(" },");
    }
    sb.append(" }");
    return sb.toString();
  }

  @Deprecated // pending struts removal
  public Map<RecordClass, List<Favorite>> getFavorites(User user) throws WdkModelException {
    List<Favorite> favorites = getAllFavorites(user);
    // sort into map
    Map<RecordClass, List<Favorite>> map = new HashMap<>();
    for (Favorite fav : favorites) {
      RecordClass rc = fav.getRecordClass();
      List<Favorite> list = map.get(rc);
      if (list == null) {
        list = new ArrayList<>();
        map.put(rc, list);
      }
      list.add(fav);
    }
    return map;
  }

  @Deprecated // pending struts removal
  public boolean isInFavorite(User user, RecordClass recordClass,
      Map<String, Object> recordId) {
    return getFavorite(user, recordClass, recordId, false) != null;
  }

  @Deprecated // pending struts removal
  public void setNotes(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds, String note)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sql = "UPDATE " + _userSchema + TABLE_FAVORITES + " SET "
        + COLUMN_RECORD_NOTE + " = ? WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sql += " AND " + COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psUpdate = null;
    try {
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);

      int count = 0;
      for (Map<String, Object> recordId : recordIds) {
        // check if the record already exists.
        psUpdate.setString(1, note);
        setParams(psUpdate, userId, projectId, rcName, pkColumns, recordId, 2);
        psUpdate.addBatch();
        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psUpdate.executeBatch();
          QueryLogger.logEndStatementExecution(sql, "wdk-favorite-update-note",
              start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psUpdate.executeBatch();
        QueryLogger.logEndStatementExecution(sql, "wdk-favorite-update-note",
            -start);
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not set favorite note for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }

  @Deprecated // pending struts removal
  public void setGroups(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds, String group)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = _wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sql = "UPDATE " + _userSchema + TABLE_FAVORITES + " SET "
        + COLUMN_RECORD_GROUP + " = ? WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sql += " AND " + COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psUpdate = null;
    try {
      psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);

      int count = 0;
      for (Map<String, Object> recordId : recordIds) {
        // check if the record already exists.
        psUpdate.setString(1, group);
        setParams(psUpdate, userId, projectId, rcName, pkColumns, recordId, 2);
        psUpdate.addBatch();
        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psUpdate.executeBatch();
          QueryLogger.logEndStatementExecution(sql,
              "wdk-favorite-update-group", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psUpdate.executeBatch();
        QueryLogger.logEndStatementExecution(sql, "wdk-favorite-update-group",
            -start);
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not set favorite group for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeStatement(psUpdate);
    }
  }

  @Deprecated // pending struts removal
  public String[] getGroups(User user) throws WdkModelException {
    String sql = "SELECT " + COLUMN_RECORD_GROUP + " FROM " + _userSchema
        + TABLE_FAVORITES + " WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_IS_DELETED  + " = " +
        _userDb.getPlatform().convertBoolean(false);
    DataSource dataSource = _wdkModel.getUserDb().getDataSource();
    PreparedStatement psSelect = null;
    ResultSet resultSet = null;
    try {
      psSelect = SqlUtils.getPreparedStatement(dataSource, sql);
      psSelect.setLong(1, user.getUserId());
      psSelect.setString(2, _wdkModel.getProjectId());

      long start = System.currentTimeMillis();
      resultSet = psSelect.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, "wdk-favorite-select-group",
          start, resultSet);
      Set<String> groups = new HashSet<String>();
      while (resultSet.next()) {
        String group = resultSet.getString(COLUMN_RECORD_GROUP);
        if (group == null || group.trim().length() == 0) continue;
        group = group.trim();
        groups.add(group);
      }
      String[] array = new String[groups.size()];
      groups.toArray(array);
      Arrays.sort(array);
      return array;
    } catch (SQLException e) {
      throw new WdkModelException("Could not set favorite groups for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet, psSelect);
    }
  }

  private void setParams(PreparedStatement ps, long userId, String projectId,
      String rcName, String[] pkColumns, Map<String, Object> recordId, int index)
      throws SQLException {
    ps.setLong(index++, userId);
    ps.setString(index++, projectId);
    ps.setString(index++, rcName);
    for (String column : pkColumns) {
      ps.setObject(index++, recordId.get(column));
    }
  }
}
