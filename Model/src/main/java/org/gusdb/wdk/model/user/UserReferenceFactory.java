package org.gusdb.wdk.model.user;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Optional;

import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

class UserReferenceFactory {

  // -------------------------------------------------------------------------
  // database table and column definitions
  // -------------------------------------------------------------------------

  public static final String TABLE_USERS = "users";
  public static final String COL_USER_ID = "user_id";
  public static final String COL_IS_GUEST = "is_guest";
  public static final String COL_FIRST_ACCESS = "first_access";

  // -------------------------------------------------------------------------
  // sql and sql macro definitions
  // -------------------------------------------------------------------------

  public static final String USER_SCHEMA_MACRO = "$$USER_SCHEMA$$";
  private static final String IS_GUEST_VALUE_MACRO = "$$IS_GUEST$$";

  // SQL and types to insert previously unknown user refs into the users table
  private static final String INSERT_USER_REF_SQL =
      "insert" +
      "  when not exists (select 1 from " + USER_SCHEMA_MACRO + TABLE_USERS + " where " + COL_USER_ID + " = ?)" +
      "  then" +
      "  into " + USER_SCHEMA_MACRO + TABLE_USERS + " (" + COL_USER_ID + "," + COL_IS_GUEST + "," + COL_FIRST_ACCESS +")" +
      "  select ?, " + IS_GUEST_VALUE_MACRO + ", ? from dual";

  private static final Integer[] INSERT_USER_REF_PARAM_TYPES = { Types.BIGINT, Types.BIGINT, Types.TIMESTAMP };

  // SQL and types to select user ref by ID
  private static final String SELECT_USER_REF_BY_ID_SQL =
      "select " + COL_USER_ID + ", " + COL_IS_GUEST + ", " + COL_FIRST_ACCESS +
      "  from " + USER_SCHEMA_MACRO + TABLE_USERS +
      "  where " + COL_USER_ID + " = ?";

  private static final Integer[] SELECT_USER_REF_BY_ID_PARAM_TYPES = { Types.BIGINT };

  private static class UserReference extends ThreeTuple<Long, Boolean, Date> {
    public UserReference(Long userId, Boolean isGuest, Date firstAccess) {
      super(userId, isGuest, firstAccess);
    }
    public Long getUserId() { return getFirst(); }
    public Boolean isGuest() { return getSecond(); }
    public Date getFirstAccess() { return getThird(); }
  }

  private final DatabaseInstance _userDb;
  private final String _userSchema;

  public UserReferenceFactory(WdkModel wdkModel) {
    _userDb = wdkModel.getUserDb();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  /**
   * Adds a user reference row to the UserDB users table if one does not exist.
   * This is for tracking and for foreign keys on other user DB tables.
   * Note is_guest and first_access are immutable fields and once set will not be
   * changed by this code.
   *
   * @param user user to add
   * @throws WdkModelException 
   */
  public int addUserReference(User user) throws WdkModelException {
    try {
      long userId = user.getUserId();
      boolean isGuest = user.isGuest();
      Timestamp insertedOn = new Timestamp(new Date().getTime());
      String sql = INSERT_USER_REF_SQL
          .replace(USER_SCHEMA_MACRO, _userSchema)
          .replace(IS_GUEST_VALUE_MACRO, _userDb.getPlatform().convertBoolean(isGuest).toString());
      return new SQLRunner(_userDb.getDataSource(), sql, "insert-user-ref")
        .executeUpdate(new Object[]{ userId, userId, insertedOn }, INSERT_USER_REF_PARAM_TYPES);
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }

  // FIXME: see if this is actually needed anywhere?  E.g. do we ever need to look up user refs by user ID to find last login?
  private Optional<UserReference> getUserReference(long userId) throws WdkModelException {
    try {
      String sql = SELECT_USER_REF_BY_ID_SQL.replace(USER_SCHEMA_MACRO, _userSchema);
      return new SQLRunner(_userDb.getDataSource(), sql, "get-user-ref").executeQuery(
          new Object[]{ userId },
          SELECT_USER_REF_BY_ID_PARAM_TYPES,
          rs ->
              !rs.next()
              ? Optional.empty()
              : Optional.of(new UserReference(
                  rs.getLong(COL_USER_ID),
                  rs.getBoolean(COL_IS_GUEST),
                  new Date(rs.getTimestamp(COL_FIRST_ACCESS).getTime()))));
    }
    catch (SQLRunnerException e) {
      throw WdkModelException.translateFrom(e);
    }
  }
}
