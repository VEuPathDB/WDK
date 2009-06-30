/**
 * 
 */
package org.gusdb.wdk.model.migrate;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.ModelConfigUserDB;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class Migrator1_18To1_19 extends Migrator {

    private static final Logger logger = Logger.getLogger(Migrator1_18To1_19.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.migrate.Migrator#migrate()
     */
    @Override
    public void migrate() throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        copyData();
    }

    public void copyData() throws SQLException {
        copyClobValues();
        copyDatasetIndices();
        copyDatasetValues();
        copyUserDatasets();
        copyAnswers();
        copySteps();
    }

    /**
     * @throws SQLException
     * 
     */
    private void copyClobValues() throws SQLException {
        String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        String cvo = oldWdkSchema + "clob_values";
        String cvn = newWdkSchema + "clob_values";
        StringBuffer sql = new StringBuffer("INSERT INTO " + cvn);
        sql.append("  (clob_checksum, clob_value) ");
        sql.append("SELECT cvo.clob_checksum, cvo.clob_value ");
        sql.append("FROM ").append(cvo).append(" cvo, ");
        sql.append("  (SELECT clob_checksum FROM ").append(cvo);
        sql.append("   MINUS");
        sql.append("   SELECT clob_checksum FROM ").append(cvn).append(") cvm ");
        sql.append("WHERE cvo.clob_checksum = cvm.clob_checksum ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " clob_value rows inserted");
    }

    /**
     * @throws SQLException
     * 
     */
    private void copyDatasetIndices() throws SQLException {
        String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        String dio = oldWdkSchema + "dataset_indices";
        String din = newWdkSchema + "dataset_indices";
        StringBuffer sql = new StringBuffer("INSERT INTO " + din);
        sql.append("  (dataset_id, dataset_checksum, summary, ");
        sql.append("   dataset_size, prev_dataset_id) ");
        sql.append("SELECT ");
        sql.append(newWdkSchema).append(
                "dataset_indices_pkseq.nextval as dataset_id, ");
        sql.append("  dataset_checksum, summary, dataset_size, prev_dataset_id ");
        sql.append("FROM (SELECT DISTINCT d.dataset_checksum, d.summary, ");
        sql.append("        d.dataset_size, d.dataset_id AS prev_dataset_id ");
        sql.append("      FROM ").append(dio).append(" d, ");
        sql.append(oldUserSchema).append("user_datasets ud, ");
        sql.append("         (SELECT dataset_checksum FROM ").append(dio);
        sql.append("          MINUS ");
        sql.append("          SELECT dataset_checksum FROM ").append(din).append(
                ") dm ");
        sql.append("      WHERE d.dataset_id = ud.dataset_id ");
        sql.append("        AND d.dataset_checksum = dm.dataset_checksum)");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " dataset index rows inserted");
    }

    /**
     * 
     * @throws SQLException
     */
    private void copyDatasetValues() throws SQLException {
        String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        String dvo = oldWdkSchema + "dataset_values";
        String dvn = newWdkSchema + "dataset_values";
        StringBuffer sql = new StringBuffer("INSERT INTO " + dvn);
        sql.append("  (dataset_id, dataset_value) ");
        sql.append("SELECT di.dataset_id, dv.dataset_value ");
        sql.append("FROM ").append(dvo).append(" dv, ");
        sql.append(newWdkSchema).append("dataset_indices di ");
        sql.append("WHERE dv.dataset_id = di.prev_dataset_id ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " dataset value rows inserted");
    }

    /**
     * 
     * @throws SQLException
     */
    private void copyAnswers() throws SQLException {
        String newWdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        String ao = oldWdkSchema + "answer";
        String an = newWdkSchema + "answers";
        StringBuffer sql = new StringBuffer("INSERT INTO " + an);
        sql.append("  (answer_id, answer_checksum, project_id, project_version, ");
        sql.append("   question_name, query_checksum, prev_answer_id) ");
        sql.append("SELECT ").append(newWdkSchema).append(
                "answers_pkseq.nextval, ");
        sql.append("  answer_checksum, project_id, project_version, ");
        sql.append("  question_name, query_checksum, prev_answer_id ");
        sql.append("FROM (SELECT DISTINCT a.answer_checksum, a.project_id, ");
        sql.append("        a.project_version, a.question_name, ");
        sql.append("        a.query_checksum, a.answer_id AS prev_answer_id ");
        sql.append("      FROM ").append(ao).append(" a, ");
        sql.append(oldUserSchema).append("histories h, ");
        sql.append(oldUserSchema).append("users u, ");
        sql.append("        (SELECT answer_id FROM ").append(ao);
        sql.append("         MINUS ");
        sql.append("         SELECT ao.answer_id FROM ");
        sql.append(ao).append(" ao, ").append(an).append(" an ");
        sql.append("         WHERE ao.question_name = an.question_name ");
        sql.append("           AND ao.project_id = an.project_id ");
        sql.append("           AND ao.answer_checksum = an.answer_checksum) am ");
        sql.append("      WHERE a.answer_id = h.answer_id ");
        sql.append("        AND a.answer_id = am.answer_id ");
        sql.append("        AND h.user_id = u.user_id AND u.is_guest = 0) ");

        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " answer rows inserted");

        sql = new StringBuffer("UPDATE (");
        sql.append("SELECT an.params, an.result_message, ");
        sql.append("  ao.params AS old_params, ao.result_message AS old_message ");
        sql.append("FROM ").append(an).append(" an, ").append(ao).append(" ao ");
        sql.append("WHERE an.prev_answer_id = ao.answer_id) t ");
        sql.append("SET t.params = t.old_params, t.result_message = t.old_message");

        count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " answer rows updated");
    }

    /**
     * 
     * @throws SQLException
     */
    private void copyUserDatasets() throws SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String newWdkSchema = userDB.getWdkEngineSchema();
        String newUserSchema = userDB.getUserSchema();
        String udo = oldUserSchema + "user_datasets";
        String udn = newUserSchema + "user_datasets2";
        StringBuffer sql = new StringBuffer("INSERT INTO " + udn);
        sql.append("  (user_dataset_id, dataset_id, user_id, ");
        sql.append("   create_time, upload_file) ");
        sql.append("SELECT ").append(newUserSchema).append(
                "user_datasets2_pkseq.nextval, ");
        sql.append("  din.dataset_id, u.user_id, udo.create_time, udo.upload_file ");
        sql.append("FROM ").append(newUserSchema).append("users u, ");
        sql.append(newWdkSchema).append("dataset_indices din, ");
        sql.append(oldWdkSchema).append("dataset_indices dio, ");
        sql.append(udo).append(" udo ");
        sql.append("WHERE u.prev_user_id = udo.user_id ");
        sql.append("  AND udo.dataset_id = dio.dataset_id ");
        sql.append("  AND dio.dataset_checksum = din.dataset_checksum ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " user dataset rows inserted");
    }

    /**
     * 
     * @throws SQLException
     */
    private void copySteps() throws SQLException {
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        String newWdkSchema = userDB.getWdkEngineSchema();
        String newUserSchema = userDB.getUserSchema();
        String h = oldUserSchema + "histories";
        String s = newUserSchema + "steps";
        String ao = oldWdkSchema + "answer";
        String an = newWdkSchema + "answers";
        StringBuffer sql = new StringBuffer("INSERT INTO " + s);
        sql.append("  (step_id, display_id, user_id, answer_id, create_time, ");
        sql.append("   last_run_time, estimate_size, answer_filter, ");
        sql.append("   custom_name, is_deleted, display_params, prev_step_id) ");
        sql.append("SELECT ").append(newUserSchema).append(
                "steps_pkseq.nextval, ");
        sql.append("  NVL((SELECT max(sn.display_id) FROM ").append(s);
        sql.append("  sn WHERE sn.user_id = u.user_id), '0') + rownum AS display_id, ");
        sql.append("  u.user_id, an.answer_id, h.create_time, h.last_run_time, ");
        sql.append("  h.estimate_size, h.answer_filter,  h.custom_name, ");
        sql.append("  h.is_deleted, h.display_params, h.history_id as prev_step_id ");
        sql.append("FROM ").append(newUserSchema).append("users u, ");
        sql.append(h).append(" h, ");
        sql.append(an).append(" an, ").append(ao).append(" ao ");
        sql.append("WHERE u.prev_user_id = h.user_id ");
        sql.append("  AND h.answer_id = ao.answer_id ");
        sql.append("  AND ao.answer_checksum = an.answer_checksum ");
        sql.append("  AND ao.project_id = an.project_id ");

        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        int count = SqlUtils.executeUpdate(dataSource, sql.toString());
        logger.debug(count + " step rows inserted");
    }
}
