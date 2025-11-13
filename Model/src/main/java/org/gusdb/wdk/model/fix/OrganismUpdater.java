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
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrganismUpdater {

  private static final String PARAM_ORGANISM[] = { "organism", "BlastDatabaseOrganism", "motif_organism",
      "text_search_organism", "organismSinglePick" };
  private static final int lenParamOrg = PARAM_ORGANISM.length;
  private static final Logger logger = Logger.getLogger(OrganismUpdater.class);

  public static void main(String[] args) {

    // the format of the mapping file is:
    // old_name=new_name
    // one for each line
    if (args.length != 2) {
      System.err.println("Usage: organismUpdater <project_id> <map_file>\nPlease enter one project at a time.");
      System.exit(1);
    }

    try (WdkModel wdkModel = WdkModel.construct(args[0], GusHome.getGusHome())) {
      new OrganismUpdater(wdkModel, args[1]).update();
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

  public OrganismUpdater(WdkModel wdkModel, String mapFile) throws IOException, SQLException {
    _wdkModel = wdkModel;
    _projectId = _wdkModel.getProjectId();
    _userSchema = _wdkModel.getModelConfig().getUserDB().getUserSchema();
    _mappings = loadMapFile(mapFile);
    _stepLogger = new UpdatedStepLogger(_wdkModel);
    logger.debug("\n" + _mappings + "\n\n\n");
  }

  private static Map<String, String> loadMapFile(String fileName) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))) {
      Map<String, String> mappings = new HashMap<String, String>();
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0)
          continue;
        String[] parts = line.split("=", 2);
        mappings.put(parts[0].trim(), parts[1].trim());
      }
      return mappings;
    }
  }

  public void update() throws SQLException, JSONException {
    updateStepParams();
  }

  private void updateStepParams() throws SQLException, JSONException {
    logger.info("Checking step params...");

    DatabaseInstance database = _wdkModel.getUserDb();
    DBPlatform platform = database.getPlatform();
    DataSource dataSource = database.getDataSource();
    PreparedStatement psSelect = null, psUpdate = null;
    ResultSet resultSet = null;
    String select = "SELECT s.step_id, s.display_params            " + " FROM " + _userSchema + "users u, " +
        _userSchema + "steps s" + " WHERE u.is_guest = 0 AND u.user_id = s.user_id " +
        "   AND s.project_id = ?";
    String update = "UPDATE " + _userSchema + "steps " + " SET display_params = ? WHERE step_id = ?";
    try {
      psSelect = SqlUtils.getPreparedStatement(dataSource, select, SqlUtils.Autocommit.OFF);
      psUpdate = SqlUtils.getPreparedStatement(dataSource, update, SqlUtils.Autocommit.ON);
      psSelect.setString(1, _projectId);
      resultSet = psSelect.executeQuery();
      int count = 0;
      int stepCount = 0;
      while (resultSet.next()) {
        stepCount++;
        if (stepCount % 1000 == 0) {
          logger.debug(stepCount + " steps read");
        }

        int stepId = resultSet.getInt("step_id");
        String content = platform.getClobData(resultSet, "display_params");
        if (content == null || content.trim().length() == 0)
          continue;
        if (content.replaceAll("\\s", "").equals("{}"))
          continue;

        JSONObject jsParams = new JSONObject(content);
        if (jsParams.has("params")) 
          jsParams = jsParams.getJSONObject("params");

        if (jsParams!=null && changeParams(jsParams)) {
          content = JsonUtil.serialize(jsParams);
          platform.setClobData(psUpdate, 1, content, false);
          psUpdate.setInt(2, stepId);
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
    }
    catch (SQLException ex) {
      logger.error(ex);
      throw ex;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, psSelect);
      SqlUtils.closeStatement(psUpdate);
    }
  }

  private boolean changeParams(JSONObject jsParams) throws JSONException {
    boolean updated = false;
    for (String name : JsonUtil.getKeys(jsParams)) {
      for (int i = 0; i < lenParamOrg; i++) {
        if (name.equals(PARAM_ORGANISM[i])) {
          String organisms = jsParams.getString(name);
          JSONArray newOrgs = new JSONArray();
          for (String organism : AbstractEnumParam.convertToTerms(organisms)) {
            // logger.debug("Organism found: --" + organism + "\n\n");
            if (_mappings.containsKey(organism)) {
              logger.debug("FOUND param organism uncompressed with value that needs update...");
              organism = _mappings.get(organism);
              updated = true;
            }
            newOrgs.put(organism);
          }
          jsParams.put(name, newOrgs.toString());
        }
      }
    }
    return updated;
  }
}
