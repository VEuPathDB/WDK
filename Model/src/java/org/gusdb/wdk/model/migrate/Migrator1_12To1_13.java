/**
 * @description
 */
package org.gusdb.wdk.model.migrate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    private class HistoryItem {

        private int userId;
        private String projectId;
        private int historyId;
        private String params;
        private String checksum;

        /**
         * @return the checksum
         */
        String getChecksum() {
            return this.checksum;
        }

        /**
         * @param checksum
         *            the checksum to set
         */
        void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        /**
         * @return the historyId
         */
        int getHistoryId() {
            return this.historyId;
        }

        /**
         * @param historyId
         *            the historyId to set
         */
        void setHistoryId(int historyId) {
            this.historyId = historyId;
        }

        /**
         * @return the params
         */
        String getParams() {
            return this.params;
        }

        /**
         * @param params
         *            the params to set
         */
        void setParams(String params) {
            this.params = params;
        }

        /**
         * @return the projectId
         */
        String getProjectId() {
            return this.projectId;
        }

        /**
         * @param projectId
         *            the projectId to set
         */
        void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        /**
         * @return the userId
         */
        int getUserId() {
            return this.userId;
        }

        /**
         * @param userId
         *            the userId to set
         */
        void setUserId(int userId) {
            this.userId = userId;
        }

    }

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
        
        List<HistoryItem> histories = new ArrayList<HistoryItem>();

        ResultSet rsHistory = null;
        try {
            rsHistory = SqlUtils.getResultSet(dataSource, "SELECT user_id, "
                    + "project_id, history_id, params FROM " + newSchema
                    + "histories WHERE query_instance_checksum IS NULL");

            while (rsHistory.next()) {
                int userId = rsHistory.getInt("user_id");
                String projectId = rsHistory.getString("history_id");
                int historyId = rsHistory.getInt("user_id");
                String params = platform.getClobData(rsHistory, "params");

                params = params.replaceAll("--WDK_PARAM_DIVIDER--",
                        Utilities.DATA_DIVIDER);
                String content = projectId + Utilities.DATA_DIVIDER + params;
                String checksum = Utilities.encrypt(content);
                
                HistoryItem item = new HistoryItem();
                item.setChecksum(checksum);
                item.setHistoryId(historyId);
                item.setParams(params);
                item.setProjectId(projectId);
                item.setUserId(userId);
                histories.add(item);
            }
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsHistory);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
        
        PreparedStatement psHistory = null;
        try {
            psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE " + newSchema
                    + "histories SET query_instance_checksum = ? "
                    + ", params = ? WHERE user_id = ? AND project_id = ? "
                    + "AND history_id = ?");
            
            int count = 0;
            for (HistoryItem item : histories) {
                psHistory.setString(1, item.getChecksum());
                platform.updateClobData(psHistory, 2, item.getParams(), false);
                psHistory.setInt(3, item.getUserId());
                psHistory.setString(4, item.getProjectId());
                psHistory.setInt(5, item.getHistoryId());
                psHistory.addBatch();

                count++;
                if (count % 100 == 0) {
                    psHistory.executeBatch();
                }
            }
            psHistory.executeBatch();
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeStatement(psHistory);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
    }
}
