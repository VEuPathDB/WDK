package org.gusdb.wdk.model.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfigUserDB;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionNameUpdater {

    private static final Logger logger = Logger.getLogger(OrganismUpdater.class);

    /**
     * @param args
     * @throws WdkModelException
     * @throws SQLException
     * @throws IOException
     * @throws JSONException
     */
    public static void main(String[] args) throws WdkModelException,
            SQLException, IOException, JSONException {

				//the format of the mapping file is:
				//       old_name=new_name
				//one for each line
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
    private final String wdkSchema;
    private final Map<String, String> mappings;

    public QuestionNameUpdater(String projectId, String mapFile)
            throws WdkModelException, IOException {
        this.projectId = projectId;
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        wdkModel = WdkModel.construct(projectId, gusHome);
        ModelConfigUserDB userDB = wdkModel.getModelConfig().getUserDB();
        userSchema = userDB.getUserSchema();
        wdkSchema = userDB.getWdkEngineSchema();
        mappings = loadMapFile(mapFile);
    }

    private Map<String, String> loadMapFile(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(
                fileName)));
        Map<String, String> mappings = new HashMap<String, String>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) continue;
            String[] parts = line.split("=", 2);
            mappings.put(parts[0].trim(), parts[1].trim());
        }
        reader.close();
        return mappings;
    }

    public void update() throws SQLException, JSONException, WdkModelException {
        Set<String> clobKeys = new HashSet<String>();
        updateQuestionNames(clobKeys);
    }

    private void updateQuestionNames(Set<String> clobKeys) throws SQLException,
            JSONException, WdkModelException {
        logger.info("Checking question names...");

        DBPlatform platform = wdkModel.getUserPlatform();
        DataSource dataSource = platform.getDataSource();
        PreparedStatement psSelect = null, psUpdate = null;
        ResultSet resultSet = null;
        String select = "SELECT a.question_name           "
                + " FROM " + userSchema + "users u, " + userSchema
                + "steps s, " + wdkSchema + "answers a "
                + " WHERE u.is_guest = 0 AND u.user_id = s.user_id "
                + "   AND s.answer_id = a.answer_id AND a.project_id = ?";
				logger.info("SELECT:   " + select + "\n\n");
        String update = "UPDATE " + wdkSchema + "answers "
                + " SET question_name = ? WHERE answer_id = ?";
				logger.info("UPDATE:   " + update + "\n\n");

				/*
        try {
            psSelect = SqlUtils.getPreparedStatement(dataSource, select);
            psUpdate = SqlUtils.getPreparedStatement(dataSource, update);
            psSelect.setString(1, projectId);
            resultSet = psSelect.executeQuery();
            int count = 0;
            int stepCount = 0;
            while (resultSet.next()) {
                stepCount++;
                if (stepCount % 1000 == 0) {
                    logger.debug(stepCount + " steps read");
                }

                int stepId = resultSet.getInt("step_id");
                String content = platform.getClobData(resultSet,
                        "display_params");
                if (content == null || content.trim().length() == 0) continue;
                if (content.replaceAll("\\s", "").equals("{}")) continue;
                
                JSONObject jsParams = new JSONObject(content);
                if (changeParams(jsParams, clobKeys)) {
                    content = jsParams.toString();
                    platform.setClobData(psUpdate, 1, content, false);
                    psUpdate.setInt(2, stepId);
                    psUpdate.addBatch();
                    count++;
                    if (count % 100 == 0) psUpdate.executeBatch();
                }
                if (count % 100 != 0) psUpdate.executeBatch();
            }
        }
        catch (SQLException ex) {
            logger.error(ex);
            throw ex;
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
            SqlUtils.closeStatement(psUpdate);
        }
				*/
    }

    private boolean changeParams(JSONObject jsParams, Set<String> clobKeys)
            throws JSONException {
        boolean updated = false;
        for (String name : JSONObject.getNames(jsParams)) {
            if (name.equals(PARAM_ORGANISM)) {
                String organisms = jsParams.getString(name);
                if (organisms.startsWith("[C]")) { // compressed values
                    String clobKey = organisms.substring(3);
                    clobKeys.add(clobKey);
                } else { // uncompressed values
                    StringBuilder buffer = new StringBuilder();
                    for (String organism : organisms.split("\\s*,\\s*")) {
                        if (mappings.containsKey(organism)) {
                            organism = mappings.get(organism);
                            updated = true;
                        }
                        if (buffer.length() > 0) buffer.append(',');
                        buffer.append(organism);
                    }
                    jsParams.put(name, buffer.toString());
                }
            }
        }
        return updated;
    }

 
