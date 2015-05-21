package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.StepFactory;

/**
 * @author xingao
 * 
 *         This code is revived to fill in the strategy id on the steps;
 * 
 *         The original code generates the signature for the strategy (the old system doesn't create the
 *         signature automatically, but the new one does. Therefore, this code is considered deprecated.
 * 
 */
public class StrategyFixer extends BaseCLI {

  private static final int BATCH_SIZE = 1000;

  private static final Logger LOG = Logger.getLogger(StrategyFixer.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    StrategyFixer fixer = new StrategyFixer(cmdName);
    try {
      fixer.invoke(args);
    }
    finally {
      LOG.info("strategy fixer finished.");
      System.exit(0);
    }
  }

  /**
   * @param command
   * @param description
   */
  protected StrategyFixer(String command) {
    super((command == null) ? command : "strategyFixer", "Fill in the strategy id on the steps. "
        + "The original code generate unique strategy signature, and is commented out.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "A project id, which should match the directory name "
        + " under $GUS_HOME, where model-config.xml is stored. Only one project is needed to provide " +
        " access to apicomm instance, and all the steps from all projects will be patched.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
      WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

      fillInStrategyId(wdkModel);
      // generateSignatures(wdkModel);
  }

  /**
   * fill the strategy_id on the steps that belong to any existing strategies. new steps created in the
   * context of strategies will automatically have the strategy_id assigned.
   * 
   * Note: WDK still creates some steps outside of the strategy, and those steps will have NULL for the
   * strategy_id column.
   * 
   * @param model
   * @throws SQLException
   */
  private void fillInStrategyId(WdkModel model) throws SQLException {
    LOG.info("Filling strategy_id into steps...");
    String schema = model.getModelConfig().getUserDB().getUserSchema();
    DataSource dataSource = model.getUserDb().getDataSource();

    PreparedStatement psUpdate = null, psChildren = null;
    ResultSet rsRoots = null, rsChildren = null;
    try {
      psUpdate = SqlUtils.getPreparedStatement(dataSource, "UPDATE " + schema + "steps SET strategy_id = ? " +
          " WHERE step_id = ?");

      // update the strategy id for root steps;
      LOG.info("updating root steps...");
      rsRoots = SqlUtils.executeQuery(dataSource, "SELECT sr.strategy_id, sr.root_step_id         " +
          " FROM " + schema + "strategies sr, " + schema + "users u, " + schema + "steps st" +
          " WHERE sr.user_id = u.user_id AND sr.root_step_id = st.step_id " +
          "   AND sr.is_deleted = 0 AND u.is_guest = 0 AND st.strategy_id IS NULL", "select-roots");
      int count = 0;
      while (rsRoots.next()) {
        psUpdate.setInt(1, rsRoots.getInt("strategy_id"));
        psUpdate.setInt(2, rsRoots.getInt("root_step_id"));
        psUpdate.addBatch();
        count++;
        if (count % BATCH_SIZE == 0) {
          psUpdate.executeBatch();
          LOG.info(count + " steps updated.");
        }
      }

      // recursively update all the depended steps
      LOG.info("recursively updating children steps...");
      String sql = "SELECT s.step_id, p.strategy_id FROM " + schema + "steps s, " + schema + "steps p " +
          " WHERE s.strategy_id IS NULL and p.strategy_id IS NOT NULL ";
      psChildren = SqlUtils.getPreparedStatement(dataSource, sql + " AND s.step_id = p.left_child_id " +
          " UNION " + sql + " AND s.step_id = p.right_child_id");
      int prev;
      do {
        prev = count;
        rsChildren = psChildren.executeQuery();
        while (rsChildren.next()) {
          psUpdate.setInt(1, rsChildren.getInt("strategy_id"));
          psUpdate.setInt(2, rsChildren.getInt("step_id"));
          psUpdate.addBatch();
          count++;
          if (count % BATCH_SIZE == 0) {
            psUpdate.executeBatch();
            LOG.info(count + " steps updated.");
          }
        }
      }
      while (prev != count);

      if (count % BATCH_SIZE != 0)
        psUpdate.executeBatch();
      LOG.info("Total" + count + " steps updated.");
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rsChildren);
      SqlUtils.closeResultSetAndStatement(rsRoots);
      SqlUtils.closeStatement(psUpdate);
    }

  }

  /**
   * This method is no longer needed, since all new strategies will have a signature generated on creation
   * time, and all existing ones are already patched.
   * 
   * @param wdkModel
   * @throws SQLException
   * @throws WdkModelException
   */
  @Deprecated
  private void generateSignatures(WdkModel wdkModel) throws SQLException, WdkModelException {
    String schema = wdkModel.getModelConfig().getUserDB().getUserSchema();

    StringBuffer sqlSelect = new StringBuffer("SELECT strategy_id, user_id ");
    sqlSelect.append("FROM ").append(schema).append("strategies ");
    sqlSelect.append("WHERE signature IS NULL AND project_id = ?");

    StringBuffer sqlUpdate = new StringBuffer("UPDATE ");
    sqlUpdate.append(schema).append("strategies SET signature = ? ");
    sqlUpdate.append("WHERE strategy_id = ?");

    StepFactory factory = wdkModel.getStepFactory();
    ResultSet resultSet = null;
    PreparedStatement psSelect = null, psUpdate = null;
    DataSource src = wdkModel.getUserDb().getDataSource();
    try {
      psSelect = SqlUtils.getPreparedStatement(src, sqlSelect.toString());
      psUpdate = SqlUtils.getPreparedStatement(src, sqlUpdate.toString());

      psSelect.setString(1, wdkModel.getProjectId());
      resultSet = psSelect.executeQuery();
      while (resultSet.next()) {
        int strategyId = resultSet.getInt("strategy_id");
        int userId = resultSet.getInt("user_id");
        String sig = factory.getStrategySignature(userId, strategyId);

        psUpdate.setString(1, sig);
        psUpdate.setInt(2, strategyId);
        psUpdate.executeUpdate();
      }
    }
    finally {
      SqlUtils.closeStatement(psUpdate);
      SqlUtils.closeStatement(psSelect);
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }
}
