/**
 * 
 */
package org.gusdb.wdk.model.fix;

import java.security.NoSuchAlgorithmException;
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
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wsf.util.BaseCLI;
import org.json.JSONException;

/**
 * @author xingao
 * 
 *         due to the change of the way the question checksum is generated, the
 *         older steps became invalid, this code is to fix that, one time work.
 *         Noe the code is considered deprecated.
 */
@Deprecated
public class QuestionChecksumUpdater extends BaseCLI {

    private static final Logger logger = Logger.getLogger(QuestionChecksumUpdater.class);

    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        QuestionChecksumUpdater updater = new QuestionChecksumUpdater(cmdName);
        try {
            updater.invoke(args);
        } finally {
            logger.info("question checksum updated.");
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    protected QuestionChecksumUpdater(String command) {
        super((command == null) ? command : "answerChecksumUpdater",
                "Update the question checksum stored in answer table to the"
                        + " latest version from model");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "A comma-separated"
                + " list of ProjectIds, which should match the directory name "
                + "under $GUS_HOME, where model-config.xml is stored.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wsf.util.BaseCLI#execute()
     */
    @Override
    protected void execute() throws Exception {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

        String strProject = (String) getOptionValue(ARG_PROJECT_ID);
        String[] projects = strProject.split(",");
        for (String projectId : projects) {
            logger.info("Updating question checksum for project " + projectId);
            WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

            updateChecksum(wdkModel);
            // copyParams(wdkModel);
        }
    }

    public void updateChecksum(WdkModel wdkModel)
            throws NoSuchAlgorithmException, JSONException, WdkModelException,
            SQLException {
        PreparedStatement psUpdate = null;
        try {
            psUpdate = prepareUpdate(wdkModel);
            for (QuestionSet questionSet : wdkModel.getAllQuestionSets()) {
                for (Question question : questionSet.getQuestions()) {
                    String questionName = question.getFullName();
                    String checksum = question.getQuery().getChecksum(false);
                    psUpdate.setString(1, checksum);
                    psUpdate.setString(2, questionName);
                    psUpdate.setString(3, wdkModel.getProjectId());
                    psUpdate.addBatch();
                }
            }
            psUpdate.executeBatch();
        } finally {
            SqlUtils.closeStatement(psUpdate);
        }
    }

    private PreparedStatement prepareUpdate(WdkModel wdkModel)
            throws SQLException, WdkModelException {
        StringBuffer sql = new StringBuffer("UPDATE ");
        sql.append(wdkModel.getModelConfig().getUserDB().getWdkEngineSchema());
        sql.append("answers SET old_query_checksum = query_checksum, ");
        sql.append(" query_checksum = ? WHERE question_name = ? ");
        sql.append(" AND project_id = ? AND old_query_checksum IS NULL");
        DataSource dataSource = wdkModel.getUserPlatform().getDataSource();
        return SqlUtils.getPreparedStatement(dataSource, sql.toString());
    }

    public void copyParams(WdkModel wdkModel) throws SQLException,
            WdkUserException, WdkModelException {
        PreparedStatement psUpdate = null;
        ResultSet rsHistory = null;
        DBPlatform platform = wdkModel.getUserPlatform();
        DataSource dataSource = platform.getDataSource();

        try {
            rsHistory = SqlUtils.executeQuery(wdkModel, dataSource, "SELECT "
                    + "     h.history_id, h.user_id, a.params "
                    + "FROM userlogins3.histories h, userlogins3.users u, "
                    + "     wdkstorage.answer a  "
                    + "WHERE u.is_guest = 0 AND u.user_id = h.user_id "
                    + "  AND h.answer_id = a.answer_id "
                    + "  AND length(h.display_params) = 0 ",
                    "wdk-select-history");
            psUpdate = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + "  userlogins3.histories SET display_params = ? "
                    + "WHERE user_id = ? AND history_id = ? ");

            int count = 0;
            while (rsHistory.next()) {
                int historyId = rsHistory.getInt("history_id");
                int userId = rsHistory.getInt("user_id");
                String params = platform.getClobData(rsHistory, "params");

                platform.setClobData(psUpdate, 1, params, false);
                psUpdate.setInt(2, userId);
                psUpdate.setInt(3, historyId);
                psUpdate.executeUpdate();

                count++;
                if (count % 100 == 0)
                    logger.debug(count + " history params updated.");
            }
            logger.info("totally updated " + count + " history params.");
        } finally {
            SqlUtils.closeResultSetAndStatement(rsHistory);
            SqlUtils.closeStatement(psUpdate);
        }
    }
}
