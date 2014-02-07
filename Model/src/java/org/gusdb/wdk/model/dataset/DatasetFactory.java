/**
 * 
 */
package org.gusdb.wdk.model.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;

/**
 * @author xingao
 * 
 */
public class DatasetFactory {

  // all the dataset tables are now in wdk user schema.
  public static final String TABLE_DATASET_VALUES = "dataset_values";
  public static final String TABLE_DATASETS = "datasets";

  // Columns in datasets table
  public static final String COLUMN_DATASET_ID = "dataset_id";
  public static final String COLUMN_DATASET_SIZE = "dataset_size";
  public static final String COLUMN_USER_ID = "user_id";
  public static final String COLUMN_NAME = "dataset_name";
  public static final String COLUMN_PARSER = "parser";
  public static final String COLUMN_CONTENT_CHECKSUM = "content_checksum";
  public static final String COLUMN_CONTENT = "content";
  public static final String COLUMN_CREATED_TIME = "created_time";
  public static final String COLUMN_UPLOAD_FILE = "upload_file";
//  private static final String COLUMN_CATEGORY_ID = "category_id";

  // Columns in the dataset_values table
  private static final String COLUMN_DATASET_VALUE_ID = "dataset_value_id";
  private static final String COLUMN_DATA_PREFIX = "data";

  public static final int MAX_VALUE_COLUMNS = 5;
  public static final int MAX_VALUE_LENGTH = 1000;
  public static final int UPLOAD_FILE_MAX_SIZE = 2000;

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

  public Dataset createOrGetDataset(User user, DatasetParser parser, String content, String uploadFile)
      throws WdkUserException, WdkModelException {
    // parse the content
    List<String[]> values = parser.parse(content);

    // validate values
    validateValues(values);

    // remove duplicates
    removeDuplicates(values);

    return createOrGetDataset(user, content, values, uploadFile, parser.getName());
  }

  private Dataset createOrGetDataset(User user, String content, List<String[]> values, String uploadFile,
      String parserName) throws WdkModelException {
    // truncate upload file if needed
    if (uploadFile != null && uploadFile.length() > UPLOAD_FILE_MAX_SIZE)
      uploadFile = uploadFile.substring(0, UPLOAD_FILE_MAX_SIZE - 3) + "...";

    Connection connection = null;
    try {
      try {
        connection = userDb.getDataSource().getConnection();
        connection.setAutoCommit(false);

        // check if dataset exists
        String checksum = Utilities.encrypt(content);
        Dataset dataset = getDataset(user, connection, checksum);
        if (dataset != null)
          return dataset;

        logger.debug("Creating dataset for user#" + user.getUserId() + ": " + checksum);

        // insert dataset and its values
        Date createdTime = new Date();

        // get a new dataset id
        int datasetId = userDb.getPlatform().getNextId(dataSource, schema, TABLE_DATASETS);
        String name = "My dataset#" + datasetId;
        insertDataset(user, connection, datasetId, name, content, checksum, values.size(), createdTime,
            parserName, uploadFile);
        insertDatasetValues(connection, datasetId, values);
        connection.commit();

        // create and insert user dataset.
        dataset = new Dataset(this, user, datasetId);
        dataset.setName(name);
        dataset.setChecksum(checksum);
        dataset.setCreatedTime(createdTime);
        dataset.setSize(values.size());
        dataset.setParserName(parserName);
        dataset.setUploadFile(uploadFile);

        // refresh through the dblink to make sure the subsequent query doesn't
        // get stale data.
        checkRemoteTable();

        return dataset;
      }
      catch (SQLException | WdkModelException ex) {
        connection.rollback();
        throw new WdkModelException(ex);
      }
      finally {
        if (connection != null) {
          connection.setAutoCommit(true);
          connection.close();
        }
      }
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * Get dataset by datasetId;
   * 
   * @param user
   * @param datasetId
   * @return
   * @throws WdkModelException
   *           if the datasetId doesn't exist or doesn't belong to the given user.
   */
  public Dataset getDataset(User user, int datasetId) throws WdkModelException {
    StringBuilder sql = new StringBuilder("SELECT d.* ");
    sql.append(" FROM " + schema + TABLE_DATASETS + " d ");
    sql.append(" WHERE d." + COLUMN_DATASET_ID + " = " + datasetId);

    DataSource dataSource = userDb.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(), "wdk-dataset-by-dataset-id");

      if (!resultSet.next())
        throw new WdkModelException("Unable to get data set with ID: " + datasetId);

      Dataset dataset = readDataset(user, resultSet);
      return dataset;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: " + datasetId, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public String getContent(int datasetId) throws WdkModelException {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT " + COLUMN_CONTENT);
    sql.append(" FROM " + schema + TABLE_DATASETS);
    sql.append(" WHERE " + COLUMN_DATASET_ID + " = " + datasetId);
    DataSource dataSource = userDb.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql.toString(), "wdk-dataset-content-by-dataset-id");

