/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

    private static final String DATASET_INDEX_TABLE = "datasets";
    private static final String DATASET_TABLE_PREFIX = "dataset_";

    public static final String COLUMN_PROJECT_ID = "project_id";
    public static final String COLUMN_PRIMARY_KEY = "primary_key";

    public static final int COLUMN_PROJECT_ID_WIDTH = 200;
    public static final int COLUMN_PRIMARY_KEY_WIDTH = 4000;

    private RDBMSPlatformI platform;
    private String datasetSchema;

    public DatasetFactory(RDBMSPlatformI platform, String datasetSchema) {
        this.platform = platform;
        this.datasetSchema = datasetSchema;
    }

    public String getDatasetSchema() {
        return datasetSchema;
    }

    Map<String, Dataset> loadDatasets(User user) throws WdkUserException {
        int userId = user.getUserId();
        ResultSet rsInfo = null;
        Map<String, Dataset> datasets = new LinkedHashMap<String, Dataset>();
        try {
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT dataset_id, "
                            + "dataset_name, cache_table, temporary, "
                            + "create_time, data_type FROM " + datasetSchema
                            + DATASET_INDEX_TABLE + " WHERE user_id = ? AND "
                            + "temporary = 1");
            psInfo.setInt(1, userId);
            rsInfo = psInfo.executeQuery();
            while (rsInfo.next()) {
                Dataset dataset = new Dataset(this, userId, rsInfo.getInt("dataset_id"));
                dataset.setDatasetName(rsInfo.getString("dataset_name"));
                dataset.setCacheTable(rsInfo.getString("cache_table"));
                dataset.setTemporary(rsInfo.getBoolean("temporary"));
                Timestamp createTime = rsInfo.getTimestamp("create_time");
                dataset.setCreateTime(new Date(createTime.getTime()));
                dataset.setDataType(rsInfo.getString("data_type"));
                datasets.put(dataset.getDatasetName(), dataset);
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return datasets;
    }

    Dataset loadDataset(int datasetId) throws WdkUserException {
        Dataset dataset = null;
        ResultSet rsInfo = null;
        try {
            // get dataset information
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT user_id, dataset_name, "
                            + "cache_table, temporary, create_time, "
                            + "data_type FROM " + datasetSchema
                            + DATASET_INDEX_TABLE + " WHERE dataset_id = ?");
            psInfo.setInt(1, datasetId);
            rsInfo = psInfo.executeQuery();
            if (rsInfo.next()) {
                int userId = rsInfo.getInt("user_id");
                dataset = new Dataset(this, userId, datasetId);
                dataset.setDatasetName(rsInfo.getString("dataset_name"));
                dataset.setCacheTable(rsInfo.getString("cache_table"));
                dataset.setTemporary(rsInfo.getBoolean("temporary"));
                Timestamp createTime = rsInfo.getTimestamp("create_time");
                dataset.setCreateTime(new Date(createTime.getTime()));
                dataset.setDataType(rsInfo.getString("data_type"));
            } else throw new WdkUserException("The dataset with the given id "
                    + datasetId + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return dataset;
    }

    Dataset loadDataset(User user, String datasetName) throws WdkUserException {
        int userId = user.getUserId();
        Dataset dataset = null;
        ResultSet rsInfo = null;
        try {
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT dataset_id, cache_table,"
                            + " temporary, create_time, data_type FROM "
                            + datasetSchema + DATASET_INDEX_TABLE
                            + " WHERE user_id = ? AND dataset_name = ?");
            psInfo.setInt(1, userId);
            psInfo.setString(2, datasetName);
            rsInfo = psInfo.executeQuery();
            if (rsInfo.next()) {
                dataset = new Dataset(this,userId,  rsInfo.getInt("dataset_id"));
                dataset.setDatasetName(datasetName);
                dataset.setCacheTable(rsInfo.getString("cache_table"));
                dataset.setTemporary(rsInfo.getBoolean("temporary"));
                Timestamp createTime = rsInfo.getTimestamp("create_time");
                dataset.setCreateTime(new Date(createTime.getTime()));
                dataset.setDataType(rsInfo.getString("data_type"));
            } else throw new WdkUserException(
                    "The dataset with the given name " + datasetName
                            + " cannot be found.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return dataset;
    }

    String[][] loadDatasetValues(String cacheTable) throws WdkUserException {
        ResultSet rsValue = null;
        try {
            rsValue = SqlUtils.getResultSet(platform.getDataSource(), "SELECT "
                    + COLUMN_PROJECT_ID + ", " + COLUMN_PRIMARY_KEY + " FROM "
                    + datasetSchema + cacheTable);
            List<String[]> values = new ArrayList<String[]>();
            while (rsValue.next()) {
                String[] row = new String[2];
                row[0] = rsValue.getString(COLUMN_PROJECT_ID);
                row[1] = rsValue.getString(COLUMN_PRIMARY_KEY);
                values.add(row);
            }
            String[][] array = new String[values.size()][2];
            values.toArray(array);
            return array;
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsValue);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    public Dataset createDataset(User user, String datasetName,
            String dataType, String[][] values, boolean temporary)
            throws WdkUserException {
        int userId = user.getUserId();
        if (datasetName.length() > 200)
            datasetName = datasetName.substring(0, 200);
        
        Dataset dataset = null;
        DataSource dataSource = platform.getDataSource();
        PreparedStatement psInfo = null;
        PreparedStatement psCreate = null;
        PreparedStatement psValue = null;
        try {
            // get next available dataset id
            int datasetId = Integer.parseInt(platform.getNextId(datasetSchema,
                    DATASET_INDEX_TABLE));
            // check if name is unique
            if (!checkAvailability(user, datasetName))
                datasetName += " #" + datasetId;
            String cacheTable = DATASET_TABLE_PREFIX + datasetId;
            Date createTime = new Date();

            // update the info of dataset
            psInfo = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + datasetSchema + DATASET_INDEX_TABLE + " (dataset_id, "
                    + "user_id, dataset_name, cache_table, temporary, "
                    + "create_time, date_type) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            psInfo.setInt(1, datasetId);
            psInfo.setInt(2, userId);
            psInfo.setString(3, datasetName);
            psInfo.setString(4, cacheTable);
            psInfo.setBoolean(5, temporary);
            psInfo.setTimestamp(6, new Timestamp(createTime.getTime()));
            psInfo.setString(7, dataType);
            psInfo.executeUpdate();

            // create the dataset table
            psCreate = SqlUtils.getPreparedStatement(dataSource, "CREATE "
                    + "TABLE " + datasetSchema + cacheTable + " ("
                    + COLUMN_PROJECT_ID + " varchar(" + COLUMN_PROJECT_ID_WIDTH
                    + "), " + COLUMN_PRIMARY_KEY + " varchar("
                    + COLUMN_PRIMARY_KEY_WIDTH + "))");
            psCreate.executeUpdate();

            // construct dataset
            dataset = new Dataset(this, userId, datasetId);
            dataset.setDatasetName(datasetName);
            dataset.setCacheTable(cacheTable);
            dataset.setCreateTime(createTime);
            dataset.setTemporary(temporary);
            dataset.setDataType(dataType);

            // then save the values
            saveDatasetValue(dataset, values);
        } catch (NumberFormatException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psInfo);
                SqlUtils.closeStatement(psCreate);
                SqlUtils.closeStatement(psValue);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return dataset;
    }

    public Dataset createDataset(User user, String datasetName,
            String dataType, String cacheFullTable, 
            String primaryKeyColumn, boolean temporary) throws WdkUserException {
        int userId = user.getUserId();
        if (datasetName.length() > 200)
            datasetName = datasetName.substring(0, 200);
        
        Dataset dataset = null;
        DataSource dataSource = platform.getDataSource();
        PreparedStatement psInfo = null;
        PreparedStatement psCreate = null;
        try {
            // get next available dataset id
            int datasetId = Integer.parseInt(platform.getNextId(datasetSchema,
                    DATASET_INDEX_TABLE));
            // check if name is unique
            if (!checkAvailability(user, datasetName))
                datasetName += " #" + datasetId;
            String cacheTable = DATASET_TABLE_PREFIX + datasetId;
            Date createTime = new Date();

            // update the info of dataset
            psInfo = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + datasetSchema + DATASET_INDEX_TABLE + " (dataset_id, "
                    + "user_id, dataset_name, cache_table, temporary, "
                    + "create_time, data_type) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            psInfo.setInt(1, datasetId);
            psInfo.setInt(2, userId);
            psInfo.setString(3, datasetName);
            psInfo.setString(4, cacheTable);
            psInfo.setBoolean(5, temporary);
            psInfo.setTimestamp(6, new Timestamp(createTime.getTime()));
            psInfo.setString(7, dataType);
            psInfo.executeUpdate();

            // create the dataset from the cache
            StringBuffer sbCreate = new StringBuffer("CREATE TABLE ");
            sbCreate.append(datasetSchema + cacheTable);
            sbCreate.append(" AS SELECT ");
            sbCreate.append(primaryKeyColumn + " AS " + COLUMN_PRIMARY_KEY);
            sbCreate.append(" FROM " + cacheFullTable);
            psCreate = SqlUtils.getPreparedStatement(dataSource,
                    sbCreate.toString());
            psCreate.executeUpdate();

            // construct dataset
            dataset = new Dataset(this, userId, datasetId);
            dataset.setDatasetName(datasetName);
            dataset.setCacheTable(cacheTable);
            dataset.setCreateTime(createTime);
            dataset.setTemporary(temporary);
            dataset.setDataType(dataType);
        } catch (NumberFormatException ex) {
            throw new WdkUserException(ex);
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psInfo);
                SqlUtils.closeStatement(psCreate);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return dataset;
    }

    boolean checkAvailability(User user, String datasetName)
            throws WdkUserException {
        ResultSet rsInfo = null;
        boolean available = false;
        try {
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), "SELECT dataset_id FROM "
                            + datasetSchema + DATASET_INDEX_TABLE + " WHERE "
                            + "user_id = ? AND dataset_name = ?");
            psInfo.setInt(1, user.getUserId());
            psInfo.setString(2, datasetName);
            rsInfo = psInfo.executeQuery();
            available = rsInfo.next();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
        return available;
    }

    void saveDatasetInfo(Dataset dataset) throws WdkUserException {
        int datasetId = dataset.getDatasetId();
        PreparedStatement psInfo = null;
        try {
            psInfo = SqlUtils.getPreparedStatement(platform.getDataSource(),
                    "UPDATE " + datasetSchema + DATASET_INDEX_TABLE + " SET "
                            + "dataset_name = ?, cache_table = ?, "
                            + "temporary = ?, data_type = ? "
                            + "WHERE dataset_id = ?");
            psInfo.setString(1, dataset.getDatasetName());
            psInfo.setString(2, dataset.getCacheTable());
            psInfo.setBoolean(3, dataset.isTemporary());
            psInfo.setString(4, dataset.getDataType());
            psInfo.setInt(5, datasetId);
            int rows = psInfo.executeUpdate();
            if (rows == 0)
                throw new WdkUserException("The dataset #" + datasetId
                        + " cannot be found in the database.");
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psInfo);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void saveDatasetValue(Dataset dataset, String[][] values)
            throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        String cacheTable = dataset.getCacheTable();
        PreparedStatement psDelete = null;
        PreparedStatement psValue = null;
        try {
            // delete the old data
            psDelete = SqlUtils.getPreparedStatement(dataSource, "DELETE FROM "
                    + datasetSchema + cacheTable);
            psDelete.executeUpdate();

            // insert values
            psValue = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
                    + datasetSchema + cacheTable + " (" + COLUMN_PROJECT_ID
                    + ", " + COLUMN_PRIMARY_KEY + ") VALUES (?, ?)");
            for (String[] row : values) {
                psValue.setString(1, row[0]);
                psValue.setString(2, row[1]);
                psValue.executeQuery();
            }
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psDelete);
                SqlUtils.closeStatement(psValue);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteDataset(User user, String datasetName) throws WdkUserException {
        int userId = user.getUserId();
        DataSource dataSource = platform.getDataSource();
        ResultSet rsInfo = null;
        PreparedStatement psDelInfo = null;
        PreparedStatement psDelValue = null;
        try {
            // get cache table name and dataset id
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT dataset_id, cache_table FROM "
                            + datasetSchema + DATASET_INDEX_TABLE + " WHERE "
                            + "user_id = ? AND dataset_name = ?");
            psInfo.setInt(1, userId);
            psInfo.setString(2, datasetName);
            rsInfo = psInfo.executeQuery();
            if (!rsInfo.next())
                throw new WdkUserException("The dataset [" + userId + "] "
                        + datasetName + " cannot be found in the database.");

            int datasetId = rsInfo.getInt("dataset_id");
            String cacheTable = rsInfo.getString("cache_table");

            // delete dataset entry
            psDelInfo = SqlUtils.getPreparedStatement(dataSource,
                    "DELETE FROM " + datasetSchema + DATASET_INDEX_TABLE
                            + " WHERE dataset_id = ?");
            psDelInfo.setInt(1, datasetId);
            psDelInfo.executeUpdate();

            // drop the dataset cache table
            psDelValue = SqlUtils.getPreparedStatement(dataSource,
                    "DROP TABLE " + datasetSchema + cacheTable);
            psDelValue.executeUpdate();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
                SqlUtils.closeStatement(psDelInfo);
                SqlUtils.closeStatement(psDelValue);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteDataset(int datasetId) throws WdkUserException {
        DataSource dataSource = platform.getDataSource();
        ResultSet rsInfo = null;
        PreparedStatement psDelInfo = null;
        PreparedStatement psDelValue = null;
        try {
            // get cache table name
            PreparedStatement psInfo = SqlUtils.getPreparedStatement(
                    dataSource, "SELECT cache_table FROM " + datasetSchema
                            + DATASET_INDEX_TABLE + " WHERE dataset_id = ?");
            psInfo.setInt(1, datasetId);
            rsInfo = psInfo.executeQuery();
            if (!rsInfo.next())
                throw new WdkUserException("The dataset #" + datasetId
                        + " cannot be found in the database.");

            String cacheTable = rsInfo.getString("cache_table");

            // delete dataset entry
            psDelInfo = SqlUtils.getPreparedStatement(dataSource,
                    "DELETE FROM " + datasetSchema + DATASET_INDEX_TABLE
                            + " WHERE dataset_id = ?");
            psDelInfo.setInt(1, datasetId);
            psDelInfo.executeUpdate();

            // drop the dataset cache table
            psDelValue = SqlUtils.getPreparedStatement(dataSource,
                    "DROP TABLE " + datasetSchema + cacheTable);
            psDelValue.executeUpdate();
        } catch (SQLException ex) {
            throw new WdkUserException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsInfo);
                SqlUtils.closeStatement(psDelInfo);
                SqlUtils.closeStatement(psDelValue);
            } catch (SQLException ex) {
                throw new WdkUserException(ex);
            }
        }
    }

    void deleteDatasets(User user) throws WdkUserException {
        Dataset[] datasets = user.getDatasets();
        for (Dataset dataset : datasets) {
            deleteDataset(dataset.getDatasetId());
        }
    }
}
