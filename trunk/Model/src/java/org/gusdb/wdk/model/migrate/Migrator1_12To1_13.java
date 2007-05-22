/**
 * @description
 */
package org.gusdb.wdk.model.migrate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author Jerric
 * @created May 22, 2007
 * @modified May 22, 2007
 */
public class Migrator1_12To1_13 extends Migrator {

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.migrate.Migrator#migrate()
     */
    @Override
    public void migrate() throws WdkModelException, WdkUserException {
        // update histories
        updateHistories();
    }

    /**
     * @throws WdkUserException
     * @throws WdkModelException
     * 
     * 
     */
    private void updateHistories() throws WdkUserException, WdkModelException {
        RDBMSPlatformI platform = wdkModel.getAuthenticationPlatform();
        DataSource dataSource = platform.getDataSource();
        String newSchema = getNewSchema();

        ResultSet rsHistory = null;
        PreparedStatement psHistory = null;
        try {
            rsHistory = SqlUtils.getResultSet(dataSource, "SELECT user_id, "
                    + "project_id, history_id, params FROM " + newSchema
                    + "histories WHERE query_instance_checksum IS NULL");
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
                    + newSchema + "histories SET query_instance_checksum = ? "
                    + ", params = ? WHERE user_id = ? AND project_id = ? "
                    + "AND history_id = ?");
            int count = 0;
            while (rsHistory.next()) {
                int userId = rsHistory.getInt("user_id");
                String projectId = rsHistory.getString("history_id");
                int historyId = rsHistory.getInt("user_id");
                String params = platform.getClobData(rsHistory, "params");

                // update
                params = params.replaceAll("--WDK_PARAM_DIVIDER--",
                        Utilities.DATA_DIVIDER);
                String content = projectId + Utilities.DATA_DIVIDER + params;
                String checksum = Utilities.encrypt(content);

                psHistory.setString(1, checksum);
                platform.updateClobData(psHistory, 2, params, false);
                psHistory.setInt(3, userId);
                psHistory.setString(4, projectId);
                psHistory.setInt(5, historyId);
                psHistory.execute();

                count++;
                if (count % 100 == 0) {
                    System.out.println(count + " histories updated so far.");
                }
            }
            System.out.println("Totally " + count + " histories updated.");
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
    }
}
