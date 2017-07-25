package org.gusdb.wdk.model.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.runner.BasicArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Procedure;
import org.gusdb.wdk.model.Utilities;
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

  private static final String TABLE_FAVORITES = "favorites";
  private static final String COLUMN_FAVORITE_ID = "favorite_id";
  private static final String COLUMN_USER_ID = "user_id";
  private static final String COLUMN_PROJECT_ID = "project_id";
  private static final String COLUMN_RECORD_CLASS = "record_class";
  private static final String COLUMN_RECORD_NOTE = "record_note";
  private static final String COLUMN_RECORD_GROUP = "record_group";
  private static final String COLUMN_IS_DELETED = "is_deleted";

  private static final Logger logger = Logger.getLogger(FavoriteFactory.class);
  
  private static final int BATCH_SIZE = 100;
  private static final String PK_COLUMNS_MACRO = "$$PK_COLUMNS_MACROS$$";
  private static final String PK_VALUES_MACRO = "$$PK_VALUES_MACRO$$";
  private static final String PK_PREDICATE_MACRO = "$$PK_PREDICATE_MACRO$$";
  
  //private static final String DELETE_FAVORITE_BY_ID_SQL =
  //  "DELETE FROM " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
  //  " WHERE " + COLUMN_FAVORITE_ID + "= ? AND " + COLUMN_USER_ID + " = ?";
  
  private static final String SELECT_ALL_FAVORITES_SQL =
    "SELECT * FROM " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " WHERE " + COLUMN_USER_ID + " = ? " +
    "  AND " + COLUMN_IS_DELETED + " = 0";
    
  private static final String DELETE_ALL_FAVORITES_SQL =
	"UPDATE " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " SET " + COLUMN_IS_DELETED + " = 1 " +
	" WHERE " + COLUMN_USER_ID + " = ? " +
    "  AND " + COLUMN_IS_DELETED + " = 0";
  
  private static final String DELETE_FAVORITES_BY_ID_SQL =
    "UPDATE " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " SET " + COLUMN_IS_DELETED + " = 1 " +
    " WHERE " + COLUMN_FAVORITE_ID + "= ? " +
    "  AND " + COLUMN_USER_ID + " = ? " +
    "  AND " + COLUMN_IS_DELETED + " = 0";
  
  private static final String UNDELETE_FAVORITES_BY_ID_SQL =
    "UPDATE " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " SET " + COLUMN_IS_DELETED + " = 0 " +
    " WHERE " + COLUMN_FAVORITE_ID + "= ? " +
    "  AND " + COLUMN_USER_ID + " = ? " +
    "  AND " + COLUMN_IS_DELETED + " = 1";
  
  private static final String SELECT_FAVORITE_BY_ID_SQL =
    "SELECT * FROM " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES +
    " WHERE " + COLUMN_FAVORITE_ID + "= ? " +
    "  AND " + COLUMN_USER_ID + " = ?" +
    "  AND " + COLUMN_IS_DELETED + " = 0";
  
  private static final String UPDATE_FAVORITE_BY_ID_SQL =
    "UPDATE "  + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " SET " + COLUMN_RECORD_NOTE + " = ? " + COLUMN_RECORD_GROUP + " = ? " +
    " WHERE " + COLUMN_FAVORITE_ID + "= ? " +
    "  AND " + COLUMN_USER_ID + " = ? " +
    "  AND " + COLUMN_IS_DELETED + " = 0";
  
  private static final String INSERT_FAVORITE_SQL =
    "INSERT INTO " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES + 
    " (" + COLUMN_FAVORITE_ID + 
    ", " + COLUMN_USER_ID + 
    ", " + COLUMN_PROJECT_ID  + 
    ", " + COLUMN_RECORD_CLASS +
           PK_COLUMNS_MACRO +
    ", " + COLUMN_RECORD_NOTE + 
    " ," + COLUMN_IS_DELETED +
    ") VALUES (?, ?, ?, ?" + PK_VALUES_MACRO + ", ?, 0)";
  
  private static final String CHECK_EXISTENCE_FAVORITE_SQL =
    "SELECT * FROM " + UserFactory.USER_SCHEMA_MACRO + TABLE_FAVORITES +
    " WHERE " + COLUMN_USER_ID + "= ? " +
    " AND " +   COLUMN_PROJECT_ID + " = ? " +
    " AND " +   COLUMN_RECORD_CLASS + " = ? " +
                PK_PREDICATE_MACRO;
		  
  private WdkModel wdkModel;
  private String schema;

  public FavoriteFactory(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    this.schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }
  
  public List<Favorite> getAllFavorites(User user) throws WdkModelException {
    long userId = user.getUserId();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    final List<Favorite> favorites = new ArrayList<>();
	try {  
	  final String selectAllFavoritesSql = SELECT_ALL_FAVORITES_SQL.replace(UserFactory.USER_SCHEMA_MACRO, schema);
	  new SQLRunner(dataSource, selectAllFavoritesSql, "select-all-favorite").executeQuery(
	   new Object[]{ userId }, new Integer[]{ Types.BIGINT }, resultSet -> {
		String recordClassName = null;
	    while (resultSet.next()) {	
	      recordClassName = resultSet.getString(COLUMN_RECORD_CLASS);
	      // Need to avoid showing favorite for defunct (per current wdk model) record class sets or record classes
	      try {
	        if(wdkModel.isExistsRecordClassSet(recordClassName)) {
	          RecordClass recordClass = wdkModel.getRecordClass(recordClassName);  
	          String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
              Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
              for (int i = 1; i <= pkColumns.length; i++) {
	            Object value = resultSet.getObject(Utilities.COLUMN_PK_PREFIX + i);
	            primaryKeys.put(pkColumns[i - 1], value);
	          }
	          PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), primaryKeys);
	          Long favoriteId = resultSet.getLong(COLUMN_FAVORITE_ID);
	          Favorite favorite = new Favorite(user, recordClass, pkValue, favoriteId);
	          favorite.setNote(resultSet.getString(COLUMN_RECORD_NOTE));
	          favorite.setGroup(resultSet.getString(COLUMN_RECORD_GROUP));
	          favorites.add(favorite);
	        }
	      }
	      catch(WdkModelException wme) {
	        throw new RuntimeException(wme);
	      }
	    }
	  });
	  return favorites;
    }
    catch(SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }
  
  /**
   * Deletes all of a user's favorites
   * @param user
   * @return
   * @throws WdkModelException
   */
  public Integer deleteAllFavorites(User user) throws WdkModelException {
    long userId = user.getUserId();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    try {  
      final String deleteAllFavoritesSql = DELETE_ALL_FAVORITES_SQL.replace(UserFactory.USER_SCHEMA_MACRO, schema);
      Integer count = new SQLRunner(dataSource, deleteAllFavoritesSql, "delete-all-favorites")
      .executeUpdate(new Object[]{ userId }, new Integer[]{ Types.BIGINT });
      return count;
    }
    catch(SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }
  
  /**
   * Transaction safe batch operation to remove multiple favorites (if owned by the given user).  Only
   * those favorites owned by the user will be deleted.
   * @param user
   * @param favoriteIds
   * @throws WdkModelException
   */
  public void removeFromFavorite(User user, List<Long> favoriteIds) throws WdkModelException {
	Long userId = user.getUserId();
	try {  
      final Connection conn = wdkModel.getUserDb().getDataSource().getConnection();
      try {
        final String deleteFavoriteSql = DELETE_FAVORITES_BY_ID_SQL
        		.replace(UserFactory.USER_SCHEMA_MACRO, schema);
        SqlUtils.performInTransaction(conn, new Procedure() {
          @Override public void perform() {
            BasicArgumentBatch batch = new BasicArgumentBatch();
            batch.setBatchSize(BATCH_SIZE);
            batch.setParameterTypes(new Integer[]{ Types.BIGINT, Types.BIGINT });
            for(Long favoriteId : favoriteIds) {
              batch.add(new Object[] { favoriteId, userId });
            }   
            new SQLRunner(conn, deleteFavoriteSql, "delete-favorites").executeStatementBatch(batch); 
          }
        });
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
	catch(SQLException se) {
	  throw new WdkModelException(se);
	}
  }

  /**
   * Gets the favorite identified by its favorite id if owned by the given user.
   * @param user
   * @param favoriteId
   * @return
   * @throws WdkModelException
   */
  public Favorite getFavorite(User user, Long favoriteId) throws WdkModelException {
    long userId = user.getUserId();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    final Wrapper<Favorite> favoriteWrapper = new Wrapper<>();
    try {  
      final String selectFavoriteSql = SELECT_FAVORITE_BY_ID_SQL.replace(UserFactory.USER_SCHEMA_MACRO, schema);
      new SQLRunner(dataSource, selectFavoriteSql, "select-favorite-by-id").executeQuery(
        new Object[]{ favoriteId, userId }, new Integer[]{ Types.BIGINT, Types.BIGINT }, resultSet -> {
          if (resultSet.next()) {
        	String recordClassName = resultSet.getString(COLUMN_RECORD_CLASS);
        	// Need to avoid showing favorite for defunct (per current wdk model) record class sets or record classes
        	try {
        	  if(wdkModel.isExistsRecordClassSet(recordClassName)) {
                RecordClass recordClass = wdkModel.getRecordClass(recordClassName);
                String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
                Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= pkColumns.length; i++) {
                  Object value = resultSet.getObject(Utilities.COLUMN_PK_PREFIX + i);
                  primaryKeys.put(pkColumns[i - 1], value);
                }
                PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), primaryKeys);
                Favorite favorite = new Favorite(user, recordClass, pkValue, favoriteId);
                favorite.setNote(resultSet.getString(COLUMN_RECORD_NOTE));
                favorite.setGroup(resultSet.getString(COLUMN_RECORD_GROUP));
                favoriteWrapper.set(favorite);
              }
        	}
        	catch(WdkModelException wme) {
        	  throw new RuntimeException(wme);
        	}
          }
        }      
      );
      return favoriteWrapper.get();
    }
    catch(SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }

  public int undeleteFavorites(User user, List<Long> favoriteIds) throws WdkModelException {
    Long userId = user.getUserId();
    final Wrapper<Integer> updateCountWrapper = new Wrapper<>();
	try {  
	  final Connection conn = wdkModel.getUserDb().getDataSource().getConnection();
	  try {
	    final String undeleteFavoriteSql = UNDELETE_FAVORITES_BY_ID_SQL
	      .replace(UserFactory.USER_SCHEMA_MACRO, schema);
	    SqlUtils.performInTransaction(conn, new Procedure() {
	      @Override public void perform() {
          BasicArgumentBatch batch = new BasicArgumentBatch();
            batch.setBatchSize(BATCH_SIZE);
            batch.setParameterTypes(new Integer[]{ Types.BIGINT, Types.BIGINT });
            for(Long favoriteId : favoriteIds) {
              batch.add(new Object[] { favoriteId, userId });
            }   
            Integer count = new SQLRunner(conn, undeleteFavoriteSql, "undelete-favorites").executeUpdateBatch(batch); 
            updateCountWrapper.set(count);
          }
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
    catch(SQLException se) {
      throw new WdkModelException(se);
    }
  }
  
  /**
   * 
   * @param user
   * @param favoriteId
   * @return
   * @throws WdkModelException
   */
  public int editFavorite(User user, Long favoriteId, String note, String group) throws WdkModelException {
    Long userId = user.getUserId();
    Integer count = 0;
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    final String updateFavoriteSql = UPDATE_FAVORITE_BY_ID_SQL
	  .replace(UserFactory.USER_SCHEMA_MACRO, schema);
	try {
	  count = new SQLRunner(dataSource, updateFavoriteSql, "edit-favorite") 
	    .executeUpdate(new Object[]{ note, group, favoriteId, userId }, new Integer[]{ Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.BIGINT });
	  return count;
	}
    catch (SQLRunnerException sre) {
      throw new WdkModelException(sre.getCause().getMessage(), sre.getCause()); 
    }
    catch(Exception ex) {
      throw new WdkModelException(ex);
    }
  }
  
  /**
   * Checks to see whether favorite already exists for this user based on record class and primary key
   * values.  If it doesn't, a -1 is returned, if it does but the favorite is labeled as deleted, the
   * favorite id is returned.  If the favorite exists and is not deleted, a null is returned.
   * @param conn
   * @param user
   * @param recordClass
   * @param pkValues
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Long isInFavorites(Connection conn, User user, RecordClass recordClass, Map<String, Object> pkValues) throws WdkModelException, WdkUserException {
	final Wrapper<Long> existenceWrapper = new Wrapper<>();
	existenceWrapper.set(-1L);
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    int numPkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs().length;
    List<Integer> paramTypes = new ArrayList<>(Arrays.asList(Types.BIGINT, Types.VARCHAR, Types.VARCHAR));
    List<Object> paramValues = new ArrayList<>(Arrays.asList(userId, projectId, recordClass.getFullName()));
    StringBuilder primaryKeyPredicate = new StringBuilder();
    int i = 0;
    for(String columnRef : recordClass.getPrimaryKeyDefinition().getColumnRefs()) {
      i++;
      paramTypes.add(Types.VARCHAR);
      paramValues.add(pkValues.get(columnRef));
      primaryKeyPredicate.append(" AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?");
    }
	final String checkExistenceFavoriteSql = CHECK_EXISTENCE_FAVORITE_SQL
	  .replace(UserFactory.USER_SCHEMA_MACRO, schema)
	  .replace(PK_PREDICATE_MACRO, primaryKeyPredicate.toString());
	try {
	  new SQLRunner(dataSource, checkExistenceFavoriteSql, "check_existence-favorite-by-props")
	    .executeQuery(paramValues.toArray(), paramTypes.toArray(new Integer[(3 + numPkColumns)]), resultSet -> {
	      if(resultSet.next()) {
	    	boolean isDeleted = resultSet.getBoolean(COLUMN_IS_DELETED);
	        existenceWrapper.set(isDeleted ? resultSet.getLong(COLUMN_FAVORITE_ID) : null);
	      }
	    });
	  return existenceWrapper.get();
	}   
	catch(SQLRunnerException sre) {
	  throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
    }
    catch(Exception e) {
      throw new WdkModelException(e);
    }
  }
  
  public void addToFavorites(User user, RecordClass recordClass, Map<String, Object> pkValues) throws WdkModelException, WdkUserException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    int numPkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs().length;
    try {
      long favoriteId = platform.getNextId(dataSource, schema, TABLE_FAVORITES);
      List<Integer> paramTypes = new ArrayList<>(Arrays.asList(Types.BIGINT, Types.BIGINT, Types.VARCHAR, Types.VARCHAR));
      List<Object> paramValues = new ArrayList<>(Arrays.asList(favoriteId, userId, projectId, recordClass.getFullName()));
      StringBuilder primaryKeyColumns = new StringBuilder();
      StringBuilder primaryKeyValues = new StringBuilder();
      int i = 0;
      for(String columnRef : recordClass.getPrimaryKeyDefinition().getColumnRefs()) {
        i++;
        paramTypes.add(Types.VARCHAR);
        paramValues.add(pkValues.get(columnRef));
        primaryKeyColumns.append(", " + Utilities.COLUMN_PK_PREFIX + i);
        primaryKeyValues.append(", ?");
      } 
  	  final Connection conn = wdkModel.getUserDb().getDataSource().getConnection(); 
  	  try {
	    SqlUtils.performInTransaction(conn, new Procedure() {
	      @Override public void perform() {
	    	try {
	    	  Long existence = isInFavorites(conn, user, recordClass, pkValues);
	    	  // If a null is returned, the favorite already exists physically and is not deleted
              if(existence == null) {
                return;
              }
              // If a -1 is returned, the favorite does not physically exist
              if(existence == -1) {
                String note = createInitialNote(user, recordClass, pkValues);
                paramTypes.add(Types.VARCHAR);
                paramValues.add(note);
                final String insertFavoriteSql = INSERT_FAVORITE_SQL
          	      .replace(UserFactory.USER_SCHEMA_MACRO, schema)
                  .replace(PK_COLUMNS_MACRO, primaryKeyColumns)
      	          .replace(PK_VALUES_MACRO, primaryKeyValues);
                new SQLRunner(dataSource, insertFavoriteSql, "insert-favorite-by-props")
                  .executeUpdate(paramValues.toArray(), paramTypes.toArray(new Integer[(4 + numPkColumns)]));
                return;
              }
              // Otherwise, the favorite exist but is deleted.  The existence value corresponds to that
              // favorite id.
              else {
            	ArrayList<Long> favoriteIds = new ArrayList<>();
            	favoriteIds.add(favoriteId);
            	undeleteFavorites(user, favoriteIds);
              }
	    	}  
            catch(WdkUserException | WdkModelException ex) {
              throw new RuntimeException(ex);	
            }
	      }
	    });
	  }  
      catch(SQLRunnerException sre) {
        throw new WdkModelException(sre.getCause().getMessage(), sre.getCause());
      }
      catch(Exception e) {
        throw new WdkModelException(e);
      }
      finally {
        SqlUtils.closeQuietly(conn);
      }
    }
    catch(SQLException se) {
      throw new WdkModelException(se);
    }  
  }
  
  protected String createInitialNote(User user, RecordClass recordClass, Map<String,Object> pkValues) throws WdkModelException, WdkUserException {
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
  public void addToFavorite(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds) throws WdkModelException, WdkUserException {
    logger.debug("adding favorite...");
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlInsert = "INSERT INTO " + schema + TABLE_FAVORITES + " ("
        + COLUMN_FAVORITE_ID + ", " + COLUMN_USER_ID + ", " + COLUMN_PROJECT_ID
        + ", " + COLUMN_RECORD_CLASS;
    String sqlValues = "";
    String sqlCount = "SELECT count(*) FROM " + schema + TABLE_FAVORITES
        + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
        + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlInsert += ", " + Utilities.COLUMN_PK_PREFIX + i;
      sqlValues += ", ?";
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    sqlInsert += ", " + COLUMN_RECORD_NOTE + ") VALUES (?, ?, ?, ?" + sqlValues
        + ", ?)";
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    PreparedStatement psInsert = null, psCount = null;
    try {
      psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      int count = 0;
      for (Map<String, Object> pkValues : recordIds) {
        // check if the record already exists.
        setParams(psCount, userId, projectId, rcName, pkColumns, pkValues, 1);
        boolean hasRecord = false;
        ResultSet resultSet = null;
        try {
          long start = System.currentTimeMillis();
          resultSet = psCount.executeQuery();
          QueryLogger.logEndStatementExecution(sqlCount, "wdk-favorite-count",
              start);
          if (resultSet.next()) {
            int rsCount = resultSet.getInt(1);
            hasRecord = (rsCount > 0);
          }
        } finally {
          if (resultSet != null) SqlUtils.closeResultSetOnly(resultSet);
        }
        if (hasRecord) continue;

        // get the default favorite note
        AttributeField noteField = recordClass.getFavoriteNoteField();
        String note = null;
        if (noteField != null) {
          RecordInstance instance = new DynamicRecordInstance(user, recordClass, pkValues);
          AttributeValue noteValue = instance.getAttributeValue(noteField.getName());
          Object value = noteValue.getValue();
          note = (value != null) ? value.toString() : "";
        }

        // insert new record
        long favoriteId = platform.getNextId(dataSource, schema, TABLE_FAVORITES);
        psInsert.setLong(1, favoriteId);
        setParams(psInsert, userId, projectId, rcName, pkColumns, pkValues, 2);
        psInsert.setString(5 + pkColumns.length, note);
        psInsert.addBatch();

        count++;
        if (count % 100 == 0) {
          long start = System.currentTimeMillis();
          psInsert.executeBatch();
          QueryLogger.logEndStatementExecution(sqlInsert,
              "wdk-favorite-insert", start);
        }
      }
      if (count % 100 != 0) {
        long start = System.currentTimeMillis();
        psInsert.executeBatch();
        QueryLogger.logEndStatementExecution(sqlInsert, "wdk-favorite-insert",
            start);
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not add item to favorites", e);
    } finally {
      SqlUtils.closeStatement(psInsert);
      SqlUtils.closeStatement(psCount);
    }
  }

  public void removeFromFavorite(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlDelete = "DELETE FROM " + schema + TABLE_FAVORITES + " WHERE "
        + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
        + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlDelete += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
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
  
  

  public void clearFavorite(User user) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String sqlDelete = "DELETE FROM " + schema + TABLE_FAVORITES + " WHERE "
        + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID + " = ?";

    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psDelete = null;
    try {
      long start = System.currentTimeMillis();
      psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
      psDelete.setLong(1, userId);
      psDelete.setString(2, projectId);
      psDelete.executeUpdate();
      QueryLogger.logEndStatementExecution(sqlDelete,
          "wdk-favorite-delete-all", start);
    } catch (SQLException e) {
      throw new WdkModelException("Failed to clear favorite for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeStatement(psDelete);
    }
  }

  public int getFavoriteCounts(User user) throws WdkModelException {
    // load the unique counts
    String sql = "SELECT count(*) AS fav_size FROM " + schema + TABLE_FAVORITES
        + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_PROJECT_ID + " = ?";
    DataSource ds = wdkModel.getUserDb().getDataSource();
    PreparedStatement ps = null;
    ResultSet rs = null;
    int count = 0;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setLong(1, user.getUserId());
      ps.setString(2, wdkModel.getProjectId());
      rs = ps.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-favorite-count", start);
      if (rs.next()) {
        count = rs.getInt("fav_size");
      }
    } catch (SQLException e) {
      throw new WdkModelException("Could not get favorite counts for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rs, ps);
    }
    return count;
  }

  public int getFavoriteCount(User user, List<Map<String, Object>> records, RecordClass recordClass)
      throws WdkModelException {
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

  public Map<RecordClass, List<Favorite>> getFavorites(User user) throws WdkModelException {
    String sql = "SELECT * FROM " + schema + TABLE_FAVORITES + " WHERE "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_USER_ID + " =?"
        + " ORDER BY " + COLUMN_RECORD_CLASS + " ASC, lower("
        + COLUMN_RECORD_GROUP + ") ASC, " + Utilities.COLUMN_PK_PREFIX
        + "1 ASC";
    DataSource ds = wdkModel.getUserDb().getDataSource();
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      long start = System.currentTimeMillis();
      ps = SqlUtils.getPreparedStatement(ds, sql);
      ps.setFetchSize(100);
      ps.setString(1, wdkModel.getProjectId());
      ps.setLong(2, user.getUserId());
      rs = ps.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-favorite-select-all",
          start);

      Map<RecordClass, List<Favorite>> favorites = new LinkedHashMap<RecordClass, List<Favorite>>();
      while (rs.next()) {
    	long favoriteId = rs.getLong(COLUMN_FAVORITE_ID);  
        String rcName = rs.getString(COLUMN_RECORD_CLASS);
        // Start CWL 29JUN2016
        // Added conditionals to avoid showing favorites for defunct record class sets or record classes
        if(wdkModel.isExistsRecordClassSet(rcName)) {
          RecordClass recordClass = wdkModel.getRecordClass(rcName);
          if(recordClass != null) {
            List<Favorite> list;
            if (favorites.containsKey(recordClass)) {
             list = favorites.get(recordClass);
            } else {
              list = new ArrayList<Favorite>();
              favorites.put(recordClass, list);
            }
         
            String[] columns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
            Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
            for (int i = 1; i <= columns.length; i++) {
              Object value = rs.getObject(Utilities.COLUMN_PK_PREFIX + i);
              primaryKeys.put(columns[i - 1], value);
            }
            PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), primaryKeys);
            Favorite favorite = new Favorite(user, recordClass, pkValue, favoriteId);
            favorite.setNote(rs.getString(COLUMN_RECORD_NOTE));
            favorite.setGroup(rs.getString(COLUMN_RECORD_GROUP));
            list.add(favorite);
          }  
        }
        // End CWL 29JUN2016
      }
      return favorites;
    } catch (SQLException e) {
      throw new WdkModelException("Cannot get favorites for user "
          + user.getUserId(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(rs, ps);
    }
  }
  

  
  public Favorite getFavorite(User user, RecordClass recordClass, Map<String, Object> recordId) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlQuery = "SELECT * FROM " + schema + TABLE_FAVORITES
        + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
        + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlQuery += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psQuery = null;
    ResultSet resultSet = null;
    try {
      psQuery = SqlUtils.getPreparedStatement(dataSource, sqlQuery);
      setParams(psQuery, userId, projectId, rcName, pkColumns, recordId, 1);
      long start = System.currentTimeMillis();
      resultSet = psQuery.executeQuery();
      QueryLogger.logEndStatementExecution(sqlQuery, "wdk-favorite-instance-query", start);
      Favorite favorite = null;
      if (resultSet.next()) {
    	long favoriteId = resultSet.getLong(COLUMN_FAVORITE_ID);  
        PrimaryKeyValue pkValue = new PrimaryKeyValue(recordClass.getPrimaryKeyDefinition(), recordId);
        favorite = new Favorite(user, recordClass, pkValue, favoriteId);
        favorite.setNote(resultSet.getString(COLUMN_RECORD_NOTE));
        favorite.setGroup(resultSet.getString(COLUMN_RECORD_GROUP));
      }
      return favorite;
    }
    catch (SQLException e) {
      throw new WdkModelException(
          "Could not obtain user's favorite by record class and record id(s) "
              + user.getUserId(), e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, psQuery);
    }
  }

  public boolean isInFavorite(User user, RecordClass recordClass,
      Map<String, Object> recordId) throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sqlCount = "SELECT count(*) FROM " + schema + TABLE_FAVORITES
        + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
        + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psCount = null;
    ResultSet resultSet = null;
    try {
      psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
      // check if the record already exists.
      setParams(psCount, userId, projectId, rcName, pkColumns, recordId, 1);
      boolean hasRecord = false;
      long start = System.currentTimeMillis();
      resultSet = psCount.executeQuery();
      QueryLogger.logEndStatementExecution(sqlCount, "wdk-favorite-count",
          start);
      if (resultSet.next()) {
        int rsCount = resultSet.getInt(1);
        hasRecord = (rsCount > 0);
      }
      return hasRecord;
    } catch (SQLException e) {
      throw new WdkModelException(
          "Could not check whether record id(s) are favorites for user "
              + user.getUserId(), e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet, psCount);
    }
  }

  public void setNotes(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds, String note)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sql = "UPDATE " + schema + TABLE_FAVORITES + " SET "
        + COLUMN_RECORD_NOTE + " = ? WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sql += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
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

  public void setGroups(User user, RecordClass recordClass,
      List<Map<String, Object>> recordIds, String group)
      throws WdkModelException {
    long userId = user.getUserId();
    String projectId = wdkModel.getProjectId();
    String rcName = recordClass.getFullName();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String sql = "UPDATE " + schema + TABLE_FAVORITES + " SET "
        + COLUMN_RECORD_GROUP + " = ? WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
    for (int i = 1; i <= pkColumns.length; i++) {
      sql += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
    }
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
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

  public String[] getGroups(User user) throws WdkModelException {
    String sql = "SELECT " + COLUMN_RECORD_GROUP + " FROM " + schema
        + TABLE_FAVORITES + " WHERE " + COLUMN_USER_ID + "= ? AND "
        + COLUMN_PROJECT_ID + " = ?";
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    PreparedStatement psSelect = null;
    ResultSet resultSet = null;
    try {
      psSelect = SqlUtils.getPreparedStatement(dataSource, sql);
      psSelect.setLong(1, user.getUserId());
      psSelect.setString(2, wdkModel.getProjectId());

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
