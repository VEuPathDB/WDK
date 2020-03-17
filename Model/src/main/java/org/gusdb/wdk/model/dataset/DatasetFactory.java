package org.gusdb.wdk.model.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;

/**
 * @author xingao
 */
public class DatasetFactory {

  private static final Logger LOG = Logger.getLogger(DatasetFactory.class);

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

  // Columns in the dataset_values table
  private static final String COLUMN_DATASET_VALUE_ID = "dataset_value_id";
  private static final String COLUMN_DATA_PREFIX = "data";

  public static final int MAX_VALUE_COLUMNS = 20;
  public static final int MAX_VALUE_LENGTH = 1000;
  public static final int UPLOAD_FILE_MAX_SIZE = 2000;

  private final WdkModel _wdkModel;
  private final DatabaseInstance _userDb;
  private final String _userSchema;

  public DatasetFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    _userDb = wdkModel.getUserDb();
    _userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  public void saveDatasetMetadata(Dataset dataset) throws WdkModelException {
    try {
      String sql =
          "update " + _userSchema + TABLE_DATASETS +
          " set " + COLUMN_NAME + " = ?" +
          " where " + COLUMN_DATASET_ID + " = ?";
      new SQLRunner(_wdkModel.getUserDb().getDataSource(), sql, "update-dataset-name")
          .executeUpdate(new Object[]{ dataset.getName(), dataset.getDatasetId() });
    }
    catch (SQLRunnerException e) {
      WdkModelException.unwrap(e);
    }
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
        connection = _userDb.getDataSource().getConnection();
        connection.setAutoCommit(false);

        // check if dataset exists
        String checksum = EncryptionUtil.encrypt(content);
        Dataset dataset = getDataset(user, connection, checksum);
        if (dataset != null)
          return dataset;

        LOG.debug("Creating dataset for user#" + user.getUserId() + ": " + checksum);

        // insert dataset and its values
        Date createdTime = new Date();

        // get a new dataset id
        long datasetId = _userDb.getPlatform().getNextId(_userDb.getDataSource(), _userSchema, TABLE_DATASETS);
        String name = "My dataset#" + datasetId;
        insertDataset(user, connection, datasetId, name, content, checksum, values.size(), createdTime,
            parserName, uploadFile);
        insertDatasetValues(connection, datasetId, values);
        connection.commit();

        // create and insert user dataset.
        dataset = new Dataset(this, user.getUserId(), datasetId);
        dataset.setName(name);
        dataset.setChecksum(checksum);
        dataset.setCreatedTime(createdTime);
        dataset.setSize(values.size());
        dataset.setParserName(parserName);
        dataset.setUploadFile(uploadFile);

        // refresh through the dblink to make sure the subsequent query doesn't get stale data
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
   * @param datasetId ID of the requested dataset
   * @param ownerId ID of the requested owner of the dataset
   * @return dataset with the passed ID if owned by the user with ownerId
   * @throws WdkUserException if no dataset exists with the passed ID or exists but is not owned by the user with ownerId
   * @throws WdkModelException if error occurs while looking up dataset
   *           if the datasetId doesn't exist or doesn't belong to the given user.
   */
  public Dataset getDatasetWithOwner(long datasetId, long ownerId) throws WdkUserException, WdkModelException {
    StringBuilder sql = new StringBuilder("SELECT d.* ");
    sql.append(" FROM ")
      .append(_userSchema)
      .append(TABLE_DATASETS)
      .append(" d ")
      .append(" WHERE d." + COLUMN_DATASET_ID + " = ")
      .append(datasetId);

    DataSource userDs = _userDb.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(userDs, sql.toString(), "wdk-dataset-by-dataset-id");

      if (!resultSet.next())
        throw new WdkUserException("Unable to get data set with ID: " + datasetId);

      Dataset dataset = readDataset(resultSet);
      if (dataset.getOwnerId() != ownerId) {
        throw new WdkUserException("Dataset with ID " + datasetId + " does not belong to user " + ownerId);
      }

      return dataset;
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: " + datasetId, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  public String getDatasetContent(long datasetId) throws WdkModelException {
    StringBuilder sql = new StringBuilder("SELECT " + COLUMN_CONTENT)
      .append(" FROM ").append(_userSchema).append(TABLE_DATASETS)
      .append(" WHERE " + COLUMN_DATASET_ID + " = ").append(datasetId);
    DataSource userDs = _userDb.getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(userDs, sql.toString(), "wdk-dataset-content-by-dataset-id");

      if (!resultSet.next())
        throw new WdkModelException("Unable to get data set with ID: " + datasetId);

      DBPlatform platform = _userDb.getPlatform();
      return platform.getClobData(resultSet, COLUMN_CONTENT);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to get data set with ID: " + datasetId, e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  public String getDatasetValueSqlForAppDb(long datasetId) {
    var dbLink = _wdkModel.getModelConfig().getAppDB().getUserDbLink();
    return "SELECT dv.* FROM " + _userSchema + TABLE_DATASET_VALUES + dbLink
      + " dv " + " WHERE dv." + COLUMN_DATASET_ID + " = " + datasetId;
  }

  public List<String[]> getDatasetValues(long datasetId) throws WdkModelException {
    var sql = "SELECT * FROM " + _userSchema + TABLE_DATASET_VALUES
      + " WHERE " + COLUMN_DATASET_ID + " = " + datasetId;

    List<String[]> values = new ArrayList<>();
    ResultSet resultSet = null;
    DataSource userDs = _userDb.getDataSource();
    try {
      resultSet = SqlUtils.executeQuery(userDs, sql, "wdk-dataset-value-by-dataset-id");
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
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  /**
   * This method is called when a dataset set is cloned from one user to another
   * user.
   */
  public Dataset cloneDataset(Dataset dataset, User newUser) throws WdkModelException {
    String content = dataset.getContent();
    List<String[]> values = dataset.getValues();
    String uploadFile = dataset.getUploadFile();
    String parserName = dataset.getParserName();
    return createOrGetDataset(newUser, content, values, uploadFile, parserName);
  }

  private Dataset readDataset(ResultSet resultSet) throws SQLException {
    long datasetId = resultSet.getLong(COLUMN_DATASET_ID);
    long ownerId = resultSet.getLong(COLUMN_USER_ID);
    Dataset dataset = new Dataset(this, ownerId, datasetId);
    dataset.setName(resultSet.getString(COLUMN_NAME));
    dataset.setChecksum(resultSet.getString(COLUMN_CONTENT_CHECKSUM));
    dataset.setSize(resultSet.getInt(COLUMN_DATASET_SIZE));
    dataset.setParserName(resultSet.getString(COLUMN_PARSER));
    dataset.setCreatedTime(resultSet.getDate(COLUMN_CREATED_TIME));
    dataset.setUploadFile(resultSet.getString(COLUMN_UPLOAD_FILE));
    return dataset;
  }

  private Dataset getDataset(User user, Connection connection, String checksum) throws WdkModelException  {
    String sql =
        "SELECT d.*" +
        " FROM " + _userSchema + TABLE_DATASETS + " d" +
        " WHERE d." + COLUMN_CONTENT_CHECKSUM + " = ?" +
        "   AND d." + COLUMN_USER_ID + " = ?";

    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.prepareStatement(sql);
      long start = System.currentTimeMillis();
      statement.setString(1, checksum);
      statement.setLong(2, user.getUserId());
      resultSet = statement.executeQuery();
      QueryLogger.logEndStatementExecution(sql, "wdk-dataset-by-content-checksum", start);

      return resultSet.next() ? readDataset(resultSet) : null;
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      try {
      if (resultSet != null)
        resultSet.close();
      if (statement != null)
        statement.close();
      } catch(SQLException ex) {
        throw new WdkModelException(ex);
      }
    }
  }

  private long insertDataset(User user, Connection connection, long datasetId, String name, String content,
      String checksum, int size, Date createdDate, String parserName, String uploadFile) throws WdkModelException {
    var sql = "INSERT INTO " + _userSchema + TABLE_DATASETS + " ("
      + COLUMN_DATASET_ID+ ", " + COLUMN_NAME + ", " + COLUMN_USER_ID + ", "
      + COLUMN_CONTENT_CHECKSUM + ", " + COLUMN_DATASET_SIZE + ", "
      + COLUMN_CREATED_TIME + ", " + COLUMN_PARSER + ", "
      + COLUMN_UPLOAD_FILE + ", " + COLUMN_CONTENT
      + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    PreparedStatement psInsert = null;
    try {
      psInsert = connection.prepareStatement(sql);
      psInsert.setLong(1, datasetId);
      psInsert.setString(2, name);
      psInsert.setLong(3, user.getUserId());
      psInsert.setString(4, checksum);
      psInsert.setInt(5, size);
      psInsert.setTimestamp(6, new Timestamp(createdDate.getTime()));
      psInsert.setString(7, parserName);
      psInsert.setString(8, uploadFile);
      _userDb.getPlatform().setClobData(psInsert, 9, content, false);
      SqlUtils.executePreparedStatement(psInsert, sql, "wdk-dataset-insert-dataset");

      return datasetId;
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
    finally {
      if (psInsert != null)
        try {
          psInsert.close();
        }
        catch (SQLException ex) {
          throw new WdkModelException(ex);
        }
    }
  }

  private void insertDatasetValues(Connection connection, long datasetId, List<String[]> data)
      throws SQLException {
    int length = data.get(0).length;
    StringBuilder sql = new StringBuilder("INSERT INTO ");

    sql.append(_userSchema)
      .append(TABLE_DATASET_VALUES)
      .append(" (" + COLUMN_DATASET_VALUE_ID + ", " + COLUMN_DATASET_ID);

    for (int i = 1; i <= length; i++) {
      sql.append(", " + COLUMN_DATA_PREFIX).append(i);
    }

    sql.append(") VALUES (?, ?")
      .append(", ?".repeat(length))
      .append(")");

    try (PreparedStatement psInsert = connection.prepareStatement(sql.toString())) {
      for (int i = 0; i < data.size(); i++) {
        String[] value = data.get(i);

        // get a new value id.
        long datasetValueId = _userDb.getPlatform()
          .getNextId(_userDb.getDataSource(), _userSchema,
            TABLE_DATASET_VALUES);

        psInsert.setLong(1, datasetValueId);
        psInsert.setLong(2, datasetId);
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
   */
  private void removeDuplicates(List<String[]> values) {
    Set<String> set = new HashSet<>();
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
    String dblink = _wdkModel.getModelConfig().getAppDB().getUserDbLink();
    String table = _userSchema + TABLE_DATASETS + dblink;

    // execute this dummy sql to make sure the remote table is sync-ed.
    SqlUtils.executeScalar(_wdkModel.getAppDb().getDataSource(),
      "SELECT count(*) FROM " + table, "wdk-remote-dataset-dummy");
  }

  public void transferDatasetOwnership(User oldUser, User newUser) throws WdkModelException {
    String sql =
      "UPDATE " + _userSchema + TABLE_DATASETS +
      " SET " + COLUMN_USER_ID + " = ?" +
      " WHERE " + COLUMN_USER_ID + " = ?";
    try {
      new SQLRunner(_wdkModel.getUserDb().getDataSource(), sql, "update-dataset-owner").executeUpdate(
        new Object[] { newUser.getUserId(), oldUser.getUserId() },
        new Integer[] { Types.BIGINT, Types.BIGINT }
      );
    }
    catch (Exception e) {
      WdkModelException.unwrap(e);
    }
  }
}
