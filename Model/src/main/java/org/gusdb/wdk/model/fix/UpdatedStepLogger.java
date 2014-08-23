package org.gusdb.wdk.model.fix;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;

public class UpdatedStepLogger {

  private static final String TABLE_UPDATED_STEPS = "wdk_updated_steps";

  private static final Logger logger = Logger.getLogger(UpdatedStepLogger.class);

  private final WdkModel wdkModel;
  private final Set<Integer> steps;

  private int count;
  private PreparedStatement psInsert;

  public UpdatedStepLogger(WdkModel wdkModel) throws SQLException {
    this.wdkModel = wdkModel;
    this.steps = new HashSet<>();

    checkTable();
    loadSteps();

    count = 0;
    DataSource dataSource = wdkModel.getUserDb().getDataSource();
    psInsert = SqlUtils.getPreparedStatement(dataSource, "INSERT INTO "
        + TABLE_UPDATED_STEPS + " (step_id) VALUES (?)");
  }

  private void checkTable() throws DBStateException, SQLException {
    // check log table, and create it if it doesn't exist
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();
    if (!database.getPlatform().checkTableExists(dataSource,
        database.getDefaultSchema(), TABLE_UPDATED_STEPS)) {
      SqlUtils.executeUpdate(dataSource, "CREATE TABLE " + TABLE_UPDATED_STEPS
          + " (" + " step_id NUMBER(12) NOT NULL)",
          "organism-updater-create-log-table");
    }
  }

  private Set<Integer> loadSteps() throws SQLException {
    DataSource dataSource = wdkModel.getUserDb().getDataSource();

    // load previously logged steps
    Set<Integer> steps = new HashSet<>();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, "SELECT step_id FROM "
          + TABLE_UPDATED_STEPS, "organism-updater-select-log", 5000);
      while (resultSet.next()) {
        steps.add(resultSet.getInt("step_id"));
      }
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    logger.info(steps.size() + " steps are previously logged.");
    return steps;
  }

  public void logStep(int stepId) throws SQLException {
    if (steps.contains(stepId))
      return;
    psInsert.setInt(1, stepId);
    psInsert.addBatch();
    steps.add(stepId);
    count++;
    if (count % 100 == 0)
      psInsert.executeBatch();
  }

  public void finish() throws SQLException {
    psInsert.executeBatch();
    logger.info("Totally " + steps.size() + " steps are logged.");
    SqlUtils.closeStatement(psInsert);
  }
}