      if (!resultSet.next())
        throw new WdkModelException("Unable to get data set with ID: " + datasetId);

      DBPlatform platform = userDb.getPlatform();
      String content = platform.getClobData(resultSet, COLUMN_CONTENT);
      return content;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: " + datasetId, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  public String getDatasetValueSql(int datasetId) {
    ModelConfig config = wdkModel.getModelConfig();
    String dbLink = config.getAppDB().getUserDbLink();

    StringBuilder sql = new StringBuilder("SELECT dv.* FROM ");
    sql.append(schema + TABLE_DATASET_VALUES + dbLink + " dv ");
    sql.append(" WHERE dv." + COLUMN_DATASET_ID + " = " + datasetId);
    return sql.toString();
  }

  public List<String[]> getDatasetValues(int datasetId) throws WdkModelException {
    StringBuilder sqlBuffer = new StringBuilder("SELECT * ");
    sqlBuffer.append(" FROM " + schema + TABLE_DATASET_VALUES);
    sqlBuffer.append(" WHERE " + COLUMN_DATASET_ID + " = " + datasetId);
    String sql = sqlBuffer.toString();

    List<String[]> values = new ArrayList<>();
    ResultSet resultSet = null;
    DataSource dataSource = userDb.getDataSource();
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, "wdk-dataset-value-by-dataset-id");
      while (resultSet.next()) {
        String[] row = new String[MAX_VALUE_COLUMNS];
        for (int i = 1; i < MAX_VALUE_COLUMNS; i++) {
          row[i - 1] = resultSet.getString(COLUMN_DATA_PREFIX + i);
        }
        values.add(row);
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
   * This method is called when a dataset set is cloned from one user to another user.
   * 
   * @param dataset
   * @throws WdkModelException
   */
  public Dataset cloneDataset(Dataset dataset, User newUser) throws WdkModelException {
    String content = dataset.getContent();
    List<String[]> values = dataset.getValues();
    String uploadFile = dataset.getUploadFile();
    String parserName = dataset.getParserName();
    return createOrGetDataset(newUser, content, values, uploadFile, parserName);
  }

  private Dataset readDataset(User user, ResultSet resultSet) throws SQLException {
    int datasetId = resultSet.getInt(COLUMN_DATASET_ID);
    Dataset dataset = new Dataset(this, user, datasetId);
    dataset.setName(resultSet.getString(COLUMN_NAME));
    dataset.setChecksum(resultSet.getString(COLUMN_CONTENT_CHECKSUM));
    dataset.setSize(resultSet.getInt(COLUMN_DATASET_SIZE));
    dataset.setParserName(resultSet.getString(COLUMN_PARSER));
    dataset.setCreatedTime(resultSet.getDate(COLUMN_CREATED_TIME));
    dataset.setUploadFile(resultSet.getString(COLUMN_UPLOAD_FILE));
    return dataset;
  }

  private Dataset getDataset(User user, Connection connection, String checksum) throws SQLException {
    StringBuilder sqlBuffer = new StringBuilder("SELECT d.* FROM ");
    sqlBuffer.append(schema + TABLE_DATASETS + " d ");
    sqlBuffer.append(" WHERE d." + COLUMN_CONTENT_CHECKSUM + " = ? ");
    sqlBuffer.append("   AND d." + COLUMN_USER_ID + " = ?");
    String sql = sqlBuffer.toString();

    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      long start = System.currentTimeMillis();
      statement.setString(1, checksum);
      statement.setInt(2, user.getUserId());
      resultSet = statement.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-dataset-by-content-checksum", start);

      Dataset dataset = resultSet.next() ? readDataset(user, resultSet) : null;

      return dataset;
    }
    finally {
      if (resultSet != null)
        resultSet.close();
      if (statement != null)
        statement.close();
    }
  }

  private int insertDataset(User user, Connection connection, int datasetId, String name, String content,
      String checksum, int size, Date createdDate, String parserName, String uploadFile) throws SQLException {
    StringBuilder sqlBuffer = new StringBuilder("INSERT INTO ");
    sqlBuffer.append(schema + TABLE_DATASETS + " (");
    sqlBuffer.append(COLUMN_DATASET_ID + ", " + COLUMN_NAME + ", " + COLUMN_USER_ID + ", " +
        COLUMN_CONTENT_CHECKSUM + ", " + COLUMN_DATASET_SIZE + ", " + COLUMN_CREATED_TIME + ", " +
        COLUMN_PARSER + "," + COLUMN_UPLOAD_FILE + ", " + COLUMN_CONTENT +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    String sql = sqlBuffer.toString();

    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql);
      psInsert.setInt(1, datasetId);
      psInsert.setString(2, name);
      psInsert.setInt(3, user.getUserId());
      psInsert.setString(4, checksum);
      psInsert.setInt(5, size);
      psInsert.setTimestamp(6, new Timestamp(createdDate.getTime()));
      psInsert.setString(7, parserName);
      psInsert.setString(8, uploadFile);
      userDb.getPlatform().setClobData(psInsert, 9, content, false);
      SqlUtils.executePreparedStatement(psInsert, sql, "wdk-dataset-insert-dataset");

      return datasetId;
    }
    finally {
      if (psInsert != null)
        psInsert.close();
    }
  }

  private void insertDatasetValues(Connection connection, int datasetId, List<String[]> data)
      throws SQLException {
    int length = data.get(0).length;
    StringBuilder sql = new StringBuilder("INSERT INTO ");
    sql.append(schema + TABLE_DATASET_VALUES);
    sql.append(" (" + COLUMN_DATASET_VALUE_ID + ", " + COLUMN_DATASET_ID);
    for (int i = 1; i <= length; i++) {
      sql.append(", " + COLUMN_DATA_PREFIX + i);
    }
    sql.append(") VALUES (?, ?");
    for (int i = 1; i <= length; i++) {
      sql.append(", ?");
    }
    sql.append(")");

    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql.toString());
      for (int i = 0; i < data.size(); i++) {
        String[] value = data.get(i);

        // get a new value id.
        int datasetValueId = userDb.getPlatform().getNextId(dataSource, schema, TABLE_DATASET_VALUES);

        psInsert.setInt(1, datasetValueId);
        psInsert.setInt(2, datasetId);
        for (int j = 0; j < length; j++) {
          psInsert.setString(j + 3, value[j]);
        }
        psInsert.addBatch();

        if ((i + 1) % 1000 == 0)
          psInsert.executeBatch();
      }
      if (data.size() % 1000 != 0)
        psInsert.executeBatch();
    }
    finally {
      if (psInsert != null)
        psInsert.close();
    }
  }

  private void validateValues(List<String[]> values) throws WdkUserException {
    for (int i = values.size() - 1; i >= 0; i--) {
      String[] row = values.get(i);
      // check the number of columns
      if (row.length > MAX_VALUE_COLUMNS) {
        JSONArray jsArray = new JSONArray(Arrays.asList(row));
        throw new WdkUserException("The maximum allowed columns in datasets " + "are " + MAX_VALUE_COLUMNS +
            ", but the input has more: " + jsArray.toString());
      }
      // check the length of each value
      for (String value : row) {
        if (value != null && value.length() > MAX_VALUE_LENGTH)
          throw new WdkUserException("The maximum allowed length for a " + "single value in datasets are " +
              MAX_VALUE_LENGTH + ", but the input is: " + value);
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
      if (set.contains(key))
        values.remove(i);
      else
        set.add(key);
    }
  }

  /**
   * The method is used to address out-dated db-link issue with Oracle. The solution is suggested by Oracle
   * support, that: " Since clocks are synchronized at the end of a remote query, precede each remote query
   * with a dummy remote query to the same site (such as select * from dual@remote)."
   */
  private void checkRemoteTable() throws SQLException {
    String dblink = wdkModel.getModelConfig().getAppDB().getUserDbLink();
    String table = schema + TABLE_DATASETS + dblink;
    StringBuilder sql = new StringBuilder("SELECT count(*) FROM " + table);

    // execute this dummy sql to make sure the remote table is sync-ed.
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    SqlUtils.executeScalar(dataSource, sql.toString(), "wdk-remote-dataset-dummy");
  }
}
