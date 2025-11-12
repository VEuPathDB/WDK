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
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;

public class QuestionNameUpdater {

  private static final Logger logger = Logger.getLogger(QuestionNameUpdater.class);

  public static void main(String[] args)  {

    // the format of the mapping file is:
    // old_name=new_name
    // one for each line
    if (args.length != 2) {
      System.err.println("Usage: questionNameUpdater <project_id> <map_file>");
      System.exit(1);
    }

    try (WdkModel wdkModel = WdkModel.construct(args[0], GusHome.getGusHome())) {
      new QuestionNameUpdater(wdkModel, args[1]).update();
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private final String _projectId;
  private final WdkModel _wdkModel;
  private final String _userSchema;
  private final Map<String, String> _mappings;
  private final UpdatedStepLogger _stepLogger;

  public QuestionNameUpdater(WdkModel wdkModel, String mapFile) throws IOException, SQLException {
    _wdkModel = wdkModel;
    _projectId = _wdkModel.getProjectId();
    _userSchema = _wdkModel.getModelConfig().getUserDB().getUserSchema();
    _mappings = loadMapFile(mapFile);
    _stepLogger = new UpdatedStepLogger(_wdkModel);
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

    DatabaseInstance userDb = _wdkModel.getUserDb();
    DataSource dataSource = userDb.getDataSource();
    PreparedStatement psSelect = null, psUpdate = null;
    ResultSet resultSet = null;
    String select = "SELECT s.step_id, s.question_name           " + " FROM "
        + _userSchema + "users u, " + _userSchema + "steps s "
        + " WHERE u.is_guest = 0 AND u.user_id = s.user_id "
        + "   AND  s.project_id = ?";
    // logger.info("SELECT:   " + select + "\n\n");
    String update = "UPDATE " + _userSchema + "steps "
        + " SET question_name = ? WHERE step_id = ?";
    // logger.info("UPDATE:   " + update + "\n\n");

    try {
      psSelect = SqlUtils.getPreparedStatement(dataSource, select, SqlUtils.Autocommit.OFF);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, update, SqlUtils.Autocommit.ON);
      psSelect.setString(1, _projectId);
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

        if (_mappings.containsKey(content)) {
          logger.info("old question:" + content + "\n");
          content = _mappings.get(content);
          logger.info("new question:" + content + "\n\n");
          // platform.setClobData(psUpdate, 1, content, false);
          psUpdate.setString(1, content);
          psUpdate.setInt(2, stepId);
          logger.debug("UPDATE:   " + psUpdate + "\n\n");
          psUpdate.addBatch();
          _stepLogger.logStep(stepId);
          count++;
          if (count % 100 == 0)
            psUpdate.executeBatch();
        }
      }
      if (count % 100 != 0)
        psUpdate.executeBatch();
      _stepLogger.finish();
      logger.info("THE END:   " + count + " steps modified\n\n");
    } catch (SQLException ex) {
      logger.error(ex);
      throw ex;
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet, psSelect);
      SqlUtils.closeStatement(psUpdate);
    }
  }
}
