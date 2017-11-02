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

  public IrodsUserDatasetSession(Path usersRootDir, String wdkTempDirName) {
    super(new IrodsUserDatasetStoreAdaptor(wdkTempDirName, usersRootDir), usersRootDir);
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
      // Making the cutoff time an hour earlier than the last handled event.  So at least one handled
      // event is always passed.  Not sure if this padding is really needed.  It does mean that the same
      // check must still be applied on the event array provided farther downstream.
      String cutoffTime = "0" + (lastHandledEventId/1000 - 60L*60L);
      logger.info("Event Cutoff Timestamp is " + cutoffTime + " sec");
      String queryString = "select DATA_NAME where COLL_NAME like '" + eventsDirectory + "' AND DATA_MODIFY_TIME > '" + cutoffTime + "'";
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
