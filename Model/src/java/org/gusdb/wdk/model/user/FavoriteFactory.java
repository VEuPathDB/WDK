package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;

public class FavoriteFactory {

    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_RECORD_CLASS = "record_class";

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
     * @param pkValues
     *            a list of primary key values. the inner map is a primary-key
     *            column-value map.
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public void addToFavorite(User user, RecordClass recordClass,
            List<String[]> pkValues) throws SQLException, WdkUserException,
            WdkModelException {
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
        sqlInsert += ") VALUES (?, ?, ?" + sqlValues + ")";
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        PreparedStatement psInsert = null, psCount = null;
        try {
            psInsert = SqlUtils.getPreparedStatement(dataSource, sqlInsert);
            psCount = SqlUtils.getPreparedStatement(dataSource, sqlCount);
            int count = 0;
            for (String[] row : pkValues) {
                // fill or truncate the pk columns
                String[] pkValue = new String[pkColumns.length];
                int length = Math.min(row.length, pkValue.length);
                System.arraycopy(row, 0, pkValue, 0, length);

                // check if the record already exists.
                setParams(psCount, userId, projectId, rcName, pkValue);
                boolean hasRecord = false;
                ResultSet resultSet = null;
                try {
                    long start = System.currentTimeMillis();
                    resultSet = psCount.executeQuery();
                    SqlUtils.verifyTime(wdkModel, sqlCount, start);
                    if (resultSet.next()) {
                        int rsCount = resultSet.getInt(1);
                        hasRecord = (rsCount > 0);
                    }
                } finally {
                    if (resultSet != null) resultSet.close();
                }
                if (hasRecord) continue;

                // insert new record
                setParams(psInsert, userId, projectId, rcName, pkValue);
                psInsert.addBatch();

                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psInsert.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sqlInsert, start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psInsert.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlInsert, start);
            }
        } finally {
            SqlUtils.closeStatement(psInsert);
            SqlUtils.closeStatement(psCount);
        }
    }

    public void removeFromFavorite(User user, RecordClass recordClass,
            List<String[]> pkValues) throws SQLException, WdkUserException,
            WdkModelException {
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
            for (String[] row : pkValues) {
                // fill or truncate the pk columns
                String[] pkValue = new String[pkColumns.length];
                int length = Math.min(row.length, pkValue.length);
                System.arraycopy(row, 0, pkValue, 0, length);

                setParams(psDelete, userId, projectId, rcName, pkValue);
                psDelete.addBatch();
                count++;
                if (count % 100 == 0) {
                    long start = System.currentTimeMillis();
                    psDelete.executeBatch();
                    SqlUtils.verifyTime(wdkModel, sqlDelete, start);
                }
            }
            if (count % 100 != 0) {
                long start = System.currentTimeMillis();
                psDelete.executeBatch();
                SqlUtils.verifyTime(wdkModel, sqlDelete, -start);
            }
        } finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public void clearFavorite(User user) throws SQLException, WdkUserException,
            WdkModelException {
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
            SqlUtils.verifyTime(wdkModel, sqlDelete, start);
        } finally {
            SqlUtils.closeStatement(psDelete);
        }
    }

    public int getFavoriteCounts(User user) throws SQLException {
        // load the unique counts
        String sql = "SELECT count(*) AS fav_size FROM " + schema
                + TABLE_FAVORITES + " WHERE " + COLUMN_USER_ID + " = ? AND "
                + COLUMN_PROJECT_ID + " = ?";
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        int count = 0;
        try {
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setInt(1, user.getUserId());
            ps.setString(2, wdkModel.getProjectId());
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("fav_size");
            }
        } finally {
            SqlUtils.closeResultSet(rs);
        }
        return count;
    }

    public Map<String, Favorite> getFavorites(User user)
            throws WdkUserException, WdkModelException, SQLException,
            NoSuchAlgorithmException, JSONException {
        String sql = "SELECT * FROM " + schema + TABLE_FAVORITES + " WHERE "
                + COLUMN_PROJECT_ID + " = ? AND " + COLUMN_USER_ID + " =?"
                + " ORDER BY " + COLUMN_RECORD_CLASS;
        DataSource ds = wdkModel.getUserPlatform().getDataSource();
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            PreparedStatement ps = SqlUtils.getPreparedStatement(ds, sql);
            ps.setFetchSize(1000);
            ps.setString(1, wdkModel.getProjectId());
            ps.setInt(2, user.getUserId());
            rs = ps.executeQuery();
            SqlUtils.verifyTime(wdkModel, sql, start);

            Map<String, Favorite> favorites = new LinkedHashMap<String, Favorite>();
            while (rs.next()) {
                String rcName = rs.getString(COLUMN_RECORD_CLASS);
                RecordClass recordClass = (RecordClass) wdkModel.getRecordClass(rcName);
                Favorite favorite;
                if (favorites.containsKey(rcName)) {
                    favorite = favorites.get(rcName);
                } else {
                    favorite = new Favorite(user);
                    favorite.setRecordClass(recordClass);
                    favorites.put(rcName, favorite);
                }

                String[] columns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
                Map<String, Object> primaryKeys = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= columns.length; i++) {
                    Object value = rs.getObject(Utilities.COLUMN_PK_PREFIX + i);
                    primaryKeys.put(columns[i - 1], value);
                }
                RecordInstance instance = new RecordInstance(user, recordClass,
                        primaryKeys);
                favorite.addRecordInstance(instance);
            }
            return favorites;
        } finally {
            SqlUtils.closeResultSet(rs);
        }
    }

    public boolean isInFavorite(User user, RecordClass recordClass,
            String[] pkValue) throws SQLException, WdkUserException,
            WdkModelException {
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
            // truncate the pk columns
            String[] value = new String[pkColumns.length];
            int length = Math.min(pkColumns.length, pkValue.length);
            System.arraycopy(pkValue, 0, value, 0, length);

            // check if the record already exists.
            setParams(psCount, userId, projectId, rcName, value);
            boolean hasRecord = false;
            long start = System.currentTimeMillis();
            resultSet = psCount.executeQuery();
            SqlUtils.verifyTime(wdkModel, sqlCount, start);
            if (resultSet.next()) {
                int rsCount = resultSet.getInt(1);
                hasRecord = (rsCount > 0);
            }
            return hasRecord;
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
    }

    private void setParams(PreparedStatement ps, int userId, String projectId,
            String rcName, String[] pkValue) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, projectId);
        ps.setString(3, rcName);
        for (int i = 0; i < pkValue.length; i++) {
            ps.setString(i + 4, pkValue[i]);
        }
    }
}
