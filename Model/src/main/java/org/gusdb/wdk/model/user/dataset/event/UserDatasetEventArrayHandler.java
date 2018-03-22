package org.gusdb.wdk.model.user.dataset.event;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.config.ModelConfigUserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UnsupportedTypeHandler;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeFactory;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetShareEvent.ShareAction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This object accepts and events file in the form of a json array
 * @author crisl-adm
 *
 */
public class UserDatasetEventArrayHandler {

  protected static final String ARG_PROJECT = "project";

  private UserDatasetStore userDatasetStore;
  private ModelConfig modelConfig;
  private String projectId;
  private String wdkTempDirName;

  private static final Logger logger = Logger.getLogger(UserDatasetEventArrayHandler.class);

  public UserDatasetEventArrayHandler(String projectId) throws WdkModelException {
	this.projectId = projectId;
	modelConfig = getModelConfig();
	wdkTempDirName = getWdkTempDirName();
	userDatasetStore = getUserDatasetStore();
  }
  
  /**
   * A list of user dataset events is processed.  Only events that have not been handled
   * to date are processed.  The nature of the processing is defined by the user dataset
   * event object (install, uninstall, share).
   * @param eventList - list of user dataset event to be processed.
   * database records in tables.
   * @param tmpDir
   * @throws WdkModelException
   */
  public void handleEventList(List<UserDatasetEvent> eventList) throws WdkModelException {

	Path tmpDir = Paths.get(getWdkTempDirName());
    try (DatabaseInstance appDb = new DatabaseInstance(getModelConfig().getAppDB(), WdkModel.DB_INSTANCE_APP, true)) {

      DataSource appDbDataSource = appDb.getDataSource();
      Long lastHandledEventId = findLastHandledEvent(appDbDataSource, getUserDatasetSchemaName());
      int count = 0;

      // If the subject user dataset does not have a currently supported type handler the install
      // or uninstall command will be skipped.  In theory, no user dataset with an unsupported
      // type handler should ever be installed on the system as type handlers should only be added
      // and removed at release time when the UD database is emptied.
      for (UserDatasetEvent event : eventList) {

    	    // If this event was handled before, skip to the next event - this really shouldn't happen.
        if ((lastHandledEventId != null && event.getEventId() <= lastHandledEventId)) continue;
        
        // If the event does not apply to this project, complete the event handling and skip to
        // the next event.
        if(!event.getProjectsFilter().contains(getProjectId())) {
        	  UserDatasetEventHandler.completeEventHandling(event.getEventId(), appDbDataSource, getUserDatasetSchemaName());
        	  count++;
        	  continue;
        }
        
        if (event instanceof UserDatasetInstallEvent) {
          UserDatasetTypeHandler typeHandler = userDatasetStore.getTypeHandler(event.getUserDatasetType());
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            logger.warn("Install event " + event.getEventId() + " refers to typeHandler " +
              event.getUserDatasetType() + " which is not present in the wdk configuration." +
            	  "Skipping the install but declaring the event as handled.");
            UserDatasetEventHandler.completeEventHandling(event.getEventId(), appDbDataSource, getUserDatasetSchemaName());
            continue;
          }
          else {
            UserDatasetEventHandler.handleInstallEvent((UserDatasetInstallEvent) event, typeHandler, getUserDatasetStore(),
             appDbDataSource, getUserDatasetSchemaName(), tmpDir, getModelConfig().getProjectId());
          }  
        }

        else if (event instanceof UserDatasetUninstallEvent) {
          UserDatasetTypeHandler typeHandler = userDatasetStore.getTypeHandler(event.getUserDatasetType());
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            logger.warn("Uninstall event " + event.getEventId() + " refers to typeHandler " +
              event.getUserDatasetType() + " which is not present in the wdk configuration." +
              "Skipping the uninstall but declaring the event as handled.");
            UserDatasetEventHandler.completeEventHandling(event.getEventId(), appDbDataSource, getUserDatasetSchemaName());
          }
          else {
            UserDatasetEventHandler.handleUninstallEvent((UserDatasetUninstallEvent) event, typeHandler,
             appDbDataSource, getUserDatasetSchemaName(), tmpDir, getModelConfig().getProjectId());
          }  
        }

        else if (event instanceof UserDatasetShareEvent) {
          UserDatasetEventHandler.handleShareEvent((UserDatasetShareEvent) event,
            appDbDataSource, getUserDatasetSchemaName());
        }

        count++;
      }
      logger.info("Handled " + count + " new events");
    }
    catch (Exception e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Find the highest event id in the app db's handled events log.  Null if none.
   * @param appDbDataSource
   * @param userDatasetSchemaName
   * @return
   * @throws WdkModelException if the log has a failed event (no complete date) from a previous run.
   */
  public Long findLastHandledEvent(DataSource appDbDataSource, String userDatasetSchemaName) throws WdkModelException {

    SingleLongResultSetHandler handler = new SingleLongResultSetHandler();

    // first confirm there are no failed events from the last run.  (They'll have a null completed time)
    String sql = "select min(event_id) from " + userDatasetSchemaName + "UserDatasetEvent where completed is null";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "find-earliest-incomplete-event-id");
    sqlRunner.executeQuery(handler);
    if (!handler.getStatus().equals(Status.NULL_VALUE)) {
      throw new WdkModelException("Event id " + handler.getRetrievedValue() + " failed to complete in a previous run");
    }

    // find highest previously handled event id
    sql = "select max(event_id) from " + userDatasetSchemaName + "UserDatasetEvent";
    sqlRunner = new SQLRunner(appDbDataSource, sql, "find-latest-event-id");
    sqlRunner.executeQuery(handler); 
    return handler.getRetrievedValue();
  }

  public UserDatasetStore getUserDatasetStore() throws WdkModelException {
    if (userDatasetStore == null) {
      ModelConfigUserDatasetStore udsConfig= getModelConfig().getUserDatasetStoreConfig();
      userDatasetStore = udsConfig.getUserDatasetStore(wdkTempDirName);
    }
    return userDatasetStore;
  }

  /*
  public String getGusConfig(String key) throws IOException {
    if (gusProps == null) {
      String gusHome = System.getProperty("GUS_HOME");
      String configFileName = gusHome + "/config/gus.config";
      gusProps = new Properties();
      gusProps.load(new FileInputStream(configFileName));
    }
    String value = gusProps.getProperty(key);
    if (value == null)
      error("Required property " + key + " not found in gus.config file: " + configFileName);
    return value;
  }
  */

  // TODO: get from model config
  public String getUserDatasetSchemaName() {
    return  "ApiDBUserDatasets.";
  }

  public String getWdkTempDirName() throws WdkModelException {
    if (wdkTempDirName == null) {
      wdkTempDirName = getModelConfig().getWdkTempDir();
      // Checks that the temp dir is there with proper perms and if not, provides it.
      WdkModel.checkTempDir(wdkTempDirName);
    }
    return wdkTempDirName;
  }
  
  public ModelConfig getModelConfig() throws  WdkModelException {
    if (modelConfig == null) {
      try {
    	String gusHome = GusHome.getGusHome();
        //String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        ModelConfigParser parser = new ModelConfigParser(gusHome);
        modelConfig = parser.parseConfig(getProjectId());
        QueryLogger.initialize(modelConfig.getQueryMonitor());
      }
      catch (SAXException | IOException e) {
        throw new WdkModelException(e);
      }
    }
    return modelConfig;
  }

  /**
   * Accepts an array of event json objects from a Jenkins job that composes the content a series of
   * event files containing json objects into a single json array and returns a corresponding list
   * of UserDatasetEvent objects.
   * @param eventJsonArray - JSONArray containing an array of event JSONObjects
   * @return - a list of UserDatasetEvents corresponding to 
   * @throws WdkModelException
   */
  public static List<UserDatasetEvent> parseEventsArray(JSONArray eventJsonArray) throws WdkModelException {
    List<UserDatasetEvent> events = new ArrayList<UserDatasetEvent>();
    for(int i = 0; i < eventJsonArray.length(); i++) {
      JSONObject eventJson = eventJsonArray.getJSONObject(i);	
      parseEventObject(eventJson, events);
    }
    return events;
  }

  /**
   * Parse the individual JSONObject representing an event and call the appropriate event
   * handler based on the information contained within that object.
   * @param eventJson - JSONObject representing the event
   * @param events - a list of UserDatasetEvents to which new events are added.
   * @throws WdkModelException
   */
  protected static void parseEventObject(JSONObject eventJson, List<UserDatasetEvent> events) throws WdkModelException {
    Long eventId = eventJson.getLong("eventId");
    String event = eventJson.getString("event");
    
    // Extract an array of projects relevant to the event and add them to a project filter.
    String projectsJson = eventJson.getJSONArray("projects").toString();
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<Set<String>> setType = new TypeReference<Set<String>>() {};
    Set<String> projects;
    try {
      projects = mapper.readValue(projectsJson, setType);
    } 
    catch(IOException ioe) {
      throw new WdkModelException(ioe);
    }
    Set<String> projectsFilter = new HashSet<>();
    projectsFilter.addAll(projects);

    Long userDatasetId = eventJson.getLong("datasetId");
    TypeReference<Map<String,String>> mapType  = new TypeReference<Map<String,String>>() {};
    String typeJson = eventJson.getJSONObject("type").toString();
    Map<String, String> type = null;
    try {
      type = mapper.readValue(typeJson, mapType);
    }
    catch(IOException ioe) {
      throw new WdkModelException(ioe);
    }
    UserDatasetType userDatasetType = UserDatasetTypeFactory.getUserDatasetType(type.get("name"), type.get("version"));

    // Dataset is in the user's workspace and now needs to be installed into the database.
    if ("install".equals(event)) {
      Long ownerUserId = eventJson.getLong("owner");
      
      // A dataset may have multiple dependencies
      Set<UserDatasetDependency> dependencies = new HashSet<UserDatasetDependency>();
      JSONArray dependencyJsonArray = eventJson.getJSONArray("dependencies");
      for(int i = 0; i < dependencyJsonArray.length(); i++) {
        JSONObject dependencyJson = dependencyJsonArray.getJSONObject(i);
        dependencies.add(new UserDatasetDependency(dependencyJson.getString("resourceIdentifier"),
            dependencyJson.getString("resourceVersion"), dependencyJson.getString("resourceDisplayName")));
      }
      events.add(new UserDatasetInstallEvent(eventId, projectsFilter, userDatasetId, userDatasetType, ownerUserId, dependencies));
    }

    // Dataset has been deleted from the workspace and now needs to be removed from the database. 
    else if ("uninstall".equals(event)) {
      events.add(new UserDatasetUninstallEvent(eventId, projectsFilter, userDatasetId, userDatasetType));
    }

    // Dataset sharing has either been granted or revoked in the workspace and now must be reflected in the database
    else if ("share".equals(event)) {
      Long ownerId = eventJson.getLong("owner");
      Long recipientId = eventJson.getLong("recipient");
      ShareAction action = "grant".equals(eventJson.getString("action")) ?
          ShareAction.GRANT : ShareAction.REVOKE;
      events.add(new UserDatasetShareEvent(eventId, projectsFilter, userDatasetId, userDatasetType, ownerId, recipientId, action));
    }

    else {
      throw new WdkModelException("Unrecognized user dataset event type: " + event);
    }

  }

  public void setProjectId(String projectId) {this.projectId = projectId;}
  private String getProjectId() { return projectId;}

 
}
