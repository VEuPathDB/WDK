/**
 * @description
 */
package org.gusdb.wdk.model.migrate;

import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author Jerric
 * @created May 22, 2007
 * @modified May 22, 2007
 */
public class Migrator1_12To1_13 implements Migrator {

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
     *          the checksum to set
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
     *          the historyId to set
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
     *          the params to set
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
     *          the projectId to set
     */
    void setProjectId(String projectId) {
      this.projectId = projectId;
    }

    /**
     * @return the historyId
     */
    int getUserId() {
      return this.userId;
    }

    /**
     * @param historyId
     *          the historyId to set
     */
    void setUserId(int userId) {
      this.userId = userId;
    }

  }

  /**
   * (non-Javadoc)
   * 
   * @throws SQLException
   * @throws NoSuchAlgorithmException
   * 
   * @see org.gusdb.wdk.model.migrate.Migrator#migrate()
   */
  @Override
  public void migrate(WdkModel wdkModel, CommandLine commandLine)
      throws WdkModelException, WdkUserException, NoSuchAlgorithmException,
      SQLException {
    // update histories
    DBPlatform platform = wdkModel.getUserDb().getPlatform();
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    String newSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    List<HistoryItem> histories = new ArrayList<HistoryItem>();

    ResultSet rsHistory = null;
    try {
      rsHistory = SqlUtils.executeQuery(dataSource, "SELECT "
          + "user_id, project_id, history_id, params FROM " + newSchema
          + "histories WHERE query_instance_checksum IS " + "NULL",
          "wdk-migrate-select-history");

      while (rsHistory.next()) {
        int userId = rsHistory.getInt("user_id");
        String projectId = rsHistory.getString("project_id");
        int historyId = rsHistory.getInt("history_id");
        String params = platform.getClobData(rsHistory, "params");

        // params = params.replaceAll("--WDK_PARAM_DIVIDER--",
        // Utilities.DATA_DIVIDER);
        // String content = projectId + Utilities.DATA_DIVIDER + params;
        String content = projectId + "--WDK_PARAM_DIVIDER--" + params;
        String checksum = Utilities.encrypt(content);

        HistoryItem item = new HistoryItem();
        item.setChecksum(checksum);
        item.setHistoryId(historyId);
        item.setParams(params);
        item.setProjectId(projectId);
        item.setUserId(userId);
        histories.add(item);
      }
    } finally {
      SqlUtils.closeResultSetAndStatement(rsHistory);
    }

    PreparedStatement psHistory = null;
    try {
      psHistory = SqlUtils.getPreparedStatement(dataSource, "UPDATE "
          + newSchema + "histories SET query_instance_checksum = ? "
          + ", params = ? WHERE user_id = ? AND project_id = ? "
          + "AND history_id = ?");

      int count = 0;
      for (HistoryItem item : histories) {
        psHistory.setString(1, item.getChecksum());
        platform.setClobData(psHistory, 2, item.getParams(), false);
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
      SqlUtils.closeStatement(psHistory);
    }
  }

  @Override
  public void declareOptions(Options options) {
    Option option = new Option("model", true,
        "the name of the model.  This is used to find the Model XML "
            + "file ($GUS_HOME/config/model_name.xml) the Model "
            + "property file ($GUS_HOME/config/model_name.prop) "
            + "and the Model config file "
            + "($GUS_HOME/config/model_name-config.xml)");
    option.setRequired(true);
    option.setArgName("model");
    options.addOption(option);
  }
}
