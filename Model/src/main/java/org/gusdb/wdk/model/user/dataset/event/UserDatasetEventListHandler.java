package org.gusdb.wdk.model.user.dataset.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.config.ModelConfigUserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetExternalDatasetEvent;
import org.gusdb.wdk.model.user.dataset.UserDatasetExternalDatasetEvent.ExternalDatasetAction;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeFactory;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetShareEvent.ShareAction;
import org.xml.sax.SAXException;

public class UserDatasetEventListHandler extends BaseCLI {
  protected static final String ARG_PROJECT = "project";
  protected static final String ARG_EVENTS_FILE = "eventsFile";


  private UserDatasetStore userDatasetStore;
  private DataSource appDbDataSource;
  private ModelConfig modelConfig;
  private String projectId;
  private String wdkTempDirName;
  
  private static final Logger logger = Logger.getLogger(UserDatasetEventListHandler.class);
  
  public UserDatasetEventListHandler(String command) {
    super(command, "Handle a list of user dataset events.");
  }

  public void handleEventList(List<UserDatasetEvent> eventList,
      Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, Path tmpDir)
          throws WdkModelException {
    
    Integer lastHandledEventId = findLastHandledEvent(getAppDbDataSource(), getUserDatasetSchemaName());
    int count = 0;
    for (UserDatasetEvent event : eventList) {

      if (event.getEventId() <= lastHandledEventId) continue;
      
      if (event instanceof UserDatasetInstallEvent) {
        UserDatasetTypeHandler typeHandler = typeHandlers.get(event.getUserDatasetType());
        if (typeHandler == null)
          throw new WdkModelException("Install event " + event.getEventId() + " refers to typeHandler " +
              event.getUserDatasetType() + " which is not present in the wdk configuration");
        UserDatasetEventHandler.handleInstallEvent((UserDatasetInstallEvent) event, typeHandler,
            getUserDatasetStore(), getAppDbDataSource(), getUserDatasetSchemaName(), tmpDir);
      }

      else if (event instanceof UserDatasetUninstallEvent) {
        UserDatasetTypeHandler typeHandler = typeHandlers.get(event.getUserDatasetType());
        if (typeHandler == null)
          throw new WdkModelException("Uninstall event " + event.getEventId() + " refers to typeHandler " +
              event.getUserDatasetType() + " which is not present in the wdk configuration");
        UserDatasetEventHandler.handleUninstallEvent((UserDatasetUninstallEvent) event, typeHandler,
            getAppDbDataSource(), getUserDatasetSchemaName(), tmpDir);
      }

      else if (event instanceof UserDatasetShareEvent) {
        UserDatasetEventHandler.handleShareEvent((UserDatasetShareEvent) event,
            getAppDbDataSource(), getUserDatasetSchemaName());
      }
      
      else if (event instanceof UserDatasetExternalDatasetEvent) {
        UserDatasetEventHandler.handleExternalDatasetEvent((UserDatasetExternalDatasetEvent) event,
            getAppDbDataSource(), getUserDatasetSchemaName());
      }

      count++;
    }
    logger.info("Handled " + count + " new events");
  }

  /**
   * Find the highest event id in the app db's handled events log.  Null if none. 
   * @param appDbDataSource
   * @param userDatasetSchemaName
   * @return
   * @throws WdkModelException if the log has a failed event (no complete date) from a previous run.
   */
  private Integer findLastHandledEvent(DataSource appDbDataSource, String userDatasetSchemaName) throws WdkModelException {
    
    final List<Integer> ids = new ArrayList<Integer>();
    ResultSetHandler handler = new ResultSetHandler() {
      @Override
      public void handleResult(ResultSet rs) throws SQLException {
        if (rs.next()) ids.add(rs.getInt(1)); // one row will be returned
      }
    };

    // first confirm there are no failed events from the last run.  (They'll have a null completed time)
    String sql = "select min(event_id) from " + userDatasetSchemaName + ".UserDatasetEvent where completed is null";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "find-earliest-incomplete-event-id");
    sqlRunner.executeQuery(handler); 
    if (ids.get(0) != 0) {
      throw new WdkModelException("Event id " + ids.get(0) + " failed to complete in a previous run");
    }

