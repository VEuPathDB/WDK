package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wsf.util.BaseCLI;

/**
 * @author xingao
 * 
 *         this code extract the param values from steps, and put them into
 *         separate row. The result will be used to expedite the step
 *         validation.
 */
public class StepParamExpander extends BaseCLI {

  private static final Logger logger = Logger.getLogger(StepParamExpander.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    StepParamExpander expender = new StepParamExpander(cmdName);
    try {
      expender.invoke(args);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    } finally {
      logger.info("step params expanded.");
      System.exit(0);
    }
  }

  /**
   * @param command
   * @param description
   */
  protected StepParamExpander(String command) {
    super((command != null) ? command : "stepParamExpander",
        "expand the param clob into its own rows in step_params table");
  }

  public void expand(WdkModel wdkModel) throws SQLException, WdkModelException {
    Connection connection = wdkModel.getUserDb().getDataSource().getConnection();
    ResultSet resultSet = null;
    PreparedStatement psInsert = null;
    try {
      createParamTable(wdkModel);

      connection.setAutoCommit(false);
      resultSet = prepareSelect(wdkModel, connection);
      psInsert = prepareInsert(connection);
      DatabaseInstance database = wdkModel.getUserDb();

      int count = 0;
      while (resultSet.next()) {
        int stepId = resultSet.getInt("step_id");
        String clob = database.getPlatform().getClobData(resultSet,
            "display_params");

        if (clob == null)
          continue;
        clob = clob.trim();
        if (!clob.startsWith("{"))
          continue;

        List<String[]> values = parseClob(wdkModel, clob);

        // insert the values
        for (String[] pair : values) {
          String paramName = pair[0].trim();
          String paramValue = pair[1].trim();

          psInsert.setInt(1, stepId);
          psInsert.setString(2, paramName);
          psInsert.setString(3, paramValue);
          psInsert.addBatch();
        }
        psInsert.executeBatch();
        connection.commit();

        count++;
        if (count % 100 == 0) {
          logger.debug(count + " steps processed.");
        }
      }
      logger.info("Totally processed " + count + " steps.");
    } catch (SQLException | WdkModelException ex) {
      connection.rollback();
      throw ex;
    } finally {
      if (resultSet != null) {
        resultSet.getStatement().close();
        resultSet.close();
      }
      if (psInsert != null)
        psInsert.close();
      connection.setAutoCommit(true);
      connection.close();
    }
  }

  private void createParamTable(WdkModel wdkModel) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();

    // check if table exists
    if (database.getPlatform().checkTableExists(dataSource,
        database.getDefaultSchema(), "step_params"))
      return;

    SqlUtils.executeUpdate(dataSource, "CREATE TABLE step_params ("
        + " step_id NUMBER(12) NOT NULL, "
        + " param_name VARCHAR(200) NOT NULL, "
        + " param_value VARCHAR(4000), migration NUMBER(12))",
        "wdk-create-param-table");

    SqlUtils.executeUpdate(dataSource, "CREATE UNIQUE INDEX step_params_ux01 "
        + "ON step_params (step_id, param_name, param_value)",
        "wdk-create-param-indx");

    SqlUtils.executeUpdate(dataSource, "CREATE INDEX step_params_ix01 "
        + "ON step_params (step_id)",
        "wdk-create-param-indx");
  }

  private ResultSet prepareSelect(WdkModel wdkModel, Connection connection)
      throws SQLException {
    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    String schema = userDB.getUserSchema();
    StringBuffer sql = new StringBuffer("SELECT s.step_id, s.display_params ");
    sql.append(" FROM " + schema + "steps s, " + schema + "users u ");
    sql.append(" WHERE s.user_id = u.user_id AND u.is_guest = 0");
    sql.append("   AND s.step_id NOT IN (SELECT step_id FROM step_params) ");

    return SqlUtils.executeQuery(connection, sql.toString(),
        "wdk-select-step-params", 1000);
  }

  private PreparedStatement prepareInsert(Connection connection)
      throws SQLException {
    StringBuffer sql = new StringBuffer("INSERT INTO step_params ");
    sql.append(" (step_id, param_name, param_value) " + "  VALUES (?, ?, ?)");

    return connection.prepareStatement(sql.toString());
  }

  private List<String[]> parseClob(WdkModel wdkModel, String clob)
      throws WdkModelException {
    StepFactory stepFactory = wdkModel.getStepFactory();
    Map<String, String> values = stepFactory.parseParamContent(clob);
    List<String[]> newValues = new ArrayList<String[]>();
    for (String paramName : values.keySet()) {
      String value = values.get(paramName);
      String[] terms = value.split(",");
      Set<String> used = new HashSet<>();
      for (String term : terms) {
        if (term.length() > 4000)
          term = term.substring(0, 4000);
        if (used.contains(term)) {
          continue;
        } else {
          newValues.add(new String[] { paramName, term });
          used.add(term);
        }
      }
    }
    return newValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.util.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "ProjectId, which"
        + " should match the directory name under $GUS_HOME, where"
        + " model-config.xml is stored.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wsf.util.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);

    String projectId = (String) getOptionValue(ARG_PROJECT_ID);

    WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

    // expand step params
    logger.info("Expanding params...");
    expand(wdkModel);
  }
}
