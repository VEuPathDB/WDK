package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wsf.util.BaseCLI;

public class BackupUser extends BaseCLI {

    private static final String ARG_BACKUP_SCHEMA = "backupSchema";
    private static final String ARG_CUTOFF_DATE = "cutoffDate";

    private static final Logger logger = Logger.getLogger(BackupUser.class);

    /**
     * @param args
     * @throws Exception
     */
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

    private static final String userColumns = "user_id, email, passwd, "
            + "is_guest, signature, register_time, last_active, last_name, "
            + "first_name, middle_name, title, organization, department, "
            + "address, city, state, zip_code, phone_number, country, "
            + "prev_user_id";
    private static final String roleColumns = "user_id, user_role";
    private static final String prefColumns = "user_id, project_id, "
            + "preference_name, preference_value";
    private static final String stepColumns = "step_id, display_id, user_id, "
            + "answer_id, left_child_id, right_child_id, create_time, "
            + "last_run_time, estimate_size, answer_filter, custom_name, "
            + "is_deleted, is_valid, collapsed_name, is_collapsible, "
            + "display_params, prev_step_id, invalid_message, assigned_weight";
    private static final String strategyColumns = "strategy_id, display_id, "
            + "user_id, root_step_id, project_id, is_saved, create_time, "
            + "last_view_time, last_modify_time, description, signature, name, "
            + "saved_name, is_deleted, prev_strategy_id";
    private static final String userDatasetColumns = "user_dataset_id, dataset_id, "
            + "user_id, create_time, upload_file, prev_user_dataset_id";
    private static final String basketColumns = "user_id, project_id, "
            + "record_class, pk_column_1, pk_column_2, pk_column_3";
    private static final String favoriteColumns = "user_id, project_id, "
            + "record_class, pk_column_1, pk_column_2, pk_column_3, "
            + "record_note, record_group";
    private static final String datasetIndexColumns = "dataset_id, "
            + "dataset_checksum, record_class, summary, dataset_size, "
            + "PREV_DATASET_ID";
    private static final String datasetValueColumns = "dataset_id, pk_column_1,"
            + "            pk_column_2, pk_column_3";
    private static final String answerColumns = "answer_id, answer_checksum, "
            + "project_id, project_version, question_name, query_checksum, "
            + "old_query_checksum, params, result_message, prev_answer_id";

    private WdkModel wdkModel;
    private String userSchema;
    private String wdkSchema;
    private String backupSchema;

