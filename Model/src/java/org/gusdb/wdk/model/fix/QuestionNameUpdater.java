package org.gusdb.wdk.model.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;

public class QuestionNameUpdater {

  private static final Logger logger = Logger.getLogger(QuestionNameUpdater.class);

  public static void main(String[] args) throws WdkModelException,
      SQLException, IOException {

    // the format of the mapping file is:
    // old_name=new_name
    // one for each line
    if (args.length != 2) {
      System.err.println("Usage: questionNameUpdater <project_id> <map_file>");
      System.exit(-1);
    }

    QuestionNameUpdater updater = new QuestionNameUpdater(args[0], args[1]);
    updater.update();
  }

  private final String projectId;
  private final WdkModel wdkModel;
  private final String userSchema;
  // private final String wdkSchema;
  private final Map<String, String> mappings;
  private final UpdatedStepLogger stepLogger;

  public QuestionNameUpdater(String projectId, String mapFile)
      throws WdkModelException, IOException, SQLException {
    this.projectId = projectId;
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    wdkModel = WdkModel.construct(projectId, gusHome);
    ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
    userSchema = userDB.getUserSchema();
    // wdkSchema = userDB.getWdkEngineSchema();
    mappings = loadMapFile(mapFile);
    stepLogger = new UpdatedStepLogger(wdkModel);
  }

  private Map<String, String> loadMapFile(String fileName) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(
        new File(fileName)));
    Map<String, String> mappings = new HashMap<String, String>();
    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0)
        continue;
      String[] parts = line.split("=", 2);
      mappings.put(parts[0].trim(), parts[1].trim());
    }
    reader.close();
    return mappings;
  }

  public void update() throws SQLException {
    updateQuestionNames();
  }

  private void updateQuestionNames() throws SQLException {
    logger.info("Checking question names...");

    DatabaseInstance userDb = wdkModel.getUserDb();
    DataSource dataSource = userDb.getDataSource();
    PreparedStatement psSelect = null, psUpdate = null;
    ResultSet resultSet = null;
    String select = "SELECT s.step_id, s.question_name           " + " FROM "
        + userSchema + "users u, " + userSchema + "steps s "
        + " WHERE u.is_guest = 0 AND u.user_id = s.user_id "
        + "   AND  s.project_id = ?";
    // logger.info("SELECT:   " + select + "\n\n");
    String update = "UPDATE " + userSchema + "steps "
        + " SET question_name = ? WHERE step_id = ?";
    // logger.info("UPDATE:   " + update + "\n\n");

    try {
      psSelect = SqlUtils.getPreparedStatement(dataSource, select);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, update);
      psSelect.setString(1, projectId);
      logger.debug("SELECT:   " + psSelect + "\n\n");
      resultSet = psSelect.executeQuery();
      int count = 0;
      int stepCount = 0;
      while (resultSet.next()) {
        stepCount++;
        if (stepCount % 1000 == 0) {
          logger.debug(stepCount + " steps read");
        }

        int stepId = resultSet.getInt("step_id");
        String content = resultSet.getString("question_name");
        if (content == null || content.trim().length() == 0)
          continue;
        // if (content.replaceAll("\\s", "").equals("{}")) continue;

        if (mappings.containsKey(content)) {
          logger.info("old question:" + content + "\n");
          content = mappings.get(content);
          logger.info("new question:" + content + "\n\n");
          // platform.setClobData(psUpdate, 1, content, false);
          psUpdate.setString(1, content);
          psUpdate.setInt(2, stepId);
          logger.debug("UPDATE:   " + psUpdate + "\n\n");
          psUpdate.addBatch();
          stepLogger.logStep(stepId);
          count++;
          if (count % 100 == 0)
            psUpdate.executeBatch();
        }
      }
      if (count % 100 != 0)
        psUpdate.executeBatch();
      stepLogger.finish();
      logger.info("THE END:   " + count + " steps modified\n\n");
    } catch (SQLException ex) {
      logger.error(ex);
      throw ex;
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
      SqlUtils.closeStatement(psUpdate);
    }

  }

}
