package org.gusdb.wdk.model.test;

import java.sql.Connection;
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
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.info("WDK User Backup done.");
            System.exit(0);
        }
    }

    private String[] userColumns = { "user_id", "email", "passwd", "is_guest",
            "signature", "register_time", "last_active", "last_name",
            "first_name", "middle_name", "title", "organization", "department",
            "address", "city", "state", "zip_code", "phone_number", "country",
            "prev_user_id" };
    private String[] roleColumns = { "user_id", "user_role" };
    private String[] preferenceColumns = { "user_id", "project_id",
            "preference_name", "preference_value" };
    private String[] stepColumns = { "step_id", "display_id", "user_id",
            "answer_id", "left_child_id", "right_child_id", "create_time",
            "last_run_time", "estimate_size", "answer_filter", "custom_name",
            "is_deleted", "is_valid", "collapsed_name", "is_collapsible",
            "display_params", "prev_step_id", "invalid_message",
            "assigned_weight" };
    private String[] strategyColumns = { "strategy_id", "display_id",
            "user_id", "root_step_id", "project_id", "is_saved", "create_time",
            "last_view_time", "last_modify_time", "description", "signature",
            "name", "saved_name", "is_deleted", "prev_strategy_id" };
    private String[] datasetColumns = { "user_dataset_id", "dataset_id",
            "user_id", "create_time", "upload_file", "prev_user_dataset_id" };
    private String[] basketColumns = { "user_id", "project_id", "record_class",
            "pk_column_1", "pk_column_2", "pk_column_3" };
    private String[] favoriteColumns = { "user_id", "project_id",
            "record_class", "pk_column_1", "pk_column_2", "pk_column_3",
            "record_note", "record_group" };

    public BackupUser(String command) {
        super((command == null) ? command : "wdkBackupUser", "This command "
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

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);
        String backupSchema = (String) getOptionValue(ARG_BACKUP_SCHEMA);
        String cutoffDate = (String) getOptionValue(ARG_CUTOFF_DATE);
        logger.info("Backing up guest user data... ");

        WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

        backupGuestUsers(wdkModel, backupSchema, cutoffDate);
    }

    public void backupGuestUsers(WdkModel wdkModel, String backupSchema,
            String cutoffDate) throws WdkUserException, WdkModelException,
            SQLException {
        // normalize schema
        backupSchema = DBPlatform.normalizeSchema(backupSchema);

        copyRows(wdkModel, backupSchema, cutoffDate, "users", userColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "user_roles", roleColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "preferences",
                preferenceColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "user_baskets",
                basketColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "favorites",
                favoriteColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "user_datasets2",
                datasetColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "steps", stepColumns);
        copyRows(wdkModel, backupSchema, cutoffDate, "strategies",
                strategyColumns);

        deleteRows(wdkModel, cutoffDate, "strategies");
        deleteRows(wdkModel, cutoffDate, "steps");
        deleteRows(wdkModel, cutoffDate, "user_datasets2");
        deleteRows(wdkModel, cutoffDate, "favorites");
        deleteRows(wdkModel, cutoffDate, "user_baskets");
        deleteRows(wdkModel, cutoffDate, "preferences");
        deleteRows(wdkModel, cutoffDate, "user_roles");
        deleteRows(wdkModel, cutoffDate, "users");
    }

    private void copyRows(WdkModel wdkModel, String backupSchema,
            String cutoffDate, String tableName, String[] columns)
            throws WdkUserException, WdkModelException, SQLException {
        logger.debug("Copying from " + tableName + "...");
        String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String column : columns) {
            if (first) first = false;
            else builder.append(", ");
            builder.append(column);
        }

        String sql = "INSERT INTO " + backupSchema + tableName + " (" + builder
                + ") SELECT " + builder + " FROM " + schema + tableName
                + " WHERE user_id IN (SELECT user_id FROM " + schema
                + "users WHERE is_guest = 1 AND register_time < to_date('"
                + cutoffDate + "', 'yyyy/mm/dd'))";

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql, "wdk-backup-insert-"
                + tableName);
    }

    private void deleteRows(WdkModel wdkModel, String cutoffDate,
            String tableName) throws WdkUserException, WdkModelException,
            SQLException {
        logger.debug("deleting from " + tableName + "...");
        String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();

        String sql = "DELETE FROM " + schema + tableName + " WHERE user_id IN "
                + "   (SELECT user_id FROM " + schema + "users "
                + "    WHERE is_guest = 1 AND register_time < to_date('"
                + cutoffDate + "', 'yyyy/mm/dd'))";

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        SqlUtils.executeUpdate(wdkModel, dataSource, sql, "wdk-backup-delete-"
                + tableName);
    }
}
