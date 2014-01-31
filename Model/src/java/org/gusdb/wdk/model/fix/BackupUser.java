package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wsf.util.BaseCLI;

public class BackupUser extends BaseCLI {

  private static final String BACKUP_SCHEMA = "userlogins5_archive";
  
  private static final String ARG_CUTOFF_DATE = "cutoffDate";

  private static final Logger logger = Logger.getLogger(BackupUser.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    BackupUser backup = new BackupUser(cmdName);
    try {
      backup.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      logger.info("WDK User Backup done.");
      System.exit(0);
    }
  }

  private static final String userColumns = "user_id, email, passwd, is_guest, signature, register_time, "
      + "last_active, last_name, first_name, middle_name, title, organization, department, address, city, "
      + "state, zip_code, phone_number, country, prev_user_id";
  private static final String roleColumns = "user_id, user_role";
  private static final String prefColumns = "user_id, project_id, preference_name, preference_value";
  private static final String stepColumns = "step_id, user_id, left_child_id, right_child_id, create_time, "
      + "last_run_time, estimate_size, answer_filter, custom_name, is_deleted, is_valid, collapsed_name, "
      + "is_collapsible, assigned_weight, project_id, project_version, question_name, strategy_id, "
      + "display_params, result_message, prev_step_id";
  private static final String strategyColumns = "strategy_id, user_id, root_step_id, project_id, version, "
      + "is_saved, create_time, last_view_time, last_modify_time, description, signature, name, saved_name, "
      + "is_deleted, is_public, prev_strategy_id";
  private static final String datasetColumns = "dataset_id, user_id, dataset_name, dataset_size, "
      + "content_checksum, created_time, upload_file, parser, content, prev_dataset_id";
  private static final String datasetValueColumns = "dataset_value_id, dataset_id, "
      + "data1,data2, data3, data4, data5, prev_dataset_value_id";
  private static final String basketColumns = "basket_id, user_id, basket_name, project_id, record_class, "
      + "is_default, pk_column_1, pk_column_2, pk_column_3, prev_basket_id";
  private static final String favoriteColumns = "favorite_id, user_id, project_id, record_class, "
      + "pk_column_1, pk_column_2, pk_column_3, record_note, record_group, prev_favorite_id";

  private WdkModel wdkModel;
  private String userSchema;
  private String backupSchema;

  public BackupUser(String command) {
    super((command != null) ? command : "wdkBackupUser", "This command "
        + "backs up expired guest user data to a given schema.");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");

    addSingleValueOption(ARG_CUTOFF_DATE, true, null, "Any guest user "
        + "created by this date will be backed up, and removed "
        + "from the live schema defined in the model-config.xml. "
        + "The data should be in this format: yyyy/mm/dd");
  }

  @Override
  protected void execute() throws Exception {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

    backupSchema = BACKUP_SCHEMA;

    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    String cutoffDate = (String) getOptionValue(ARG_CUTOFF_DATE);

    wdkModel = WdkModel.construct(projectId, gusHome);
    userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    backupSchema = DBPlatform.normalizeSchema(backupSchema);
    userSchema = DBPlatform.normalizeSchema(userSchema);

    logger.info("Backing up user data from " + userSchema + " to " + backupSchema + "...");

    backupGuestUsers(userSchema, backupSchema, cutoffDate);
  }

  public void backupGuestUsers(String userSchema, String backupSchema, String cutoffDate) throws SQLException {
    Connection connection = wdkModel.getUserDb().getDataSource().getConnection();
    boolean autoCommit = connection.getAutoCommit();
    connection.setAutoCommit(false);
    Statement statement = connection.createStatement();

    String deleteCondtion = "user_id IN (SELECT user_id FROM " + userSchema + "users " +
        " WHERE is_guest = 1 AND register_time < to_date('" + cutoffDate + "', 'yyyy/mm/dd'))";
    try {
      backupTable(statement, "users", userColumns);
      backupTable(statement, "user_roles", roleColumns);
      backupTable(statement, "preferences", prefColumns);
      backupTable(statement, "steps", stepColumns);
      backupTable(statement, "strategies", strategyColumns);
      backupTable(statement, "datasets", datasetColumns);
      backupTable(statement, "user_baskets", basketColumns);
      backupTable(statement, "favorites", favoriteColumns);
      backupTable(statement, "dataset_values", datasetValueColumns, "dataset_id");

      removeGuest(statement, "dataset_values", "dataset_id IN (SELECT dataset_id FROM " + userSchema +
          "datasets WHERE " + deleteCondtion + ")");
      removeGuest(statement, "favorites", deleteCondtion);
      removeGuest(statement, "user_baskets", deleteCondtion);
      removeGuest(statement, "datasets", deleteCondtion);
      removeGuest(statement, "strategies", deleteCondtion);
      removeGuest(statement, "steps", deleteCondtion);
      removeGuest(statement, "preferences", deleteCondtion);
      removeGuest(statement, "user_roles", deleteCondtion);
      removeGuest(statement, "users", deleteCondtion);
      removeGuest(statement, "favorites", deleteCondtion);

      connection.commit();
    }
    catch (SQLException ex) {
      connection.rollback();
      throw ex;
    }
    finally {
      statement.close();
      connection.setAutoCommit(autoCommit);
      connection.close();
    }
  }

  private void backupTable(Statement statement, String table, String columns) throws SQLException {
    backupTable(statement, table, columns, "user_id");
  }

  private void backupTable(Statement statement, String table, String columns, String keyColumn)
      throws SQLException {
    String fromTable = userSchema + table;
    String toTable = backupSchema + table;

    // first, delete duplicate rows from backup table, so that updated data can be copied over.
    statement.executeUpdate("DELETE FROM " + toTable + " WHERE " + keyColumn + " IN (SELECT " + keyColumn +
        " FROM " + fromTable + ")");

    // then copy all rows into backup
    statement.executeUpdate("INSERT INTO " + toTable + "(" + columns + ") SELECT " + columns + " FROM " +
        fromTable);
  }

  private void removeGuest(Statement statement, String table, String condition) throws SQLException {
    String fromTable = userSchema + table;
    statement.executeUpdate("DELETE FROM " + fromTable + " WHERE " + condition);
  }

  // <ADD-AG 042111>
  // -----------------------------------------------------------

  // private void executeByBatch(WdkModel wdkModel, String name, String dmlSql, String selectSql)
  // throws SQLException {
  // DataSource dataSource = wdkModel.getUserDb().getDataSource();
  // Connection connection = null;
  // PreparedStatement psInsert = null;
  // ResultSet resultSet = null;
  //
  // try {
  // resultSet = SqlUtils.executeQuery(dataSource, selectSql, "wdk-backup-" + name);
  //
  // connection = dataSource.getConnection();
  // psInsert = connection.prepareStatement(dmlSql);
  //
  // int count = 0;
  //
  // while (resultSet.next()) {
  // int userId = resultSet.getInt(1);
  //
  // psInsert.setInt(1, userId);
  // psInsert.addBatch();
  //
  // count++;
  // if (count % 1000 == 0) {
  // psInsert.executeBatch();
  // logger.info("Rows processed for " + name + " = " + count + ".");
  // }
  // }
  //
  // psInsert.executeBatch();
  // logger.info("Total rows processed for " + name + " = " + count + ".");
  // }
  // finally {
  // SqlUtils.closeResultSetAndStatement(resultSet);
  // SqlUtils.closeStatement(psInsert);
  // }
  // }
}