    public BackupUser(String command) {
        super((command != null) ? command : "wdkBackupUser", "This command "
                + "backs up expired guest user data to a given schema.");
    }

    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId"
                + ", which should match the directory name under $GUS_HOME, "
                + "where model-config.xml is stored.");

        addSingleValueOption(ARG_BACKUP_SCHEMA, true, null, "the backup schema"
                + " where the data should be stored.");

        addSingleValueOption(ARG_CUTOFF_DATE, true, null, "Any guest user "
                + "created by this date will be backed up, and removed "
                + "from the live schema defined in the model-config.xml. "
                + "The data should be in this format: yyyy/mm/dd");
    }

    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        backupSchema = (String) getOptionValue(ARG_BACKUP_SCHEMA);

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);
        String cutoffDate = (String) getOptionValue(ARG_CUTOFF_DATE);
        logger.info("Backing up guest user data... ");

        wdkModel = WdkModel.construct(projectId, gusHome);
        userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
        wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();

        backupSchema = DBPlatform.normalizeSchema(backupSchema);
        userSchema = DBPlatform.normalizeSchema(userSchema);
        wdkSchema = DBPlatform.normalizeSchema(wdkSchema);

        backupGuestUsers(userSchema, wdkSchema, backupSchema, cutoffDate);
    }

    public void backupGuestUsers(String userSchema, String wdkSchema,
            String backupSchema, String cutoffDate) throws WdkUserException,
            WdkModelException, SQLException {
        // copy tables from user schema
        copyUsers();
        copyRows(roleColumns, "user_roles");
        copyRows(prefColumns, "preferences");
        copyRows(basketColumns, "user_baskets");
        copyRows(favoriteColumns, "favorites");
        copyUserDatasetRows();
        copyStepRows();
        copyStrategyRows();

        // delete rows from user schema
        cutoffDate = "to_date('" + cutoffDate + "', 'yyyy/mm/dd')";
        String selectSql = "SELECT user_id FROM " + userSchema + "users "
                + " WHERE is_guest = 1 AND register_time < " + cutoffDate;
        deleteRows(selectSql, "strategies");
        deleteRows(selectSql, "steps");
        deleteRows(selectSql, "user_datasets2");
        deleteRows(selectSql, "favorites");
        deleteRows(selectSql, "user_baskets");
        deleteRows(selectSql, "preferences");
        deleteRows(selectSql, "user_roles");
        deleteRows(selectSql, "users");

        // copy other data
        copyAnswerRows();
        deleteAnswerRows();

        copyDatasetIndexRows();
        copyDatasetValueRows();
        deleteDatasetValueRows();
        deleteDatasetIndexRows();
    }

    // <ADD-AG 042111>
    // -----------------------------------------------------------

    private void executeByBatch(WdkModel wdkModel, String name, String dmlSql,
            String selectSql) throws SQLException, WdkUserException,
            WdkModelException {
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        Connection connection = null;
        PreparedStatement psInsert = null;
        ResultSet resultSet = null;

        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, selectSql,
                    "wdk-backup-" + name);

            connection = dataSource.getConnection();
            psInsert = connection.prepareStatement(dmlSql);

            int count = 0;

            while (resultSet.next()) {
                int userId = resultSet.getInt(1);

                psInsert.setInt(1, userId);
                psInsert.addBatch();

                count++;
                if (count % 1000 == 0) {
                    psInsert.executeBatch();
                    logger.info("Rows processed for " + name + " = " + count
                            + ".");
                }
            }

            psInsert.executeBatch();
            logger.info("Total rows processed for " + name + " = " + count
                    + ".");
        }
        finally {
            SqlUtils.closeResultSet(resultSet);
            SqlUtils.closeStatement(psInsert);
        }
    }

    // </ADD-AG 042111>
    // ----------------------------------------------------------

    private void copyUsers() throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Copying from users...");

        String fromTable = userSchema + "users";
        String toTable = backupSchema + "users";
        String dmSql = "INSERT INTO " + toTable + " (" + userColumns + ")"
                + " SELECT " + userColumns + " FROM " + fromTable
                + " WHERE user_id  = ?";
        String selectSql = "SELECT user_id FROM " + fromTable
                + " MINUS SELECT user_id FROM " + toTable;

        // <ADD-AG 042111>
        executeByBatch(wdkModel, "users", dmSql, selectSql);
    }

    /**
     * TODO - this way of copying records will only copy the records of new
     * users, but the changes of the old users will be ignored. need to be fixed
     * in the future, probably by adding primairy key id column to the table;
     * 
     * @param columns
     * @param table
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     */
    private void copyRows(String columns, String table)
            throws WdkUserException, WdkModelException, SQLException {
        logger.debug("Copying from " + table + "...");

        String fromTable = userSchema + table;
        String toTable = backupSchema + table;
        String dmSql = "INSERT INTO " + toTable + " (" + columns + ")"
                + " SELECT " + columns + " FROM " + fromTable
                + " WHERE user_id = ?";
        // select users that are in the backup users with rows, exclude users
        // that already have rows backed up.
        String selectSql = "SELECT t.user_id                            "
                + "  FROM " + fromTable + " t,  " + backupSchema + "users u "
                + "  WHERE t.user_id = u.user_id "
                + " MINUS SELECT user_id FROM " + toTable;

        // <ADD-AG 042111>
        executeByBatch(wdkModel, table, dmSql, selectSql);
    }

    private void copyUserDatasetRows() throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("Copying from user_datasets2...");

        String fromTable = userSchema + "user_datasets2";
        String toTable = backupSchema + "user_datasets2";
        String dmSql = "INSERT INTO " + toTable + " (" + userDatasetColumns
                + ") SELECT " + userDatasetColumns + " FROM " + fromTable
                + " WHERE user_dataset_id = ? ";
        String selectSql = "SELECT d.user_dataset_id                        "
                + "    FROM " + fromTable + " d, " + backupSchema + "users u"
                + "    WHERE d.user_id = u.user_id "
                + "  MINUS SELECT user_dataset_id FROM " + toTable;

        // <ADD-AG 042111>
        executeByBatch(wdkModel, "user_datasets2", dmSql, selectSql);
    }

    private void copyStepRows() throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Copying from steps...");

        String fromTable = userSchema + "steps";
        String toTable = backupSchema + "steps";
        String dmSql = "INSERT INTO " + toTable + " (" + stepColumns + ")"
                + " SELECT " + stepColumns + " FROM " + fromTable
                + " WHERE step_id = ? ";
        String selectSql = "SELECT s.step_id                               "
                + "    FROM " + fromTable + " s, " + backupSchema + "users u"
                + "    WHERE s.user_id = u.user_id "
                + "  MINUS SELECT step_id FROM " + toTable;

        // <ADD-AG 042111>
        executeByBatch(wdkModel, "steps", dmSql, selectSql);
    }

    private void copyStrategyRows() throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("Copying from strategies...");

        String fromTable = userSchema + "strategies";
        String toTable = backupSchema + "strategies";
        String dmSql = "INSERT INTO " + toTable + " (" + strategyColumns + ")"
                + " SELECT " + strategyColumns + " FROM " + fromTable
                + " WHERE strategy_id = ? ";
        String selectSql = "SELECT s.strategy_id                 "
                + "     FROM " + fromTable + " s, " + backupSchema + "users u"
                + "     WHERE s.user_id = u.user_id "
                + "   MINUS SELECT strategy_id FROM " + toTable;

        // <ADD-AG 042111>
        executeByBatch(wdkModel, "strategies", dmSql, selectSql);
    }

    private void deleteRows(String selectSql, String tableName)
            throws WdkUserException, WdkModelException, SQLException {
        logger.debug("deleting from " + tableName + "...");

        String dmSql = "DELETE FROM " + userSchema + tableName
                + " WHERE user_id = ?";

        // <ADD-AG 042311>
        executeByBatch(wdkModel, tableName, dmSql, selectSql);
    }

    /**
     * copy answers that are not used by steps
     * 
     * @throws SQLException
     * @throws WdkModelException
     * @throws WdkUserException
     */
    private void copyAnswerRows() throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("copying answer rows...");

        // <ADD-AG 042211>
        // -----------------------------------------------------------
        String fromTable = wdkSchema + "answers";
        String toTable = backupSchema + "answers";
        String dmlSql = "INSERT INTO " + toTable + " (" + answerColumns + ") "
                + "SELECT " + answerColumns + " FROM " + fromTable
                + " WHERE answer_id = ?";

        String selectSql = "SELECT answer_id FROM " + fromTable
                + " MINUS SELECT answer_id FROM " + toTable;

        executeByBatch(wdkModel, "ANSWERS", dmlSql, selectSql);
    }

    private void copyDatasetIndexRows() throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("copying dataset index rows...");

        // <ADD-AG 042311>
        // -----------------------------------------------------------

        String fromTable = wdkSchema + "dataset_indices";
        String toTable = backupSchema + "dataset_indices";
        String dmlSql = "INSERT INTO " + toTable + "  (" + datasetIndexColumns
                + ") SELECT " + datasetIndexColumns + " FROM " + fromTable
                + " WHERE dataset_id  = ?";

        String selectSql = "SELECT dataset_id FROM " + fromTable
                + " MINUS SELECT dataset_id FROM " + toTable;

        executeByBatch(wdkModel, "DATASET_INDICES", dmlSql, selectSql);
    }

    private void copyDatasetValueRows() throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("copying dataset value rows...");

        // <ADD-AG 042511>
        // -----------------------------------------------------------
        String fromTable = wdkSchema + "dataset_values";
        String toTables = backupSchema + "dataset_values";
        String dmlSql = "INSERT INTO " + toTables + " (" + datasetValueColumns
                + ")     SELECT DISTINCT " + datasetValueColumns
                + "      FROM " + fromTable + " WHERE dataset_id = ?";

        String selectSql = "SELECT DISTINCT dataset_id FROM " + fromTable
                + " MINUS SELECT dataset_id FROM " + toTables;

        executeByBatch(wdkModel, "DATASET_VALUES", dmlSql, selectSql);
    }

    private void deleteAnswerRows() throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("deleting answer rows...");

        // <ADD-AG 042311>
        // -----------------------------------------------------------
        String fromTable = wdkSchema + "answers";
        String dmlSql = "DELETE FROM " + fromTable + " WHERE answer_id  = ?";

        String selectSql = "SELECT answer_id FROM "
                + " ( (SELECT answer_id FROM " + fromTable + ")"
                + "   MINUS                         "
                + "   ( SELECT answer_id FROM " + userSchema + "steps    "
                + "     UNION SELECT answer_id FROM wdkuser.steps)       "
                + " )";

        executeByBatch(wdkModel, "ANSWERS", dmlSql, selectSql);
    }

    private void deleteDatasetIndexRows() throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("deleting dataset index rows...");

        // <ADD-AG 042311>
        // -----------------------------------------------------------

        String fromTable = wdkSchema + "dataset_indices";
        String dmlSql = "DELETE FROM " + fromTable + " WHERE dataset_id = ?";

        String selectSql = "SELECT dataset_id FROM " + ""
                + " ((SELECT dataset_id FROM " + fromTable + ")"
                + "  MINUS                      "
                + "  ( SELECT dataset_id FROM " + userSchema + "user_datasets2"
                + "    UNION SELECT dataset_id FROM wdkuser.user_datasets)"
                + " )";

        executeByBatch(wdkModel, "DATASET_INDICES", dmlSql, selectSql);
    }

    private void deleteDatasetValueRows() throws WdkUserException,
            WdkModelException, SQLException {
        logger.debug("deleting dataset value rows...");

        // <ADD-AG 042511>
        // -----------------------------------------------------------
        String fromTable = wdkSchema + "dataset_values";
        String dmlSql = "DELETE FROM " + fromTable + " WHERE dataset_id = ?";

        String selectSql = "SELECT dataset_id FROM "
                + " ( (SELECT distinct dataset_id FROM " + fromTable + ")"
                + "   MINUS                     "
                + "   (SELECT dataset_id FROM " + userSchema + "user_datasets2"
                + "    UNION "
                + "    SELECT dataset_id FROM wdkuser.user_datasets)      "
                + " )";

        executeByBatch(wdkModel, "DATASET_VALUES", dmlSql, selectSql);
    }
}
