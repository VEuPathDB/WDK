package org.gusdb.wdk.model.fix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParam;
import org.gusdb.wdk.model.query.param.FilterParamHandler;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         this code extract the param values from steps, and put them into separate row. The result will be
 *         used to expedite the step validation.
 */
public class StepParamExpander extends BaseCLI {

  private static final Logger logger = Logger.getLogger(StepParamExpander.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    StepParamExpander expender = new StepParamExpander(cmdName);
    try {
      expender.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
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
    String userSchema = wdkModel.getModelConfig().getUserDB().getUserSchema();
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource ds = database.getDataSource();
    Connection connection = null;
    Statement selectStmt = null;
    ResultSet resultSet = null;
    PreparedStatement psInsert = null;
    try {
      createParamTable(wdkModel);
      connection = ds.getConnection();
      connection.setAutoCommit(false);

      // create select SQL
      String sql = new StringBuilder("SELECT s.step_id, s.question_name, s.display_params ").append(" FROM ").append(
          userSchema).append("steps s, ").append(userSchema).append("users u ").append(
          " WHERE s.user_id = u.user_id AND u.is_guest = 0").append(
          "   AND s.step_id NOT IN (SELECT step_id FROM step_params) ").toString();

      selectStmt = connection.createStatement();
      selectStmt.setFetchSize(1000);
      long start = System.currentTimeMillis();
      resultSet = selectStmt.executeQuery(sql);
      QueryLogger.logEndStatementExecution(sql, "wdk-select-step-params", start);
      psInsert = prepareInsert(connection);

      int count = 0;
      while (resultSet.next()) {

        int stepId = resultSet.getInt("step_id");
        String questionName = resultSet.getString("question_name");
        String clob = database.getPlatform().getClobData(resultSet, "display_params");

        if (clob == null)
          continue;
        clob = clob.trim();
        if (!clob.startsWith("{"))
          continue;
				//logger.debug("***** MADE IT HERE : ****:  Step ID:" + stepId + ", questionName: " + questionName + ", clob is: " + clob);
        Map<String, Set<String>> values = parseClob(wdkModel, questionName, clob);

        // insert the values
        for (String paramName : values.keySet()) {
          Set<String> paramValues = values.get(paramName);
          for (String paramValue : paramValues) {
            psInsert.setInt(1, stepId);
            psInsert.setString(2, paramName);
            psInsert.setString(3, paramValue);
            psInsert.addBatch();
          }
        }
        psInsert.executeBatch();
        connection.commit();

        count++;
        if (count % 100 == 0) {
          logger.debug(count + " steps processed.");
        }
      }
      logger.info("Totally processed " + count + " steps.");
    }
    catch (SQLException | WdkModelException | JSONException ex) {
      connection.rollback();
      throw new WdkModelException(ex);
    }
    finally {
      if (connection != null)
        connection.setAutoCommit(true);
      SqlUtils.closeQuietly(psInsert, resultSet, selectStmt, connection);
    }
  }

  private void createParamTable(WdkModel wdkModel) throws SQLException {
    DatabaseInstance database = wdkModel.getUserDb();
    DataSource dataSource = database.getDataSource();

    // check if table exists
    if (database.getPlatform().checkTableExists(dataSource, database.getDefaultSchema(), "step_params"))
      return;

    SqlUtils.executeUpdate(dataSource, "CREATE TABLE step_params (" + " step_id NUMBER(12) NOT NULL, "
        + " param_name VARCHAR(200) NOT NULL, " + " param_value VARCHAR(4000), migration NUMBER(12))",
        "wdk-create-param-table");

    SqlUtils.executeUpdate(dataSource, "CREATE UNIQUE INDEX step_params_ux01 "
        + "ON step_params (step_id, param_name, param_value)", "wdk-create-param-indx");

    SqlUtils.executeUpdate(dataSource, "CREATE INDEX step_params_ix01 " + "ON step_params (step_id)",
        "wdk-create-param-indx");
  }

  private PreparedStatement prepareInsert(Connection connection) throws SQLException {
    StringBuffer sql = new StringBuffer("INSERT INTO step_params ");
    sql.append(" (step_id, param_name, param_value) " + "  VALUES (?, ?, ?)");

    return connection.prepareStatement(sql.toString());
  }

  private Map<String, Set<String>> parseClob(WdkModel wdkModel, String questionName, String clob)
      throws WdkModelException, JSONException {
    Map<String, Set<String>> newValues = new LinkedHashMap<>();
    if (clob != null && clob.length() > 0) {
      // create a temp step to process the json and extract param values.
      Step step = new Step(wdkModel.getStepFactory(), 0, 0);
      step.setInMemoryOnly(true);
      step.setParamFilterJSON(new JSONObject(clob));
      Map<String, String> values = step.getParamValues();

      for (String paramName : values.keySet()) {
        String value = values.get(paramName);
        String[] terms;
        if (isFilterParam(wdkModel, questionName, paramName)) {
          JSONObject jsValue = new JSONObject(value);
          JSONArray jsTerms = jsValue.getJSONArray(FilterParamHandler.TERMS_KEY);
          terms = new String[jsTerms.length()];
          for (int i = 0; i < terms.length; i++) {
            terms[i] = jsTerms.getString(i);
          }
        }
        else {
          terms = value.split(",");
        }
        Set<String> used = new HashSet<>();
        for (String term : terms) {
          if (term.length() > 4000)
            term = term.substring(0, 4000);
          used.add(term);
        }
        newValues.put(paramName.trim(), used);
      }
    }
    return newValues;
  }

  private boolean isFilterParam(WdkModel wdkModel, String questionName, String paramName) {
    try {
      Question question = wdkModel.getQuestion(questionName);
      Map<String, Param> params = question.getParamMap();
      Param param = params.get(paramName);
      return (param != null && param instanceof FilterParam);
    }
    catch (WdkModelException ex) {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#declareOptions()
   */
  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "ProjectId, which"
        + " should match the directory name under $GUS_HOME, where" + " model-config.xml is stored.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.fgputil.BaseCLI#execute()
   */
  @Override
  protected void execute() throws Exception {
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    WdkModel wdkModel = null;
    try {
      wdkModel = WdkModel.construct(projectId, GusHome.getGusHome());
      // expand step params
      logger.info("Expanding params...");
      expand(wdkModel);
    }
    finally {
      if (wdkModel != null) wdkModel.releaseResources();
    }
  }
}