    // find highest previously handled event id
    ids.remove(0);
    sql = "select max(event_id) from " + userDatasetSchemaName + ".UserDatasetEvent";
    sqlRunner = new SQLRunner(appDbDataSource, sql, "find-latest-event-id");
    sqlRunner.executeQuery(handler); 
    return ids.get(0);
  }

  private UserDatasetStore getUserDatasetStore() throws WdkModelException {
    if (userDatasetStore == null) {
      ModelConfigUserDatasetStore udsConfig= getModelConfig().getUserDatasetStoreConfig();
      userDatasetStore = udsConfig.getUserDatasetStore();
    }
    return userDatasetStore;
  }
  
  private DataSource getAppDbDataSource() throws WdkModelException {
    if (appDbDataSource == null) {
      DatabaseInstance appDb = new DatabaseInstance(getModelConfig().getAppDB(), WdkModel.DB_INSTANCE_APP, true);
      appDbDataSource = appDb.getDataSource();
    }
    return appDbDataSource;
  }

  /*
  public String getGusConfig(String key) throws FileNotFoundException,
						       IOException {

    if (gusProps == null) {
      String gusHome = System.getProperty("GUS_HOME");
      String configFileName = gusHome + "/config/gus.config";
      gusProps = new Properties();
      gusProps.load(new FileInputStream(configFileName));
    }
    String value = gusProps.getProperty(key);
    if (value == null)
      error("Required property " + key
	    + " not found in gus.config file: " + configFileName);
    return value;
  }
  */

  // TODO: get from model config
  private String getUserDatasetSchemaName() {
    return  "ApiDBUserDatasets.";
  }
  
  private String getWdkTempDirName() throws WdkModelException {
    if (wdkTempDirName == null) {
      wdkTempDirName = getModelConfig().getWdkTempDir();
    }
    return wdkTempDirName;
  }
  
  private ModelConfig getModelConfig() throws  WdkModelException {
    if (modelConfig == null) {
      try {
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);     
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        modelConfig = parser.parseConfig(getProjectId());
      } catch (SAXException | IOException e) {
        throw new WdkModelException(e);
      }
    }
    return modelConfig;
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

  public List<UserDatasetEvent> parseEventsFile(File eventsFile) throws WdkModelException {

    List<UserDatasetEvent> events = new ArrayList<UserDatasetEvent>();

    try {
      InputStream in = Files.newInputStream(eventsFile.toPath());
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String line = null;
      while ((line = reader.readLine()) != null) {
	line = line.trim();
	if (line.length() == 0) break;
	if (line.startsWith("#")) continue;
	String[] columns = line.split("\t");
	
	Integer eventId = new Integer(columns[0]);
	String project = columns[2].length() > 0 ? columns[2] : null;
	Set<String> projectsFilter = new HashSet<String>();
	projectsFilter.add(project);
	Integer userDatasetId = new Integer(columns[3]);
	UserDatasetType userDatasetType = UserDatasetTypeFactory.getUserDatasetType(columns[4], columns[5]);

	// event_id install projects user_dataset_id ud_type_name ud_type_version owner_user_id genome genome_version
	if (columns[1].equals("install")) {
	  Integer ownerUserId = new Integer(columns[6]);
	  String[] dependencyArr = columns[7].split(" "); // for now, support just one dependency
	  Set<UserDatasetDependency> dependencies = new HashSet<UserDatasetDependency>();
	  dependencies.add(new UserDatasetDependency(dependencyArr[0], dependencyArr[1], ""));
	  events.add(new UserDatasetInstallEvent(eventId, projectsFilter, userDatasetId, userDatasetType, ownerUserId,
						 dependencies));
	}

	// event_id uninstall projects user_dataset_id ud_type_name ud_type_version
	else if (columns[1].equals("uninstall")) {
	  events.add(new UserDatasetUninstallEvent(eventId, projectsFilter, userDatasetId, userDatasetType));
	}

	// event_id share projects user_dataset_id ud_type_name ud_type_version user_id grant
	else if (columns[1].equals("share")) {
	  Integer userId = new Integer(columns[6]);
	  ShareAction action = columns[7].equals("grant") ? ShareAction.GRANT
            : ShareAction.REVOKE;
	  events.add(new UserDatasetShareEvent(eventId, projectsFilter, userDatasetId, userDatasetType, userId,
						       action));
	}

    // event_id externalDataset projects user_dataset_id ud_type_name ud_type_version user_id grant
    else if (columns[1].equals("externalDataset")) {
      Integer userId = new Integer(columns[6]);
      ExternalDatasetAction action = columns[7].equals("create") ? ExternalDatasetAction.CREATE
            : ExternalDatasetAction.DELETE;
      events.add(new UserDatasetExternalDatasetEvent(eventId, projectsFilter, userDatasetId, userDatasetType, userId,
                               action));
    }

	else {
	  throw new WdkModelException("Unrecognized user dataset event type: " + columns[1]);
	}
      }
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
    
    return events;
  }
  
  private void setProjectId(String projectId) {this.projectId = projectId;}
  private String getProjectId() { return projectId;}
  
  @Override
  protected void execute() throws Exception {
    setProjectId((String) getOptionValue(ARG_PROJECT));
    File eventsFile = new File((String) getOptionValue(ARG_EVENTS_FILE));
    Path tmpDir =  Paths.get(getWdkTempDirName());
    handleEventList(parseEventsFile(eventsFile), getModelConfig().getUserDatasetStoreConfig().getTypeHandlers(), tmpDir);
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT, true, null, "The project of the app db");
    addSingleValueOption(ARG_EVENTS_FILE, true, null, "File containing an ordered list of user dataset events"); 
  }

}
