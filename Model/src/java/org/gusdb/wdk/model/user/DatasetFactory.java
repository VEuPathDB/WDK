/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.json.JSONArray;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

  // all the dataset tables are now in wdk user schema.
  private static final String TABLE_DATASET_VALUES = "dataset_values";
  private static final String TABLE_DATASETS = "datasets";
  private static final String TABLE_USER_DATASETS = "user_datasets";

  // Columns in datasets table
  private static final String COLUMN_DATASET_ID = "dataset_id";
  private static final String COLUMN_DATASET_CHECKSUM = "dataset_checksum";
  private static final String COLUMN_DATASET_SIZE = "dataset_size";

  // Columns in the dataset_values table
  private static final String COLUMN_DATASET_VALUE_ID = "dataset_value_id";
  private static final String COLUMN_DATA_PREFIX = "data";

  // Columns in user_datasets table;
  private static final String COLUMN_USER_DATASET_ID = "user_dataset_id";
  private static final String COLUMN_USER_ID = "user_id";
  private static final String COLUMN_CONTENT_TYPE = "content_type";
  private static final String COLUMN_CONTENT_CHECKSUM = "content_checksum";
  private static final String COLUMN_ORIGINAL_CONTENT = "original_content";
  private static final String COLUMN_CREATE_TIME = "create_time";
  private static final String COLUMN_UPLOAD_FILE = "upload_file";

  private static final String REGEX_COLUMN_DIVIDER = "[\\|]+";
  private static final String REGEX_RECORD_DIVIDER = "[,\\s;]+";

  public static final int MAX_VALUE_COLUMNS = 5;
  private static final int MAX_VALUE_LENGTH = 1000;

  private static Logger logger = Logger.getLogger(DatasetFactory.class);

  private WdkModel wdkModel;
  private DatabaseInstance userDb;
  private DataSource dataSource;
  private String schema;

  public DatasetFactory(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    this.userDb = this.wdkModel.getUserDb();
    this.dataSource = userDb.getDataSource();

    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    this.schema = userDB.getUserSchema();
  }

  public Dataset createOrGetDataset(User user, List<String[]> data,
      String originalContent, String contentType, String uploadFile)
      throws WdkModelException, WdkUserException {
    // validate values
    validateValues(data);

    // remove duplicates
    removeDuplicates(data);

    try {
      Connection connection = null;
      try {
        connection = userDb.getDataSource().getConnection();
        connection.setAutoCommit(false);

        // check if the user dataset exists
        String contentChecksum = Utilities.encrypt(originalContent);
        Dataset dataset = getDataset(user, connection, contentChecksum);
        if (dataset != null) return dataset;

        // user dataset doesn't exist, check if dataset exists
        String datasetChecksum = makeDatasetChecksum(data);
        Integer datasetId = getDatasetId(connection, datasetChecksum);
        if (datasetId == null) { // dataset doesn't exist create dataset
          datasetId = insertDataset(connection, datasetChecksum, data.size());
          insertDatasetValues(connection, datasetId, data);
        }

        // create and insert user dataset.
        dataset = new Dataset(this, user, datasetId);
        dataset.setContentChecksum(contentChecksum);
        dataset.setCreateTime(new Date());
        dataset.setDatasetChecksum(datasetChecksum);
        dataset.setSize(data.size());
        dataset.setContentType(contentType);
        dataset.setUploadFile(uploadFile);

        insertUserDataset(connection, dataset, originalContent);

        // refresh through the dblink to make sure the subsequent query doesn't
        // get stale data.
        checkRemoteTable();

        return dataset;
      } catch (SQLException e) {
        if (connection != null) connection.rollback();
        throw e;
      } finally {
        if (connection != null) {
          connection.setAutoCommit(true);
          connection.close();
        }
      }
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * Get dataset by userDatasetId;
   * 
   * @param user
   * @param userDatasetId
   * @return
   * @throws WdkModelException
   *           if the userDatasetId doesn't exist or doesn't belong to the given
   *           user.
   */
  public Dataset getDataset(User user, int userDatasetId)
      throws WdkModelException {
    StringBuilder sql = new StringBuilder("SELECT ud.*, ");
    sql.append(" d." + COLUMN_DATASET_CHECKSUM + ", d." + COLUMN_DATASET_SIZE);
    sql.append(" FROM " + schema + TABLE_USER_DATASETS + " ud, ");
    sql.append(schema + TABLE_DATASETS + " d ");
    sql.append(" WHERE ud." + COLUMN_DATASET_ID + " = d." + COLUMN_DATASET_ID);
    sql.append("   AND ud." + COLUMN_USER_DATASET_ID + " = " + userDatasetId);

    DataSource dataSource = userDb.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(),
          "wdk-dataset-by-user-dataset-id");

      if (!resultSet.next())
        throw new WdkModelException("Unable to get data set with ID: "
            + userDatasetId);

      Dataset dataset = readDataset(user, resultSet);
      return dataset;
    } catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: "
          + userDatasetId, e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public String getOriginalContent(int userDatasetId) throws WdkModelException {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT " + COLUMN_ORIGINAL_CONTENT);
    sql.append(" FROM " + schema + TABLE_USER_DATASETS);
    sql.append(" WHERE " + COLUMN_USER_DATASET_ID + " = " + userDatasetId);
    DataSource dataSource = userDb.getDataSource();
    ResultSet resultSet = null;
    try {

      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(),
          "wdk-dataset-content-by-user-dataset-id");

      if (!resultSet.next())
        throw new WdkModelException("Unable to get data set with ID: "
            + userDatasetId);

      DBPlatform platform = userDb.getPlatform();
      String content = platform.getClobData(resultSet, COLUMN_ORIGINAL_CONTENT);
      return content;
    } catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: "
          + userDatasetId, e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public String getDatasetValueSql(int userDatasetId) {
    ModelConfig config = wdkModel.getModelConfig();
    String dbLink = config.getAppDB().getUserDbLink();
    String dvTable = schema + DatasetFactory.TABLE_DATASET_VALUES + dbLink;
    String udTable = schema + DatasetFactory.TABLE_USER_DATASETS + dbLink;

    StringBuilder sql = new StringBuilder("SELECT dv.* FROM ");
    sql.append(udTable + " ud, " + dvTable + " dv ");
    sql.append(" WHERE dv." + COLUMN_DATASET_ID + " = ud." + COLUMN_DATASET_ID);
    sql.append(" AND ud." + COLUMN_USER_DATASET_ID + " = " + userDatasetId);
    return sql.toString();
  }

  public List<String[]> getDatasetValues(int datasetId)
      throws WdkModelException {
    StringBuilder sqlBuffer = new StringBuilder("SELECT * ");
    sqlBuffer.append(" FROM " + schema + TABLE_DATASET_VALUES);
    sqlBuffer.append(" WHERE " + COLUMN_DATASET_ID + " = " + datasetId);
    String sql = sqlBuffer.toString();

    List<String[]> values = new ArrayList<>();
    ResultSet resultSet = null;
    DataSource dataSource = userDb.getDataSource();
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
          "wdk-dataset-value-by-dataset-id");
      while (resultSet.next()) {
        String[] row = new String[MAX_VALUE_COLUMNS];
        for (int i = 1; i < MAX_VALUE_COLUMNS; i++) {
          row[i - 1] = resultSet.getString(COLUMN_DATA_PREFIX + i);
        }
        values.add(row);
      }
      return values;
    } catch (SQLException e) {
      throw new WdkModelException("Could not retrieve dataset values.", e);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  /**
   * This method is called when a dataset set is cloned from one user to another
   * user.
   * 
   * @param dataset
   * @throws WdkModelException
   */
  public Dataset cloneDataset(Dataset dataset, User newUser)
      throws WdkModelException {

    try {
      Connection connection = null;
      try {
        connection = userDb.getDataSource().getConnection();
        connection.setAutoCommit(false);

        // check if new user already has a dataset of the same content checksum
        Dataset newDataset = getDataset(newUser, connection,
            dataset.getContentChecksum());
        if (newDataset != null) return newDataset;

        // user dataset doesn't exist, will create a new user dataset, but reuse
        // other information.
        newDataset = new Dataset(this, newUser, dataset.getDatasetId());
        newDataset.setContentType(dataset.getContentType());
        newDataset.setSize(dataset.getSize());
        newDataset.setDatasetChecksum(dataset.getDatasetChecksum());
        newDataset.setContentChecksum(dataset.getContentChecksum());
        newDataset.setCreateTime(new Date());

        // get the original content
        String originalContent = getOriginalContent(dataset.getUserDatasetId());

        insertUserDataset(connection, newDataset, originalContent);
        return newDataset;
      } catch (SQLException e) {
        if (connection != null) connection.rollback();
        throw e;
      } finally {
        if (connection != null) {
          connection.setAutoCommit(true);
          connection.close();
        }
      }
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    }

  }

  private Dataset readDataset(User user, ResultSet resultSet)
      throws SQLException {
    int datasetId = resultSet.getInt(COLUMN_DATASET_ID);
    Dataset dataset = new Dataset(this, user, datasetId);
    dataset.setUserDatasetId(resultSet.getInt(COLUMN_USER_DATASET_ID));
    dataset.setDatasetChecksum(resultSet.getString(COLUMN_DATASET_CHECKSUM));
    dataset.setSize(resultSet.getInt(COLUMN_DATASET_SIZE));
    dataset.setContentChecksum(resultSet.getString(COLUMN_CONTENT_CHECKSUM));
    dataset.setContentType(resultSet.getString(COLUMN_CONTENT_TYPE));
    dataset.setCreateTime(resultSet.getDate(COLUMN_CREATE_TIME));
    dataset.setUploadFile(resultSet.getString(COLUMN_UPLOAD_FILE));
    return dataset;
  }

  private Dataset getDataset(User user, Connection connection,
      String contentChecksum) throws SQLException {
    StringBuilder sqlBuffer = new StringBuilder("SELECT ud.*, ");
    sqlBuffer.append(" d." + COLUMN_DATASET_CHECKSUM + ", d."
        + COLUMN_DATASET_SIZE);
    sqlBuffer.append(" FROM " + schema + TABLE_USER_DATASETS + " ud, ");
    sqlBuffer.append(schema + TABLE_DATASETS + " d ");
    sqlBuffer.append(" WHERE ud." + COLUMN_DATASET_ID + " = d."
        + COLUMN_DATASET_ID);
    sqlBuffer.append("   AND ud." + COLUMN_CONTENT_CHECKSUM + " = ?");
    String sql = sqlBuffer.toString();

    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      long start = System.currentTimeMillis();
      statement.setString(1, contentChecksum);
      resultSet = statement.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-dataset-by-content-checksum", start);

      Dataset dataset = resultSet.next() ? readDataset(user, resultSet) : null;
      return dataset;
    } finally {
      if (resultSet != null) resultSet.close();
      if (statement != null) statement.close();
    }
  }

  private Integer getDatasetId(Connection connection, String datasetChecksum)
      throws SQLException {
    StringBuilder sqlBuffer = new StringBuilder("SELECT " + COLUMN_DATASET_ID);
    sqlBuffer.append(" FROM " + schema + TABLE_DATASETS);
    sqlBuffer.append(" WHERE " + COLUMN_DATASET_CHECKSUM + " = ?");
    String sql = sqlBuffer.toString();

    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      long start = System.currentTimeMillis();
      statement.setString(1, datasetChecksum);
      resultSet = statement.executeQuery();
      QueryLogger.logEndStatementExecution(sql,
          "wdk-dataset-id-by-dataset-checksum", start);
      Integer datasetId = null;
      if (resultSet.next()) resultSet.getInt(COLUMN_DATASET_ID);
      return datasetId;
    } finally {
      if (statement != null) statement.close();
      if (resultSet != null) resultSet.close();
    }
  }

  private int insertDataset(Connection connection, String datasetChecksum,
      int size) throws SQLException {
    StringBuilder sqlBuffer = new StringBuilder("INSERT INTO ");
    sqlBuffer.append(schema + TABLE_DATASETS + " (");
    sqlBuffer.append(COLUMN_DATASET_ID + ", " + COLUMN_DATASET_CHECKSUM + ", ");
    sqlBuffer.append(COLUMN_DATASET_SIZE + ", " + ") VALUES (?, ?, ?)");
    String sql = sqlBuffer.toString();

    // get a new dataset id
    int datasetId = userDb.getPlatform().getNextId(dataSource, schema,
        TABLE_DATASETS);

    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql);
      psInsert.setInt(1, datasetId);
      psInsert.setString(2, datasetChecksum);
      psInsert.setInt(3, size);
      SqlUtils.executePreparedStatement(psInsert, sql,
          "wdk-dataset-insert-dataset");

      return datasetId;
    } finally {
      if (psInsert != null) psInsert.close();
    }
  }

  private void insertDatasetValues(Connection connection, int datasetId,
      List<String[]> data) throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(schema + TABLE_DATASET_VALUES);
    sql.append(" (" + COLUMN_DATASET_VALUE_ID + ", " + COLUMN_DATASET_ID);
    for (int i = 1; i <= MAX_VALUE_COLUMNS; i++) {
      sql.append(", " + COLUMN_DATA_PREFIX + i);
    }
    sql.append(") VALUES (?, ?");
    for (int i = 1; i <= MAX_VALUE_COLUMNS; i++) {
      sql.append(", ?");
    }
    sql.append(")");

    // get a new dataset id
    int datasetValueId = userDb.getPlatform().getNextId(dataSource, schema,
        TABLE_DATASET_VALUES);

    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql.toString());
      for (int i = 0; i < data.size(); i++) {
        String[] value = data.get(i);
        psInsert.setInt(1, datasetValueId);
        psInsert.setInt(2, datasetId);
        for (int j = 0; j < value.length; j++) {
          psInsert.setString(j + 3, value[j]);
        }
        psInsert.addBatch();

        if ((i + 1) % 1000 == 0) psInsert.executeBatch();
      }
      if (data.size() % 1000 != 0) psInsert.executeBatch();
    } finally {
      if (psInsert != null) psInsert.close();
    }
  }

  private void insertUserDataset(Connection connection, Dataset dataset,
      String originalContent) throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(schema + TABLE_USER_DATASETS + " (");
    sql.append(COLUMN_USER_DATASET_ID + ", " + COLUMN_DATASET_ID + ", ");
    sql.append(COLUMN_USER_ID + ", " + COLUMN_CREATE_TIME + ", ");
    sql.append(COLUMN_UPLOAD_FILE + ", " + COLUMN_CONTENT_CHECKSUM + ", ");
    sql.append(COLUMN_CONTENT_TYPE + ", " + COLUMN_ORIGINAL_CONTENT);
    sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

    // get new user dataset id
    int userDatasetId = userDb.getPlatform().getNextId(dataSource, schema,
        TABLE_USER_DATASETS);
    dataset.setUserDatasetId(userDatasetId);

    logger.debug("Inserting new user dataset id: " + userDatasetId);

    DBPlatform platform = userDb.getPlatform();
    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql.toString());
      psInsert.setInt(1, userDatasetId);
      psInsert.setInt(2, dataset.getDatasetId());
      psInsert.setInt(3, dataset.getUser().getUserId());
      psInsert.setTimestamp(4, new Timestamp(dataset.getCreateTime().getTime()));
      psInsert.setString(5, dataset.getUploadFile());
      psInsert.setString(6, dataset.getContentChecksum());
      psInsert.setString(7, dataset.getContentType());
      platform.setClobData(psInsert, 8, originalContent, false);
      psInsert.executeUpdate();
    } finally {
      if (psInsert != null) psInsert.close();
    }
  }

  private String makeDatasetChecksum(List<String[]> values)
      throws WdkModelException {
    // sort the value list
    Collections.sort(values, new Comparator<String[]>() {
      @Override
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
      JSONArray record = new JSONArray(Arrays.asList(value));
      records.put(record);
    }
    return Utilities.encrypt(records.toString());
  }

  private void validateValues(List<String[]> values) throws WdkUserException {
    for (int i = values.size() - 1; i >= 0; i--) {
      String[] row = values.get(i);
      // check the number of columns
      if (row.length > MAX_VALUE_COLUMNS) {
        JSONArray jsArray = new JSONArray(Arrays.asList(row));
        throw new WdkUserException("The maximum allowed columns in datasets "
            + "are " + MAX_VALUE_COLUMNS + ", but the input has more: "
            + jsArray.toString());
      }
      // check the length of each value
      for (String value : row) {
        if (value != null && value.length() > MAX_VALUE_LENGTH)
          throw new WdkUserException("The maximum allowed length for a "
              + "single value in datasets are " + MAX_VALUE_LENGTH
              + ", but the input is: " + value);
      }
    }
  }

  /**
   * remove the duplicates from the list.
   * 
   * @param values
   * @throws WdkUserException
   */
  private void removeDuplicates(List<String[]> values) {
    Set<String> set = new HashSet<String>();
    // starting from end so that when we remove an item, it won't affect the
    // index.
    for (int i = values.size() - 1; i >= 0; i--) {
      String[] value = values.get(i);
      JSONArray jsArray = new JSONArray(Arrays.asList(value));
      String key = jsArray.toString();
      if (set.contains(key)) values.remove(i);
      else set.add(key);
    }
  }

  /**
   * The method is used to address out-dated db-link issue with Oracle. The
   * solution is suggested by Oracle support, that: " Since clocks are
   * synchronized at the end of a remote query, precede each remote query with a
   * dummy remote query to the same site (such as select * from dual@remote)."
   */
  private void checkRemoteTable() throws WdkModelException {
    try {
      String dblink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
      String table = schema + TABLE_USER_DATASETS + dblink;
      StringBuilder sql = new StringBuilder("SELECT count(*) FROM " + table);

      // execute this dummy sql to make sure the remote table is sync-ed.
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      SqlUtils.executeScalar(dataSource, sql.toString(),
          "wdk-remote-dataset-dummy");
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }
  }
}
