package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;

public class IrodsUserDatasetSession extends JsonUserDatasetSession {

  private static final Logger logger = Logger.getLogger(IrodsUserDatasetSession.class);	

  public IrodsUserDatasetSession(Path usersRootDir, Path wdkTempDir) {
    super(new IrodsUserDatasetStoreAdaptor(wdkTempDir), usersRootDir);
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, Long userDatasetId) {
    return new IrodsUserDatasetFile(path, userDatasetId);
  }
  

  @Override
  public List<Path> getRecentEvents(String eventsDirectory, Long lastHandledEventId) throws WdkModelException {
	List<Path> eventFilePaths = new ArrayList<>();
	// If no prior event has been handled it is assumed that a new db is being spun up and all prior
	// events are needed.
	if(lastHandledEventId != null && lastHandledEventId > 0) {  	
	  String cutoffTime = Long.toString(lastHandledEventId).substring(0, Long.toString(lastHandledEventId).length() - 8);
	  // Timestamp must be 11 places long
	  cutoffTime = cutoffTime.length() == 11 ? cutoffTime : '0' + cutoffTime;
      logger.info("Event Cutoff Timestamp is " + cutoffTime + " sec");
      String queryString = "select DATA_NAME where COLL_NAME like '" + eventsDirectory + "' AND DATA_MODIFY_TIME >= '" + cutoffTime + "'";
      List<String> eventFileNames = ((IrodsUserDatasetStoreAdaptor)this.getUserDatasetStoreAdaptor()).executeIrodsQuery(queryString);
      eventFilePaths = eventFileNames.stream().map(eventFileName -> Paths.get(eventsDirectory, eventFileName)).collect(Collectors.toList());
	}
	else {
	  logger.info("Number of events to be delivered is " + eventFilePaths.size());
	  eventFilePaths = adaptor.getPathsInDir(Paths.get(eventsDirectory));
	}
	return eventFilePaths;
  }

  @Override
  public void close() {
    ((IrodsUserDatasetStoreAdaptor) adaptor).closeSession();
  }
  
}
