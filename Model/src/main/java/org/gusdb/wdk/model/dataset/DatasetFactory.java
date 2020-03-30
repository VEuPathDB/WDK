package org.gusdb.wdk.model.dataset;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.DatasetParser.DatasetIterator;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.*;

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

  //--------------------------------------------------------
  //
  // Public API
  //
  //--------------------------------------------------------

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

  public Dataset createOrGetDataset(
    final User user,
    final DatasetParser parser,
    final DatasetContents content
  ) throws WdkUserException, WdkModelException {
    var it = parser.iterator(content);

    while (it.hasNext()) {
      var tmp = it.next();
      validateValue(tmp);
    }

    return createOrGetDataset(user, content, parser);
  }

  /**
   * Get dataset by datasetId;
   *
   * @param datasetId ID of the requested dataset
   * @param ownerId ID of the requested owner of the dataset
   *
   * @return dataset with the passed ID if owned by the user with ownerId
   *
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

      var dataset = readDataset(resultSet);
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

  public DatasetContents getDatasetContent(long datasetId) {
    var sql = "SELECT " + COLUMN_CONTENT + ", " + COLUMN_DATASET_SIZE + ","
      + COLUMN_UPLOAD_FILE
      + " FROM " + _userSchema + TABLE_DATASETS
      + " WHERE " + COLUMN_DATASET_ID + " = ?";

    return new SQLRunner(_userDb.getDataSource(), sql)
      .executeQuery(
        new Object[] {datasetId},
        new Integer[] {Types.BIGINT},
        rs -> {
          if (!rs.next())
            throw new WdkRuntimeException("Unable to get data set with ID: " + datasetId);
          return rs.getInt(COLUMN_DATASET_SIZE) > 1500
            ? getDatasetContentAsFile(rs)
            : getDatasetContentAsString(rs);
        });
  }

  public String getDatasetValueSqlForAppDb(long datasetId) {
    var dbLink = _wdkModel.getModelConfig().getAppDB().getUserDbLink();
    return "SELECT dv.* FROM " + _userSchema + TABLE_DATASET_VALUES + dbLink
      + " dv " + " WHERE dv." + COLUMN_DATASET_ID + " = " + datasetId;
  }

  public List<String[]> getDatasetValues(long datasetId) {
    var userDs = _userDb.getDataSource();
    var sql    = "SELECT * FROM " + _userSchema + TABLE_DATASET_VALUES
      + " WHERE " + COLUMN_DATASET_ID + " = ?";

    return new SQLRunner(userDs, sql)
      .executeQuery(
        new Object[] {datasetId},
        new Integer[] {Types.BIGINT},
        rs -> {
          var values = new ArrayList<String[]>();
          while (rs.next()) {
            String[] row = new String[MAX_VALUE_COLUMNS];
            for (int i = 1; i < MAX_VALUE_COLUMNS; i++) {
              row[i - 1] = rs.getString(COLUMN_DATA_PREFIX + i);
            }
            values.add(row);
          }
          return values;
        });
  }

  /**
   * This method is called when a dataset set is cloned from one user to another
   * user.
   */
  public Dataset cloneDataset(long oldDsId, long oldUserId, User newUser)
  throws WdkModelException {
    try (final var con = _userDb.getDataSource().getConnection()) {
      con.setAutoCommit(false);

      var newId = copyDataset(con, oldDsId, oldUserId, newUser.getUserId());
      copyDatasetValues(con, oldDsId, newId);

      con.commit();

      return getDatasetWithOwner(newId, newUser.getUserId());
    } catch (SQLException | WdkUserException e) {
      throw new WdkModelException(e);
    }
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

  //--------------------------------------------------------
  //
  // Internal Methods
  //
  //--------------------------------------------------------

  private Dataset createOrGetDataset(
    final User user,
    final DatasetContents content,
    final DatasetParser parser
  ) throws WdkModelException, WdkUserException {
    var uploadFile = content.getUploadFileName();

    // truncate upload file if needed
    if (uploadFile != null && uploadFile.length() > UPLOAD_FILE_MAX_SIZE)
      uploadFile = uploadFile.substring(0, UPLOAD_FILE_MAX_SIZE - 3) + "...";

    Connection connection = null;
    try {
      try {
        connection = _userDb.getDataSource().getConnection();
        connection.setAutoCommit(false);

        // check if dataset exists
        String checksum = content.getChecksum();
        Dataset dataset = getDataset(user, connection, checksum);
        if (dataset != null)
          return dataset;

        LOG.debug("Creating dataset for user#" + user.getUserId() + ": " + checksum);

        // insert dataset and its values
        Date createdTime = new Date();

        // get a new dataset id
        var datasetId = _userDb.getPlatform()
          .getNextId(_userDb.getDataSource(), _userSchema, TABLE_DATASETS);
        var name = "My dataset#" + datasetId;
        var size = parser.datasetContentSize(content);

        insertDataset(
          user,
          connection,
          datasetId,
          name,
          content,
          size,
          createdTime,
          parser.getName(),
          uploadFile
        );

        insertDatasetValues(connection, datasetId, parser.iterator(content),
          parser.datasetContentWidth(content));
        connection.commit();

        // create and insert user dataset.
        dataset = new Dataset(this, user.getUserId(), datasetId);
        dataset.setName(name);
        dataset.setChecksum(checksum);
        dataset.setCreatedTime(createdTime);
        dataset.setSize(size);
        dataset.setParserName(parser.getName());
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

    try(final var statement = connection.prepareStatement(sql)) {

      long start = System.currentTimeMillis();
      statement.setString(1, checksum);
      statement.setLong(2, user.getUserId());

      try(final var resultSet = statement.executeQuery()) {
        QueryLogger.logEndStatementExecution(sql,
          "wdk-dataset-by-content-checksum", start);

        return resultSet.next() ? readDataset(resultSet) : null;
      }
    } catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  private long insertDataset(
    final User user,
    final Connection connection,
    final long datasetId,
    final String name,
    final DatasetContents content,
    final int size,
    final Date createdDate,
    final String parserName,
    final String uploadFile
  ) throws WdkModelException {
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
      psInsert.setString(4, content.getChecksum());
      psInsert.setInt(5, size);
      psInsert.setTimestamp(6, new Timestamp(createdDate.getTime()));
      psInsert.setString(7, parserName);
      psInsert.setString(8, uploadFile);
      psInsert.setClob(9, content.getContentReader());
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

  private String buildDatasetValuesInsertQuery(int size) {
    var sql = new StringBuilder("INSERT INTO ");

    sql.append(_userSchema)
      .append(TABLE_DATASET_VALUES)
      .append(" (" + COLUMN_DATASET_VALUE_ID + ", " + COLUMN_DATASET_ID);

    for (int i = 1; i <= size; i++) {
      sql.append(", " + COLUMN_DATA_PREFIX).append(i);
    }

    sql.append(") VALUES (?, ?")
      .append(", ?".repeat(size))
      .append(")");

    return sql.toString();
  }

  private long copyDataset(
    final Connection con,
    final long       oldDsId,
    final long       oldUserId,
    final long       newUserId
  ) throws WdkModelException {
    long newDsId;
    try {
      newDsId = _userDb.getPlatform()
        .getNextId(_userDb.getDataSource(), _userSchema, TABLE_DATASETS);
    } catch (SQLException e) {
      throw new WdkModelException(e);
    }

    var sql = "INSERT INTO " + _userSchema + TABLE_DATASETS + " (\n"
      + "  "  + COLUMN_DATASET_ID       + "\n"
      + "  ," + COLUMN_NAME             + "\n"
      + "  ," + COLUMN_USER_ID          + "\n"
      + "  ," + COLUMN_CONTENT_CHECKSUM + "\n"
      + "  ," + COLUMN_DATASET_SIZE     + "\n"
      + "  ," + COLUMN_CREATED_TIME     + "\n"
      + "  ," + COLUMN_PARSER           + "\n"
      + "  ," + COLUMN_UPLOAD_FILE      + "\n"
      + "  ," + COLUMN_CONTENT
      + ")\n"
      + "SELECT\n"
      + "  "  + newDsId                 + "\n"
      + "  ," + COLUMN_NAME             + "\n"
      + "  ," + newUserId               + "\n"
      + "  ," + COLUMN_CONTENT_CHECKSUM + "\n"
      + "  ," + COLUMN_DATASET_SIZE     + "\n"
      + "  ," + COLUMN_CREATED_TIME     + "\n"
      + "  ," + COLUMN_PARSER           + "\n"
      + "  ," + COLUMN_UPLOAD_FILE      + "\n"
      + "  ," + COLUMN_CONTENT          + "\n"
      + "FROM\n"
      + "  " + _userSchema + TABLE_DATASETS + "\n"
      + "WHERE"
      + "  " + COLUMN_DATASET_ID + " = " + oldDsId + "\n"
      + "  AND " + COLUMN_USER_ID + " = " + oldUserId;

    new SQLRunner(con, sql).executeStatement();

    return newDsId;
  }

  private void copyDatasetValues(Connection con, long oldDsId, long newDsId) {
    var sql = "SELECT *\n"
      + "FROM " + _userSchema + TABLE_DATASET_VALUES + "\n"
      + "WHERE dataset_id = ?";

    new SQLRunner(_userDb.getDataSource(), sql).executeQuery(
      new Object[] {oldDsId},
      new Integer[] {Types.BIGINT},
      copyDatasetValues(con, newDsId)
    );
  }

  private ResultSetHandler<Void> copyDatasetValues(
    final Connection con,
    final long       newDsId
  ) {
    var sql = buildDatasetValuesInsertQuery(MAX_VALUE_COLUMNS);

    return rs -> {
      try (PreparedStatement psInsert = con.prepareStatement(sql)) {
        var row = 0;
        while (rs.next()) {

          // get a new value id.
          var datasetValueId = _userDb.getPlatform()
            .getNextId(_userDb.getDataSource(), _userSchema, TABLE_DATASET_VALUES);

          psInsert.setLong(1, datasetValueId);
          psInsert.setLong(2, newDsId);
          for (int j = 0, k = 3; j < MAX_VALUE_COLUMNS; j++, k++) {
            psInsert.setString(k, rs.getString(k));
          }
          psInsert.addBatch();

          row++;
          if (row >= 1000) {
            psInsert.executeBatch();
            row = 0;
          }
        }
        if (row > 0)
          psInsert.executeBatch();
      }

      return null;
    };
  }

  private void insertDatasetValues(
    final Connection connection,
    final long datasetId,
    final DatasetIterator data,
    final int length
  ) throws SQLException, WdkModelException, WdkUserException {
    LOG.info("insertDatasetValues");
    var sql = buildDatasetValuesInsertQuery(length);
    LOG.info("- sql");

    try (PreparedStatement psInsert = connection.prepareStatement(sql)) {
      //      for (int i = 0; i < data.size(); i++) {
      var row = 0;
      while (data.hasNext()) {
        var value = data.next();
        LOG.info("- value");

        // get a new value id.
        var datasetValueId = _userDb.getPlatform()
          .getNextId(_userDb.getDataSource(), _userSchema, TABLE_DATASET_VALUES);

        psInsert.setLong(1, datasetValueId);
        psInsert.setLong(2, datasetId);
        for (int j = 0; j < length; j++) {
          psInsert.setString(j + 3, value[j]);
        }
        psInsert.addBatch();

        row++;
        if (row >= 1000) {
          LOG.info("- batch");
          psInsert.executeBatch();
          row = 0;
        }
      }
      if (row > 0) {
        LOG.info("- remainder");
        psInsert.executeBatch();
      }
    }
  }

  private void validateValue(final String[] row) throws WdkUserException {
    // check the number of columns
    if (row.length > MAX_VALUE_COLUMNS) {
      var jsArray = new JSONArray(Arrays.asList(row));
      throw new WdkUserException(
        "The maximum allowed columns in datasets are " + MAX_VALUE_COLUMNS
          + ", but the input has more: " + jsArray.toString());
    }
    // check the length of each value
    for (String value : row)
      if (value != null && value.length() > MAX_VALUE_LENGTH)
        throw new WdkUserException("The maximum allowed length for a single "
          + "value in datasets are " + MAX_VALUE_LENGTH + ", but the input is: "
          + value);
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

  private DatasetContents getDatasetContentAsFile(final ResultSet rs)
  throws SQLException {
    try {
      return new DatasetFileContents(rs.getString(COLUMN_UPLOAD_FILE),
        rs.getClob(COLUMN_CONTENT).getCharacterStream());
    } catch (IOException e) {
      throw new WdkRuntimeException(e);
    }
  }

  private DatasetContents getDatasetContentAsString(final ResultSet rs)
  throws SQLException {
    return new DatasetStringContents(rs.getString(COLUMN_UPLOAD_FILE),
      rs.getString(COLUMN_CONTENT));
  }
}
