/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONArray;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

    public static final String TABLE_DATASET_VALUE = "dataset_values";
    public static final String TABLE_DATASET_INDEX = "dataset_indices";
    private static final String TABLE_USER_DATASET = "user_datasets";

    public static final String COLUMN_DATASET_ID = "dataset_id";
    public static final String COLUMN_DATASET_VALUE = "dataset_value";
    public static final String COLUMN_DATASET_CHECKSUM = "dataset_checksum";
    private static final String COLUMN_DATASET_SIZE = "dataset_size";
    private static final String COLUMN_SUMMARY = "summary";
    private static final String COLUMN_USER_DATASET_ID = "user_dataset_id";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_UPLOAD_FILE = "upload_file";

    private static Logger logger = Logger.getLogger(DatasetFactory.class);

    private WdkModel wdkModel;
    private DBPlatform userPlatform;
    private DataSource dataSource;
    private String userSchema;
    private String wdkSchema;

    public DatasetFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.userPlatform = this.wdkModel.getUserPlatform();
        this.dataSource = userPlatform.getDataSource();

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        this.userSchema = userDB.getUserSchema();
        this.wdkSchema = userDB.getWdkEngineSchema();
    }

    /**
     * The method will check if a dataset exists, if not, it will create it.
     * 
     * @param user
     * @param uploadFile
     * @param values
     * @return
     * @throws SQLException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     */
    public Dataset getDataset(User user, String uploadFile, String[] values)
            throws SQLException, NoSuchAlgorithmException, WdkModelException {
        String checksum = getChecksum(values);
        Connection connection = userPlatform.getDataSource().getConnection();

        try {
            Dataset dataset;
            connection.setAutoCommit(false);

            // check if dataset exists
            try {
                // get dataset id, catch WdkModelException if it doesn't exist
                int datasetId = getDatasetId(connection, checksum);
                dataset = new Dataset(this, datasetId);
                loadDatasetIndex(connection, dataset);
            } catch (WdkModelException ex) {
                logger.debug("Creating dataset for user #" + user.getUserId());

                // doesn't exist, create one
                dataset = insertDatasetIndex(connection, checksum, values);
                dataset.setChecksum(checksum);

                // and save the values
                insertDatasetValues(connection, dataset, values);
            }
            dataset.setUser(user);

            // check if user dataset exists
            try {
                int userDatasetId = getUserDatasetId(connection,
                        dataset.getDatasetId());
                dataset.setUserDatasetId(userDatasetId);
                loadUserDataset(connection, dataset);
            } catch (WdkModelException ex) {
                // user-dataset doesn't exist, insert it
                dataset.setUploadFile(uploadFile);
                insertUserDataset(connection, dataset);
            }
            connection.commit();
            return dataset;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    /**
     * Get dataset by userDatasetId;
     * 
     * @param user
     * @param userDatasetId
     * @return
     * @throws SQLException
     * @throws WdkModelException
     *             throws if the userDatasetId doesn't exist or doesn't belong
     *             to the given user.
     */
    public Dataset getDataset(User user, int userDatasetId)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_DATASET_ID);
        sql.append(" FROM ").append(userSchema).append(TABLE_USER_DATASET);
        sql.append(" WHERE ").append(UserFactory.COLUMN_USER_ID);
        sql.append(" = ").append(user.getUserId());
        sql.append(" AND ").append(COLUMN_USER_DATASET_ID);
        sql.append(" = ").append(userDatasetId);

        DataSource dataSource = userPlatform.getDataSource();
        Object result = SqlUtils.executeScalar(dataSource, sql.toString());
        int datasetId = Integer.parseInt(result.toString());

        Dataset dataset = new Dataset(this, datasetId);
        dataset.setUser(user);
        dataset.setUserDatasetId(userDatasetId);

        Connection connection = dataSource.getConnection();
        try {
            loadDatasetIndex(connection, dataset);
            loadUserDataset(connection, dataset);
        } finally {
            if (connection != null) connection.close();
        }
        return dataset;
    }

    /**
     * Get a dataset from checksum; if the dataset exists but userDataset
     * doesn't, a new user dataset will be created
     * 
     * @param user
     * @param datasetChecksum
     * @return
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkModelException
     *             throws if the dataset doesn't exist;
     */
    public Dataset getDataset(User user, String datasetChecksum)
            throws SQLException, WdkModelException {
        // get dataset id
        StringBuffer sqlDatasetId = new StringBuffer("SELECT ");
        sqlDatasetId.append(COLUMN_DATASET_ID);
        sqlDatasetId.append(" FROM ").append(wdkSchema).append(
                TABLE_DATASET_INDEX);
        sqlDatasetId.append(" WHERE ").append(COLUMN_DATASET_CHECKSUM).append(
                " = ?");
        Object result = SqlUtils.executeScalar(dataSource,
                sqlDatasetId.toString());
        int datasetId = Integer.parseInt(result.toString());

        // try to get a user dataset id
        Connection connection = dataSource.getConnection();
        try {
            connection.setAutoCommit(false);

            Dataset dataset = new Dataset(this, datasetId);
            dataset.setUser(user);
            loadDatasetIndex(connection, dataset);
            try {
                int userDatasetId = getUserDatasetId(connection, datasetId);
                dataset.setUserDatasetId(userDatasetId);
                loadUserDataset(connection, dataset);
            } catch (WdkModelException ex) {
                // user data set doesn't exist
                dataset.setUploadFile("");
                insertUserDataset(connection, dataset);
            }
            return dataset;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }

    String[] getDatasetValues(Dataset dataset) throws SQLException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_DATASET_VALUE);
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_VALUE);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID);
        sql.append(" = ").append(dataset.getDatasetId());

        ResultSet resultSet = null;
        DataSource dataSource = userPlatform.getDataSource();
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql.toString());
            List<String> values = new ArrayList<String>();
            while (resultSet.next()) {
                values.add(COLUMN_DATASET_VALUE);
            }
            String[] array = new String[values.size()];
            values.toArray(array);
            return array;
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }

    }

    /**
     * @param connection
     * @param datasetChecksum
     * @return returns dataset Id.
     * @throws WdkModelException
     *             the dataset does not exist
     * @throws SQLException
     *             the database or query failure
     */
    private int getDatasetId(Connection connection, String datasetChecksum)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_DATASET_ID);
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_INDEX);
        sql.append(" WHERE ").append(COLUMN_DATASET_CHECKSUM);
        sql.append(" = '").append(datasetChecksum).append("'");

        Object result = SqlUtils.executeScalar(dataSource, sql.toString());
        return Integer.parseInt(result.toString());
    }

    /**
     * @param connection
     * @param datasetId
     * @return the user-dataset-id.
     * @throws WdkModelException
     *             the userDataset does not exist
     * @throws SQLException
     *             the database or query failure
     */
    private int getUserDatasetId(Connection connection, int datasetId)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_USER_DATASET_ID);
        sql.append(" FROM ").append(userSchema).append(TABLE_USER_DATASET);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID);
        sql.append(" = ").append(datasetId);

        Object result = SqlUtils.executeScalar(dataSource, sql.toString());
        return Integer.parseInt(result.toString());
    }

    private Dataset insertDatasetIndex(Connection connection, String checksum,
            String[] values) throws SQLException, WdkModelException {
        // get a new dataset id
        int datasetId = userPlatform.getNextId(wdkSchema, TABLE_DATASET_INDEX);
        Dataset dataset = new Dataset(this, datasetId);
        dataset.setChecksum(checksum);

        // set summary
        dataset.setValues(values);

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(wdkSchema).append(TABLE_DATASET_INDEX).append(" (");
        sql.append(COLUMN_DATASET_ID).append(", ");
        sql.append(COLUMN_DATASET_CHECKSUM).append(", ");
        sql.append(COLUMN_DATASET_SIZE).append(", ");
        sql.append(COLUMN_SUMMARY).append(") VALUES (?, ?, ?, ?)");
        PreparedStatement psInsert = connection.prepareStatement(sql.toString());
        try {
            psInsert.setInt(1, datasetId);
            psInsert.setString(2, checksum);
            psInsert.setInt(3, dataset.getSize());
            psInsert.setString(4, dataset.getSummary());
            psInsert.execute();
        } finally {
            if (psInsert != null) psInsert.close();
        }
        return dataset;
    }

    private void insertDatasetValues(Connection connection, Dataset dataset,
            String[] values) throws SQLException {
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(wdkSchema).append(TABLE_DATASET_VALUE).append(" (");
        sql.append(COLUMN_DATASET_ID).append(", ");
        sql.append(COLUMN_DATASET_VALUE).append(") VALUE (?, ?)");

        PreparedStatement psInsert = connection.prepareStatement(sql.toString());
        try {
            for (int i = 0; i < values.length; i++) {
                psInsert.setInt(1, dataset.getDatasetId());
                psInsert.setString(2, values[i]);
                psInsert.addBatch();

                if ((i + 1) % 1000 == 0) psInsert.executeBatch();
            }
            if (values.length % 1000 != 0) psInsert.executeBatch();
        } finally {
            if (psInsert != null) psInsert.close();
        }
    }

    private void insertUserDataset(Connection connection, Dataset dataset)
            throws SQLException, WdkModelException {
        // get new user dataset id
        int userDatasetId = userPlatform.getNextId(userSchema,
                TABLE_USER_DATASET);
        dataset.setUserDatasetId(userDatasetId);
        dataset.setCreateTime(new Date());

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(userSchema).append(TABLE_USER_DATASET).append(" (");
        sql.append(COLUMN_USER_DATASET_ID).append(", ");
        sql.append(COLUMN_DATASET_ID).append(", ");
        sql.append(UserFactory.COLUMN_USER_ID).append(", ");
        sql.append(COLUMN_CREATE_TIME).append(", ");
        sql.append(COLUMN_UPLOAD_FILE).append(") VALUES (?, ?, ?, ?, ?)");

        PreparedStatement psInsert = connection.prepareStatement(sql.toString());
        try {
            psInsert.setInt(1, userDatasetId);
            psInsert.setInt(2, dataset.getDatasetId());
            psInsert.setInt(3, dataset.getUser().getUserId());
            psInsert.setTimestamp(4, new Timestamp(
                    dataset.getCreateTime().getTime()));
            psInsert.setString(5, dataset.getUploadFile());
            psInsert.executeUpdate();
        } finally {
            if (psInsert != null) psInsert.close();
        }
    }

    private void loadDatasetIndex(Connection connection, Dataset dataset)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_DATASET_CHECKSUM).append(", ");
        sql.append(COLUMN_DATASET_SIZE).append(", ").append(COLUMN_SUMMARY);
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_INDEX);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID).append(" = ");
        sql.append(dataset.getDatasetId());

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql.toString());
        try {
            if (!resultSet.next())
                throw new WdkModelException("The dataset ("
                        + dataset.getDatasetId() + ") does not exist.");
            dataset.setChecksum(resultSet.getString(COLUMN_DATASET_CHECKSUM));
            dataset.setSize(resultSet.getInt(COLUMN_DATASET_SIZE));
            dataset.setSummary(resultSet.getString(COLUMN_SUMMARY));
        } finally {
            try {
                if (resultSet != null) resultSet.close();
            } finally {
                if (stmt != null) stmt.close();
            }
        }
    }

    private void loadUserDataset(Connection connection, Dataset dataset)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_CREATE_TIME).append(", ").append(COLUMN_UPLOAD_FILE);
        sql.append(" FROM ").append(userSchema).append(TABLE_USER_DATASET);
        sql.append(" WHERE ").append(COLUMN_USER_DATASET_ID).append(" = ");
        sql.append(dataset.getUserDatasetId());

        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery(sql.toString());
        try {
            if (!resultSet.next())
                throw new WdkModelException("The userDataset ("
                        + dataset.getUserDatasetId() + ") does not exist.");
            dataset.setCreateTime(resultSet.getDate(COLUMN_CREATE_TIME));
            dataset.setUploadFile(resultSet.getString(COLUMN_UPLOAD_FILE));
        } finally {
            try {
                if (resultSet != null) resultSet.close();
            } finally {
                if (stmt != null) stmt.close();
            }
        }
    }

    private String getChecksum(String[] values)
            throws NoSuchAlgorithmException, WdkModelException {
        // sort the value list
        Arrays.sort(values);
        JSONArray array = new JSONArray();
        for (String value : values) {
            array.put(value);
        }
        return Utilities.encrypt(array.toString());
    }
}
