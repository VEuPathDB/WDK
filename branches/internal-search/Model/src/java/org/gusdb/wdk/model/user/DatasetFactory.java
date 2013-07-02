/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
import org.json.JSONArray;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

    public static final String TABLE_DATASET_VALUE = "dataset_values";
    public static final String TABLE_DATASET_INDEX = "dataset_indices";
    public static final String TABLE_USER_DATASET = "user_datasets2";

    public static final String COLUMN_DATASET_ID = "dataset_id";
    public static final String COLUMN_DATASET_CHECKSUM = "dataset_checksum";
    public static final String COLUMN_USER_DATASET_ID = "user_dataset_id";
    private static final String COLUMN_DATASET_SIZE = "dataset_size";
    private static final String COLUMN_SUMMARY = "summary";
    private static final String COLUMN_CREATE_TIME = "create_time";
    private static final String COLUMN_UPLOAD_FILE = "upload_file";
    private static final String COLUMN_RECORD_CLASS = "record_class";

    private static final String REGEX_COLUMN_DIVIDER = "[\\|]+";
    private static final String REGEX_RECORD_DIVIDER = "[,\\s;]+";

    public static final String RECORD_DIVIDER = ";";
    public static final String COLUMN_DIVIDER = "|";

    private static final int MAX_VALUE_LENGTH = 1000;

    private static Logger logger = Logger.getLogger(DatasetFactory.class);

    private WdkModel wdkModel;
    private DatabaseInstance userDb;
    private DataSource dataSource;
    private String userSchema;
    private String wdkSchema;

    public DatasetFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.userDb = this.wdkModel.getUserDb();
        this.dataSource = userDb.getDataSource();

        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        this.userSchema = userDB.getUserSchema();
        this.wdkSchema = userDB.getWdkEngineSchema();
    }

    public Dataset getDataset(User user, RecordClass recordClass,
            String uploadFile, String strValues)
            throws WdkModelException {
        List<String[]> values = parseValues(recordClass, strValues);
        return getDataset(user, recordClass, uploadFile, values);
    }

    public Dataset getDataset(User user, RecordClass recordClass,
            String uploadFile, List<String[]> values)
            throws WdkModelException {
        if (values.size() == 0)
            throw new WdkDatasetException("The dataset is empty. User #"
                    + user.getUserId());

        Connection connection = null;
        try {
            // remove duplicates
            removeDuplicates(values);
        
            String checksum = getChecksum(values);
            connection = userDb.getDataSource().getConnection();
            Dataset dataset;
            connection.setAutoCommit(false);

            boolean needRefresh = false;
            // check if dataset exists
            try {
                // get dataset id, catch WdkModelException if it doesn't exist
                int datasetId = getDatasetId(connection, checksum);
                dataset = new Dataset(this, datasetId);
                dataset.setRecordClass(recordClass);
                loadDatasetIndex(connection, dataset);
            }
            catch (WdkModelException ex) {
                logger.debug("Creating dataset for user #" + user.getUserId());

                // doesn't exist, create one
                dataset = insertDatasetIndex(recordClass, connection, checksum,
                        values);
                dataset.setChecksum(checksum);

                // and save the values
                insertDatasetValues(recordClass, connection, dataset, values);
                needRefresh = true;
            }
            dataset.setUser(user);

            // check if user dataset exists
            try {
                int userDatasetId = getUserDatasetId(connection, user,
                        dataset.getDatasetId());
                logger.debug("user dataset exist: " + userDatasetId);
                dataset.setUserDatasetId(userDatasetId);
                loadUserDataset(connection, dataset);
            }
            catch (WdkModelException ex) {
                // user-dataset doesn't exist, insert it
                dataset.setUploadFile(uploadFile);
                insertUserDataset(connection, dataset);
                needRefresh = true;
            }
            connection.commit();

            if (needRefresh) {
                // check remote table to solve out-dated db-link issue with Oracle.
                checkRemoteTable();
            }

            return dataset;
        }
        catch (NoSuchAlgorithmException e) {
        	throw new WdkRuntimeException(e);
        }
        catch (SQLException e) {
        	try {
        		if (connection != null) connection.rollback();
        	}
        	catch(SQLException e2) {
        		logger.error("Unable to roll back exception after SQL error during processing.", e2);
        	}
            throw new WdkRuntimeException("Error while retrieving dataset.", e);
        }
        finally {
        	try {
        		if (connection != null) {
        			connection.setAutoCommit(true);
        			connection.close();
        		}
        	}
        	catch(SQLException e2) {
        		logger.error("Unable to close DB connection.", e2);
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
     * @throws WdkUserException
     */
    public Dataset getDataset(User user, int userDatasetId)
            throws WdkModelException {
	        StringBuffer sql = new StringBuffer("SELECT ");
	        sql.append(COLUMN_DATASET_ID);
	        sql.append(" FROM ").append(userSchema).append(TABLE_USER_DATASET);
	        sql.append(" WHERE ").append(Utilities.COLUMN_USER_ID);
	        sql.append(" = ").append(user.getUserId());
	        sql.append(" AND ").append(COLUMN_USER_DATASET_ID);
	        sql.append(" = ").append(userDatasetId);
	
	        DataSource dataSource = userDb.getDataSource();
	        Connection connection = null;
	        try {
	        	Object result = SqlUtils.executeScalar(dataSource,
	        			sql.toString(), "wdk-dataset-factory-dataset-by-user-dataset");
	        	int datasetId = Integer.parseInt(result.toString());
	
	        	Dataset dataset = new Dataset(this, datasetId);
	        	dataset.setUser(user);
	        	dataset.setUserDatasetId(userDatasetId);
	
	        	connection = dataSource.getConnection();
	            loadDatasetIndex(connection, dataset);
	            loadUserDataset(connection, dataset);
	            return dataset;
	        }
	        catch (SQLException e) {
	        	throw new WdkModelException("Unable to get data set with ID: " + userDatasetId, e);
	        }
	        finally {
	            if (connection != null)
	            	try { connection.close(); }
	            	catch (SQLException e) { logger.error("Unable to close DB conection!", e); }
	        }
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
     *             throws if the dataset doesn't exist;
     * @throws WdkUserException
     */
    public Dataset getDataset(User user, String datasetChecksum)
            throws WdkModelException {
    	try {
	        // get dataset id
	        StringBuffer sqlDatasetId = new StringBuffer("SELECT ");
	        sqlDatasetId.append(COLUMN_DATASET_ID);
	        sqlDatasetId.append(" FROM ").append(wdkSchema).append(
	                TABLE_DATASET_INDEX);
	        sqlDatasetId.append(" WHERE ").append(COLUMN_DATASET_CHECKSUM).append(
	                " = ?");
	        int datasetId;
	        ResultSet resultSet = null;
	        try {
	            long start = System.currentTimeMillis();
	            String sql = sqlDatasetId.toString();
	            PreparedStatement psQuery = SqlUtils.getPreparedStatement(dataSource, sql);
	            psQuery.setString(1, datasetChecksum);
	            resultSet = psQuery.executeQuery();
	            QueryLogger.logEndStatementExecution(sql,
	                    "wdk-dataset-factory-dataset-by-checksum", start);
	            if (!resultSet.next())
	                throw new WdkModelException("The dataset with checksum '"
	                        + datasetChecksum + "' doesn't exist.");
	            datasetId = resultSet.getInt(COLUMN_DATASET_ID);
	        }
	        finally {
	            SqlUtils.closeResultSetAndStatement(resultSet);
	        }
	
	        // try to get a user dataset id
	        Connection connection = dataSource.getConnection();
	        try {
	            connection.setAutoCommit(false);
	
	            Dataset dataset = new Dataset(this, datasetId);
	            dataset.setUser(user);
	            loadDatasetIndex(connection, dataset);
	            try {
	                int userDatasetId = getUserDatasetId(connection, user,
	                        datasetId);
	                dataset.setUserDatasetId(userDatasetId);
	                loadUserDataset(connection, dataset);
	            }
	            catch (WdkModelException ex) {
	                // user data set doesn't exist
	                dataset.setUploadFile("");
	                insertUserDataset(connection, dataset);
	            }
	            return dataset;
	        }
	        catch (SQLException ex) {
	            connection.rollback();
	            throw ex;
	        }
	        finally {
	            connection.setAutoCommit(true);
	            connection.close();
	        }
    	}
    	catch (SQLException e) {
    		throw new WdkModelException("Unable to retrieve data set.", e);
    	}
    }

    List<String> getDatasetValues(Dataset dataset) throws WdkModelException {
        String columnPrefx = Utilities.COLUMN_PK_PREFIX;
        int columnCount = Utilities.MAX_PK_COLUMN_COUNT;
        StringBuffer sql = new StringBuffer();
        for (int i = 1; i <= columnCount; i++) {
            if (sql.length() == 0) sql.append("SELECT ");
            else sql.append(", ");
            sql.append(columnPrefx + i);
        }
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_VALUE);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID);
        sql.append(" = ").append(dataset.getDatasetId());

        ResultSet resultSet = null;
        DataSource dataSource = userDb.getDataSource();
        try {
            resultSet = SqlUtils.executeQuery(dataSource,
                    sql.toString(), "wdk-dataset-factory-dataset-by-id");

            RecordClass recordClass = dataset.getRecordClass();
            PrimaryKeyAttributeField pkField = recordClass.getPrimaryKeyAttributeField();
            String[] pkColumns = pkField.getColumnRefs();

            List<String> values = new ArrayList<String>();
            while (resultSet.next()) {
                Map<String, Object> columnValues = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= pkColumns.length; i++) {
                    String column = pkColumns[i - 1];
                    Object columnValue = resultSet.getObject(Utilities.COLUMN_PK_PREFIX
                            + i);
                    columnValues.put(column, columnValue);
                }
                // create primary key value stub to format the primary key
                PrimaryKeyAttributeValue pkValue = new PrimaryKeyAttributeValue(
                        pkField, columnValues);
                values.add(pkValue.getValue().toString());
            }
            return values;
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not retrieve dataset values.", e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
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
     * @throws WdkUserException
     */
    private int getDatasetId(Connection connection, String datasetChecksum)
            throws WdkModelException {
      try {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_DATASET_ID);
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_INDEX);
        sql.append(" WHERE ").append(COLUMN_DATASET_CHECKSUM);
        sql.append(" = '").append(datasetChecksum).append("'");

        Object result = SqlUtils.executeScalar(dataSource,
                sql.toString(), "wdk-dataset-factory-id-by-checksum");
        return Integer.parseInt(result.toString());
      }
      catch (SQLException e) {
        throw new WdkModelException(e);
      }
    }

    /**
     * @param connection
     * @param datasetId
     * @return the user-dataset-id.
     * @throws WdkModelException
     *             the userDataset does not exist
     * @throws SQLException
     *             the database or query failure
     * @throws WdkUserException
     */
    public int getUserDatasetId(Connection connection, User user, int datasetId)
            throws WdkModelException {
      try {
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(COLUMN_USER_DATASET_ID);
        sql.append(" FROM ").append(userSchema).append(TABLE_USER_DATASET);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID).append(" = ").append(
                datasetId);
        sql.append(" AND ").append(Utilities.COLUMN_USER_ID).append(" = ").append(
                user.getUserId());

        Object result = SqlUtils.executeScalar(dataSource,
                sql.toString(), "wdk-dataset-factory-user-dataset-id");
        return Integer.parseInt(result.toString());
      }
      catch (SQLException e) {
        throw new WdkModelException(e);
      }
    }

    private Dataset insertDatasetIndex(RecordClass recordClass,
            Connection connection, String checksum, List<String[]> values)
            throws WdkModelException {
        PreparedStatement psInsert = null;
        try {
          // get a new dataset id
          int datasetId = userDb.getPlatform().getNextId(dataSource, wdkSchema, TABLE_DATASET_INDEX);
          Dataset dataset = new Dataset(this, datasetId);
          dataset.setChecksum(checksum);
          dataset.setRecordClass(recordClass);
  
          // set summary
          dataset.setSummary(values);
  
          StringBuffer sql = new StringBuffer("INSERT INTO ");
          sql.append(wdkSchema).append(TABLE_DATASET_INDEX).append(" (");
          sql.append(COLUMN_DATASET_ID).append(", ");
          sql.append(COLUMN_DATASET_CHECKSUM).append(", ");
          sql.append(COLUMN_DATASET_SIZE).append(", ");
          sql.append(COLUMN_RECORD_CLASS).append(", ");
          sql.append(COLUMN_SUMMARY).append(") VALUES (?, ?, ?, ?, ?)");
          
        	psInsert = connection.prepareStatement(sql.toString());
          psInsert.setInt(1, datasetId);
          psInsert.setString(2, checksum);
          psInsert.setInt(3, dataset.getSize());
          psInsert.setString(4, recordClass.getFullName());
          psInsert.setString(5, dataset.getSummary());
          psInsert.execute();

          return dataset;
        }
        catch (SQLException e) {
        	throw new WdkModelException("Could not insert dataset index.", e);
        }
        finally {
            SqlUtils.closeQuietly(psInsert);
        }
    }

    private void insertDatasetValues(RecordClass recordClass,
            Connection connection, Dataset dataset, List<String[]> values)
            throws SQLException {
        int columnCount = recordClass.getPrimaryKeyAttributeField().getColumnRefs().length;

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(wdkSchema).append(TABLE_DATASET_VALUE);
        sql.append(" (").append(COLUMN_DATASET_ID);
        for (int i = 1; i <= columnCount; i++) {
            sql.append(", ").append(Utilities.COLUMN_PK_PREFIX + i);
        }
        sql.append(") VALUES (?");
        for (int i = 1; i <= columnCount; i++) {
            sql.append(", ?");
        }
        sql.append(")");

        PreparedStatement psInsert = connection.prepareStatement(sql.toString());
        try {
            for (int i = 0; i < values.size(); i++) {
                String[] value = values.get(i);
                psInsert.setInt(1, dataset.getDatasetId());
                for (int j = 0; j < columnCount; j++) {
                    String val = (j < value.length) ? value[j] : null;
                    psInsert.setString(j + 2, val);
                }
                psInsert.addBatch();

                if ((i + 1) % 1000 == 0) psInsert.executeBatch();
            }
            if (values.size() % 1000 != 0) psInsert.executeBatch();
        }
        catch (BatchUpdateException ex) {
            logger.error(ex);
            ex.getNextException().printStackTrace();
            throw ex;
        }
        finally {
            if (psInsert != null) psInsert.close();
        }
    }

    private void insertUserDataset(Connection connection, Dataset dataset)
            throws SQLException, WdkModelException {
        // get new user dataset id
        int userDatasetId = userDb.getPlatform().getNextId(dataSource, userSchema,
                TABLE_USER_DATASET);

        logger.debug("Inserting new user dataset id: " + userDatasetId);
        dataset.setUserDatasetId(userDatasetId);
        dataset.setCreateTime(new Date());

        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(userSchema).append(TABLE_USER_DATASET).append(" (");
        sql.append(COLUMN_USER_DATASET_ID).append(", ");
        sql.append(COLUMN_DATASET_ID).append(", ");
        sql.append(Utilities.COLUMN_USER_ID).append(", ");
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
        }
        finally {
            if (psInsert != null) psInsert.close();
        }
    }

    private void loadDatasetIndex(Connection connection, Dataset dataset)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT * ");
        sql.append(" FROM ").append(wdkSchema).append(TABLE_DATASET_INDEX);
        sql.append(" WHERE ").append(COLUMN_DATASET_ID).append(" = ");
        sql.append(dataset.getDatasetId());

        Statement stmt = null;
        ResultSet resultSet = null;
        try {
          stmt = connection.createStatement();
          resultSet = stmt.executeQuery(sql.toString());
            if (!resultSet.next())
                throw new WdkModelException("The dataset ("
                        + dataset.getDatasetId() + ") does not exist.");
            dataset.setChecksum(resultSet.getString(COLUMN_DATASET_CHECKSUM));
            dataset.setSize(resultSet.getInt(COLUMN_DATASET_SIZE));
            dataset.setSummary(resultSet.getString(COLUMN_SUMMARY));

            String rcName = resultSet.getString(COLUMN_RECORD_CLASS);
            // the recordClass might be determined by the datasetParam later
            if (dataset.getRecordClass() == null) {
                dataset.setRecordClass((RecordClass) wdkModel.resolveReference(rcName));
            }
        }
        finally {
            try {
                if (resultSet != null) SqlUtils.closeResultSetOnly(resultSet);
            }
            finally {
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

        Statement stmt = null;
        ResultSet resultSet = null;
        try {
           stmt = connection.createStatement();
           resultSet = stmt.executeQuery(sql.toString());
            if (!resultSet.next())
                throw new WdkModelException("The userDataset ("
                        + dataset.getUserDatasetId() + ") does not exist.");
            dataset.setCreateTime(resultSet.getTimestamp(COLUMN_CREATE_TIME));
            dataset.setUploadFile(resultSet.getString(COLUMN_UPLOAD_FILE));
        }
        finally {
            try {
                if (resultSet != null) SqlUtils.closeResultSetOnly(resultSet);
            }
            finally {
                if (stmt != null) stmt.close();
            }
        }
    }

    private String getChecksum(List<String[]> values)
            throws NoSuchAlgorithmException, WdkModelException {
        // sort the value list
        Collections.sort(values, new Comparator<String[]>() {
            public int compare(String[] o1, String[] o2) {
                int limit = Math.min(o1.length, o2.length);
                for (int i = 0; i < limit; i++) {
                    if (o1[i] == null || o2[i] == null) break;
                    int result = o1[i].compareTo(o2[i]);
                    if (result != 0) return result;
                }
                return 0;
            }
        });
        JSONArray records = new JSONArray();
        for (String[] value : values) {
            JSONArray record = new JSONArray();
            for (String column : value) {
                record.put(column);
            }
            records.put(record);
        }
        return Utilities.encrypt(records.toString());
    }

    public List<String[]> parseValues(RecordClass recordClass, String strValue)
            throws WdkModelException {
        String[] rows = strValue.split(REGEX_RECORD_DIVIDER);
        List<String[]> records = new ArrayList<String[]>();
        int length = recordClass.getPrimaryKeyAttributeField().getColumnRefs().length;
        for (String row : rows) {
            row = row.trim();
            if (row.length() == 0) continue;

            String[] record = new String[length];
            if (length == 1) { // one column primary key, ignore the divider
              record[0] = row;
            } else { // multi column primary key
              String[] columns = row.split(REGEX_COLUMN_DIVIDER);
              if (columns.length > length)
                throw new WdkDatasetException("The dataset raw value of "
                        + "recordClass '" + recordClass.getFullName()
                        + "' has more columns than expected: '" + row + "'");

              // check if the value is too long, throw an exception if it is.
              for (String column : columns) {
                if (column.length() > MAX_VALUE_LENGTH)
                    throw new WdkModelException("The dataset raw value is too "
                            + " big to be an id for the recordClass "
                            + recordClass.getFullName() + ": " + column);
              }
              System.arraycopy(columns, 0, record, 0, columns.length);
            }
            records.add(record);
        }
        return records;
    }

    public void removeDuplicates(List<String[]> values) {
        Set<String> set = new HashSet<String>();
        for (int i = values.size() - 1; i >= 0; i--) {
            String[] value = values.get(i);
            StringBuilder builder = new StringBuilder();
            for (String val : value) {
                builder.append(val).append(COLUMN_DIVIDER);
            }
            String key = builder.toString();
            if (set.contains(key)) values.remove(i);
            else set.add(key);
        }
    }

    /**
     * The method is used to address out-dated db-link issue with Oracle. The
     * solution is suggested by Oracle support, that: " Since clocks are
     * synchronized at the end of a remote query, precede each remote query with
     * a dummy remote query to the same site (such as select * from
     * dual@remote)."
     * 
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws SQLException
     */
    private void checkRemoteTable() throws WdkModelException {
      try {
        String dblink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
        StringBuilder sql = new StringBuilder("SELECT count(*) FROM ");
        sql.append(wdkSchema).append(TABLE_DATASET_VALUE).append(dblink);

        // execute this dummy sql to make sure the remote table is sync-ed.
        DataSource dataSource = wdkModel.getAppDb().getDataSource();
        SqlUtils.executeScalar(dataSource, sql.toString(),
                "wdk-remote-dataset-dummy");
      }
      catch (SQLException e) {
        throw new WdkModelException(e);
      }
    }
}
