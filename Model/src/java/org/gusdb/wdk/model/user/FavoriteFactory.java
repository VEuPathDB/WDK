package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.json.JSONException;

public class FavoriteFactory {

    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_RECORD_CLASS = "record_class";
    private static final String COLUMN_RECORD_NOTE = "record_note";
    private static final String COLUMN_RECORD_GROUP = "record_group";

    private static final Logger logger = Logger.getLogger(FavoriteFactory.class);

    private WdkModel wdkModel;
    private String schema;

    public FavoriteFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.schema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    }

    /**
     * @param user
     * @param recordClass
     * @param recordIds
     *            a list of primary key values. the inner map is a primary-key
     *            column-value map.
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws JSONException
     * @throws NoSuchAlgorithmException
     */
    public void addToFavorite(User user, RecordClass recordClass,
            List<Map<String, Object>> recordIds) throws WdkModelException {
        logger.debug("adding favorite...");
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sqlInsert = "INSERT INTO " + schema + TABLE_FAVORITES + " ("
                + COLUMN_USER_ID + ", " + COLUMN_PROJECT_ID + ", "
                + COLUMN_RECORD_CLASS;
        String sqlValues = "";
        String sqlCount = "SELECT count(*) FROM " + schema + TABLE_FAVORITES
                + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
                + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sqlInsert += ", " + Utilities.COLUMN_PK_PREFIX + i;
            sqlValues += ", ?";
            sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }
        sqlInsert += ", " + COLUMN_RECORD_NOTE + ") VALUES (?, ?, ?"
                + sqlValues + ", ?)";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psInsert = null, psCount = null;
        try {
            psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
            psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
            int count = 0;
            for (Map<String, Object> recordId : recordIds) {
                // check if the record already exists.
                setParams(psCount, userId, projectId, rcName, pkColumns,
                        recordId, 1);
                boolean hasRecord = false;
                ResultSet resultSet = null;
                try {
                    long start = System.currentTimeMillis();
                    resultSet = psCount.executeQuery();
                    SqlUtils.verifyTime(wdkModel, sqlCount,
                            "wdk-favorite-count", start);
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
                    RecordInstance instance = new RecordInstance(user,
                            recordClass, recordId);
                    AttributeValue noteValue = instance.getAttributeValue(noteField.getName());
                    Object value = noteValue.getValue();
                    note = (value != null) ? value.toString() : "";
                }

                // insert new record
                setParams(psInsert, userId, projectId, rcName, pkColumns,
                        recordId, 1);
                psInsert.setString(4 + pkColumns.length, note);
                psInsert.addBatch();

                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psInsert.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sqlInsert,
                            "wdk-favorite-insert", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psInsert.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlInsert, "wdk-favorite-insert",
                        start);
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException ("Could not add item to favorites", e);
        }
        finally {
            SqlUtils.closeStatement(psInsert);
            SqlUtils.closeStatement(psCount);
        }
    }

    public void removeFromFavorite(User user, RecordClass recordClass,
            List<Map<String, Object>> recordIds) throws WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sqlDelete = "DELETE FROM " + schema + TABLE_FAVORITES
                + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
                + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sqlDelete += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psDelete = null;
        try {
            psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
            int count = 0;
            for (Map<String, Object> recordId : recordIds) {
                setParams(psDelete, userId, projectId, rcName, pkColumns,
                        recordId, 1);
                psDelete.addBatch();
                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psDelete.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sqlDelete,
                            "wdk-favorite-delete", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psDelete.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlDelete, "wdk-favorite-delete",
                        -start);
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not remove favorite(s) for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public void clearFavorite(User user) throws WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String sqlDelete = "DELETE FROM " + schema + TABLE_FAVORITES
                + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
                + " = ?";

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psDelete = null;
        try {
            long start = System.currentTimeMillis();
            psDelete = SqlUtils.getPreparedStatement(dataSource, sqlDelete);
            psDelete.setInt(1, userId);
            psDelete.setString(2, projectId);
            psDelete.executeUpdate();
            SqlUtils.verifyTime(wdkModel, sqlDelete, "wdk-favorite-delete-all",
                    start);
        }
        catch (SQLException e) {
        	throw new WdkModelException("Failed to clear favorite for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public int getFavoriteCounts(User user) throws WdkModelException {
        // load the unique counts
        String sql = "SELECT count(*) AS fav_size FROM " + schema
                + TABLE_FAVORITES + " WHERE " + COLUMN_USER_ID + " = ? AND "
                + COLUMN_PROJECT_ID + " = ?";
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        int count = 0;
        try {
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setInt(1, user.getUserId());
            ps.setString(2, wdkModel.getProjectId());
            rs = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-count", start);
            if (rs.next()) {
                count = rs.getInt("fav_size");
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not get favorite counts for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(rs);
        }
        return count;
    }

    public Map<RecordClass, List<Favorite>> getFavorites(User user)
            throws WdkModelException {
        String sql = "SELECT * FROM " + schema + TABLE_FAVORITES + " WHERE "
                + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_USER_ID + " =?"
                + " ORDER BY " + COLUMN_RECORD_CLASS + " ASC, lower("
                + COLUMN_RECORD_GROUP + ") ASC, " + Utilities.COLUMN_PK_PREFIX
                + "1 ASC";
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setFetchSize(100);
            ps.setString(1, wdkModel.getProjectId());
            ps.setInt(2, user.getUserId());
            rs = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-select-all", start);

            Map<RecordClass, List<Favorite>> favorites = new LinkedHashMap<RecordClass, List<Favorite>>();
            while (rs.next()) {
                String rcName = rs.getString(COLUMN_RECORD_CLASS);
                RecordClass recordClass = (RecordClass) wdkModel.getRecordClass(rcName);
                List<Favorite> list;
                if (favorites.containsKey(recordClass)) {
                    list = favorites.get(recordClass);
                } else {
                    list = new ArrayList<Favorite>();
                    favorites.put(recordClass, list);
                }

                String[] columns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
                Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= columns.length; i++) {
                    Object value = rs.getObject(Utilities.COLUMN_PK_PREFIX + i);
                    primaryKeys.put(columns[i - 1], value);
                }
                RecordInstance instance = new RecordInstance(user, recordClass,
                        primaryKeys);
                Favorite favorite = new Favorite(user);
                favorite.setRecordInstance(instance);
                favorite.setNote(rs.getString(COLUMN_RECORD_NOTE));
                favorite.setGroup(rs.getString(COLUMN_RECORD_GROUP));
                list.add(favorite);
            }
            return favorites;
        }
        catch (SQLException e) {
        	throw new WdkModelException("Cannot get favorites for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(rs);
        }
    }

    public boolean isInFavorite(User user, RecordClass recordClass,
            Map<String, Object> recordId) throws WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sqlCount = "SELECT count(*) FROM " + schema + TABLE_FAVORITES
                + " WHERE " + COLUMN_USER_ID + "= ? AND " + COLUMN_PROJECT_ID
                + " = ? AND " + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sqlCount += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            PreparedStatement psCount = SqlUtils.getPreparedStatement(
                    dataSource, sqlCount);
            // check if the record already exists.
            setParams(psCount, userId, projectId, rcName, pkColumns, recordId,
                    1);
            boolean hasRecord = false;
            long start = System.currentTimeMillis();
            resultSet = psCount.executeQuery();
            SqlUtils.verifyTime(wdkModel, sqlCount, "wdk-favorite-count", start);
            if (resultSet.next()) {
                int rsCount = resultSet.getInt(1);
                hasRecord = (rsCount > 0);
            }
            return hasRecord;
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not check whether record id(s) are favorites for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
        }
    }

    public void setNotes(User user, RecordClass recordClass,
            List<Map<String, Object>> recordIds, String note) throws WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sql = "UPDATE " + schema + TABLE_FAVORITES + " SET "
                + COLUMN_RECORD_NOTE + " = ? WHERE " + COLUMN_USER_ID
                + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sql += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psUpdate = null;
        try {
            psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);

            int count = 0;
            for (Map<String, Object> recordId : recordIds) {
                // check if the record already exists.
                psUpdate.setString(1, note);
                setParams(psUpdate, userId, projectId, rcName, pkColumns,
                        recordId, 2);
                psUpdate.addBatch();
                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psUpdate.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-update-note", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psUpdate.executeBatch();
                SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-update-note", -start);
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not set favorite note for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeStatement(psUpdate);
        }
    }

    public void setGroups(User user, RecordClass recordClass,
            List<Map<String, Object>> recordIds, String group) throws WdkModelException {
        int userId = user.getUserId();
        String projectId = wdkModel.getProjectId();
        String rcName = recordClass.getFullName();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String sql = "UPDATE " + schema + TABLE_FAVORITES + " SET "
                + COLUMN_RECORD_GROUP + " = ? WHERE " + COLUMN_USER_ID
                + "= ? AND " + COLUMN_PROJECT_ID + " = ? AND "
                + COLUMN_RECORD_CLASS + " = ?";
        for (int i = 1; i <= pkColumns.length; i++) {
            sql += " AND " + Utilities.COLUMN_PK_PREFIX + i + " = ?";
        }
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psUpdate = null;
        try {
            psUpdate = SqlUtils.getPreparedStatement(dataSource, sql);

            int count = 0;
            for (Map<String, Object> recordId : recordIds) {
                // check if the record already exists.
                psUpdate.setString(1, group);
                setParams(psUpdate, userId, projectId, rcName, pkColumns,
                        recordId, 2);
                psUpdate.addBatch();
                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psUpdate.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-update-group", start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psUpdate.executeBatch();
                SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-update-group", -start);
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not set favorite group for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeStatement(psUpdate);
        }
    }

    public String[] getGroups(User user) throws WdkModelException {
        String sql = "SELECT " + COLUMN_RECORD_GROUP + " FROM " + schema
                + TABLE_FAVORITES + " WHERE " + COLUMN_USER_ID + "= ? AND "
                + COLUMN_PROJECT_ID + " = ?";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            PreparedStatement psSelect = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psSelect.setInt(1, user.getUserId());
            psSelect.setString(2, wdkModel.getProjectId());

            long start = System.currentTimeMillis();
            resultSet = psSelect.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, "wdk-favorite-select-group", start, resultSet);
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
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not set favorite groups for user " + user.getUserId(), e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
        }
    }

    private void setParams(PreparedStatement ps, int userId, String projectId,
            String rcName, String[] pkColumns, Map<String, Object> recordId,
            int index) throws SQLException {
        ps.setInt(index++, userId);
        ps.setString(index++, projectId);
        ps.setString(index++, rcName);
        for (String column : pkColumns) {
            ps.setObject(index++, recordId.get(column));
        }
    }
}
