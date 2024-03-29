package org.gusdb.wdk.model.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class UserPreferenceFactory {

  private static final Logger LOG = Logger.getLogger(UserPreferenceFactory.class);

  private static final String GLOBAL_PREFERENCE_KEY = "[Global]";

  // even though we store value as a CLOB, the entire value is stored in memory
  //   at once (no streaming), so we must still be conscious of size
  public static final long MAX_PREFERENCE_VALUE_SIZE = 200000;
  public static final long MAX_PREFERENCE_KEY_SIZE = 4000;

  private final WdkModel _wdkModel;
  private final DatabaseInstance _userDb;
  private final String _userSchema;

  public UserPreferenceFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _userDb = wdkModel.getUserDb();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  public void savePreferences(long userId, UserPreferences newPrefs) throws WdkModelException {
    // get old preferences and determine what to delete, update, insert
    UserPreferences oldPreferences = getPreferences(userId);
    Map<String, String> oldGlobal = oldPreferences.getGlobalPreferences();
    Map<String, String> newGlobal = newPrefs.getGlobalPreferences();
    updatePreferences(userId, GLOBAL_PREFERENCE_KEY, oldGlobal, newGlobal);
    Map<String, String> oldSpecific = oldPreferences.getProjectPreferences();
    Map<String, String> newSpecific = newPrefs.getProjectPreferences();
    updatePreferences(userId, _wdkModel.getProjectId(), oldSpecific, newSpecific);
  }

  private void updatePreferences(long userId, String prefProjectId,
      Map<String, String> oldPreferences, Map<String, String> newPreferences)
      throws WdkModelException {
    // determine whether to delete, insert or update
    Set<String> toDelete = new LinkedHashSet<String>();
    Map<String, String> toUpdate = new LinkedHashMap<String, String>();
    Map<String, String> toInsert = new LinkedHashMap<String, String>();
    for (String key : oldPreferences.keySet()) {
      if (!newPreferences.containsKey(key)) {
        toDelete.add(key);
      } else { // key exist, check if need to update
        String newValue = newPreferences.get(key);
        String oldValue = oldPreferences.get(key);
        if (newValue == null || oldValue == null) 
          throw new WdkModelException("Null values not allowed for preferences. Key: " + key + " Old pref: " + oldValue + " New pref: " + newValue);
        if (!oldPreferences.get(key).equals(newValue))
          toUpdate.put(key, newValue);
      }
    }
    for (String key : newPreferences.keySet()) {
      if (newPreferences.get(key) == null) 
        throw new WdkModelException("Null values not allowed for new preference values. Key: " + key);
      if (!oldPreferences.containsKey(key))
        toInsert.put(key, newPreferences.get(key));
    }
    LOG.debug("to insert: " + FormatUtil.prettyPrint(toInsert, Style.MULTI_LINE));
    LOG.debug("to update: " + FormatUtil.prettyPrint(toUpdate, Style.MULTI_LINE));
    LOG.debug("to delete: " + FormatUtil.arrayToString(toDelete.toArray()));

    PreparedStatement psDelete = null, psInsert = null, psUpdate = null;
    try {

      // delete preferences
      if (!toDelete.isEmpty()) {
        String sqlDelete = "DELETE FROM " + _userSchema + "preferences "
            + " WHERE user_id = ? AND project_id = ? "
            + " AND preference_name = ?";
        psDelete = SqlUtils.getPreparedStatement(_userDb.getDataSource(), sqlDelete);
        long start = System.currentTimeMillis();
        for (String key : toDelete) {
          psDelete.setLong(1, userId);
          psDelete.setString(2, prefProjectId);
          psDelete.setString(3, key);
          psDelete.addBatch();
        }
        psDelete.executeBatch();
        QueryLogger.logEndStatementExecution(sqlDelete, "wdk-user-delete-preference", start);
      }

      // insert preferences
      if (!toInsert.isEmpty()) {
        String sqlInsert = "INSERT INTO " + _userSchema + "preferences "
            + " (user_id, project_id, preference_name, " + " preference_value)"
            + " VALUES (?, ?, ?, ?)";
        psInsert = SqlUtils.getPreparedStatement(_userDb.getDataSource(), sqlInsert);
        long start = System.currentTimeMillis();
        for (String key : toInsert.keySet()) {
          psInsert.setLong(1, userId);
          psInsert.setString(2, prefProjectId);
          psInsert.setString(3, key);
          psInsert.setString(4, toInsert.get(key));
          psInsert.addBatch();
        }
        psInsert.executeBatch();
        QueryLogger.logEndStatementExecution(sqlInsert, "wdk-user-insert-preference", start);
      }

      // update preferences
      if (!toUpdate.isEmpty()) {
        String sqlUpdate = "UPDATE " + _userSchema + "preferences "
            + " SET preference_value = ? WHERE user_id = ? "
            + " AND project_id = ? AND preference_name = ?";
        psUpdate = SqlUtils.getPreparedStatement(_userDb.getDataSource(), sqlUpdate);
        long start = System.currentTimeMillis();
        for (String key : toUpdate.keySet()) {
          psUpdate.setString(1, toUpdate.get(key));
          psUpdate.setLong(2, userId);
          psUpdate.setString(3, prefProjectId);
          psUpdate.setString(4, key);
          psUpdate.addBatch();
        }
        psUpdate.executeBatch();
        QueryLogger.logEndStatementExecution(sqlUpdate, "wdk-user-update-preference", start);
      }
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to update user (id=" + userId
          + ") preferences", e);
    }
    finally {
      SqlUtils.closeStatement(psDelete);
      SqlUtils.closeStatement(psInsert);
      SqlUtils.closeStatement(psUpdate);
    }
  }

  /**
   * @param user
   * @return a list of 2 elements, the first is a map of global preferences, the
   *         second is a map of project-specific preferences.
   */
  public UserPreferences getPreferences(long userId) throws WdkModelException {
    try {
      String sql = "SELECT * FROM " + _userSchema + "preferences WHERE user_id = ?";
      return new SQLRunner(_userDb.getDataSource(), sql, "wdk-user-select-preference")
        .executeQuery(
          new Object[]{ userId },
          new Integer[]{ Types.BIGINT },
          rs -> {
            UserPreferences prefs = new UserPreferences();
            while (rs.next()) {
              String prefProjectId = rs.getString("project_id");
              String prefName = rs.getString("preference_name");
              String prefValue = rs.getString("preference_value");
              if (prefProjectId.equals(GLOBAL_PREFERENCE_KEY)) {
                prefs.setGlobalPreference(prefName, prefValue);
              }
              else if (prefProjectId.equals(_wdkModel.getProjectId())) {
                prefs.setProjectPreference(prefName, prefValue);
              }
            }
            return prefs;
          });
    }
    catch (Exception e) {
      return WdkModelException.unwrap(e);
    }
  }
}
