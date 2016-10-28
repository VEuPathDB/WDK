package org.gusdb.wdk.model.user.dataset.event;

import java.io.File;
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
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.config.ModelConfigUserDatasetStore;
import org.gusdb.wdk.model.fix.ModelCacher;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetAccessControlEvent.AccessControlAction;
import org.xml.sax.SAXException;

public class UserDatasetEventListHandler extends BaseCLI {
  protected static final String ARG_PROJECT = "project";
  protected static final String ARG_EVENTS_FILE = "eventsFile";


  private UserDatasetStore userDatasetStore;
  private DataSource appDbDataSource;
  private String userDatasetSchemaName;
  private ModelConfig modelConfig;
  private String projectId;
  private String wdkTempDirName;
  
  private static final Logger logger = Logger.getLogger(ModelCacher.class);
  
  public UserDatasetEventListHandler(String command) {
    super(command, "Handle a list of user dataset events.");
  }

  private  void handleEventList(List<UserDatasetEvent> eventList, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, Path tmpDir) throws WdkModelException, SAXException, IOException {
    for (UserDatasetEvent event : eventList) {
      if (event instanceof UserDatasetInstallEvent) {
        UserDatasetEventHandler.handleInstallEvent((UserDatasetInstallEvent)event, typeHandlers.get(event.getUserDatasetType()), getUserDatasetStore(), getAppDbDataSource(), getUserDatasetSchemaName(), tmpDir);
      } else if (event instanceof UserDatasetUninstallEvent) {
        UserDatasetEventHandler.handleUninstallEvent((UserDatasetUninstallEvent)event, typeHandlers.get(event.getUserDatasetType()), getAppDbDataSource(), getUserDatasetSchemaName());
      } else if (event instanceof UserDatasetAccessControlEvent) {
        UserDatasetEventHandler.handleAccessControlEvent((UserDatasetAccessControlEvent)event, getAppDbDataSource(), getUserDatasetSchemaName());
      }
    }
  }
  
  private UserDatasetStore getUserDatasetStore() throws WdkModelException {
    if (userDatasetStore != null) {
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

  private String getUserDatasetSchemaName() {
    return userDatasetSchemaName;
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

  private List<UserDatasetEvent> parseEventsFile(File eventsFile) throws WdkModelException {
    List<UserDatasetEvent> events = new ArrayList<UserDatasetEvent>();
    String line = "";
    String[] columns = line.split("\t");
    
    String project = columns[1].length() > 0? columns[1] : null;
    Set<String> projectsFilter = new HashSet<String>();
    projectsFilter.add(project);
    Integer userDatasetId = new Integer(columns[2]);
    UserDatasetType userDatasetType = new UserDatasetType(columns[3], columns[4]);
    
    // install projects user_dataset_id ud_type_name ud_type_version owner_user_id genome genome_version
    if (columns[0].equals("install")) {
      Integer ownerUserId = new Integer(columns[5]);
      String[] dependencyArr = columns[6].split(" ");  // for now, support just one dependency
      Set<UserDatasetDependency> dependencies = new HashSet<UserDatasetDependency>();
      dependencies.add(new UserDatasetDependency(dependencyArr[0], dependencyArr[1], ""));
      events.add(new UserDatasetInstallEvent(projectsFilter, userDatasetId, userDatasetType, ownerUserId, dependencies));
    }


    else if (columns[0].equals("uninstall")) {
      events.add(new UserDatasetUninstallEvent(projectsFilter, userDatasetId, userDatasetType));
    } 

    else if (columns[0].equals("accessControl")) {
        Integer userId = new Integer(columns[5]);
        AccessControlAction action = columns[6].equals("grant")? AccessControlAction.GRANT : AccessControlAction.REVOKE;
        events.add(new UserDatasetAccessControlEvent(projectsFilter, userDatasetId, userDatasetType, userId, action));     
    } 

    else {
      throw new WdkModelException("Unrecognized user dataset event type: " + columns[0]);
    }
      
    return null;
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
