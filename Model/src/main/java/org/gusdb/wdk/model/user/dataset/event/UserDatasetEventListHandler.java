package org.gusdb.wdk.model.user.dataset.event;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.json.JSONArray;
import org.xml.sax.SAXException;

/**
 * A wrapper around the UserDatasetEventArrayHandler library to allow it to be
 * called via a command line interface.
 * @author crisl-adm
 *
 */
public class UserDatasetEventListHandler extends BaseCLI {

  protected static final String ARG_PROJECT = "project";
  protected static final String ARG_EVENTS_FILE = "eventsFile";

  private static final Logger logger = Logger.getLogger(UserDatasetEventListHandler.class);

  public UserDatasetEventListHandler(String command) {
    super(command, "Handle a list of user dataset events.");
  }

  
  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    UserDatasetEventListHandler handler = new UserDatasetEventListHandler(cmdName);
    try {
      handler.invoke(args);
      logger.info("done.");
      System.exit(0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  protected ModelConfig getModelConfig(String projectId) {
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    ModelConfigParser parser = new ModelConfigParser(gusHome);
    try {
      return parser.parseConfig(projectId);
    }
    catch(WdkModelException | SAXException | IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void execute() throws Exception {
	UserDatasetEventArrayHandler handler = new UserDatasetEventArrayHandler();
	String projectId = (String) getOptionValue(ARG_PROJECT);
    handler.setProjectId(projectId);
    ModelConfig modelConfig = getModelConfig(projectId);
    File eventFile = new File((String)getOptionValue(ARG_EVENTS_FILE));
    JSONArray eventJsonArray = null;
    try (FileInputStream fileInputStream = new FileInputStream(eventFile)) {
      eventJsonArray = new JSONArray(fileInputStream.toString());
    }
    Path tmpDir =  Paths.get(handler.getWdkTempDirName());
    handler.handleEventList(UserDatasetEventArrayHandler.parseEventsArray(eventJsonArray),
      modelConfig.getUserDatasetStoreConfig().getTypeHandlers(), tmpDir);
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT, true, null, "The project of the app db");
    addSingleValueOption(ARG_EVENTS_FILE, true, null, "File containing an ordered JSON Array of user dataset events"); 
  }

}
