package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.runner.BasicResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetShareEvent.ShareAction;


public class UserDatasetEventHandler {

  private static final Logger logger = Logger.getLogger(UserDatasetEventHandler.class);
  
  private static final String installedTable = "InstalledUserDataset";
  private static final String ownerTable = "UserDatasetOwner";
  private static final String sharedTable = "UserDatasetSharedWith";
  private static final String eventTable = "UserDatasetEvent";

  public static void handleInstallEvent (UserDatasetInstallEvent event, UserDatasetTypeHandler typeHandler, UserDatasetStore dsStore, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir, String projectId) throws WdkModelException {

    logger.info("Installing user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
    
    
    try(UserDatasetSession dsSession = dsStore.getSession()) {	
    
      // there is a theoretical race condition here, because this check is not in the same
      // transaction as the rest of this method.   but that risk is very small.
      if (!dsSession.getUserDatasetExists(event.getOwnerUserId(), event.getUserDatasetId())) {
        logger.info("User dataset " + event.getUserDatasetId() + " not found in store.  Was probably deleted.  Skipping install.");
      } 
    
      else {
        UserDataset userDataset = dsSession.getUserDataset(event.getOwnerUserId(), event.getUserDatasetId());

        // Weeding out obsolete user datasets - skipped but completed.
        UserDatasetCompatibility compatibility = typeHandler.getCompatibility(userDataset, appDbDataSource);
        if(compatibility.isCompatible()) {
        
          // insert into the installedTable
          String sql = "insert into " + userDatasetSchemaName + installedTable +
          " (user_dataset_id, name) values (?, ?)";
          SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "insert-user-dataset-row");
          Object[] args = { event.getUserDatasetId(), userDataset.getMeta().getName() };
          sqlRunner.executeUpdate(args);

          // insert into the type-specific tables
          typeHandler.installInAppDb(dsSession, userDataset, tmpDir, projectId);
          
          // grant access to the owner, by installing into the ownerTable
          grantAccess(event.getOwnerUserId(), event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName,
          ownerTable);
        }
        else {
        	  logger.info("User dataset " + event.getUserDatasetId() + " deemed obsolete: " + compatibility.notCompatibleReason() + ".  Skipping install.");
        }
      }
    }  
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  public static void handleUninstallEvent (UserDatasetUninstallEvent event, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir, String projectId) throws WdkModelException {

    logger.info("Uninstalling user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    revokeAllAccess(event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    typeHandler.uninstallInAppDb(event.getUserDatasetId(), tmpDir, projectId);
    String sql = "delete from " + userDatasetSchemaName + installedTable + " where user_dataset_id = ?";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "delete-user-dataset-row");
    Object[] args = {event.getUserDatasetId()};
    sqlRunner.executeUpdate(args);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  public static void handleShareEvent (UserDatasetShareEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {

    logger.info("Updating share of user dataset " + event.getUserDatasetId() );
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    if (!checkUserDatasetInstalled(event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName)) {
      // this can happen if the install was skipped, because the ud was deleted first
      logger.info("User dataset " + event.getUserDatasetId() + " is not installed. Skipping share.");
    } else {
      if (event.getAction() == ShareAction.GRANT)
        grantShareAccess(event.getOwnerId(), event.getRecipientId(), event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName,
            sharedTable);
      else
        revokeShareAccess(event.getOwnerId(), event.getRecipientId(), event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName,
            sharedTable);
    }
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }
  
  /**
   * check if a user dataset is installed (in the installed table).
   * operations that call this method are at theoretical risk of a race condition, since this check
   * is in its own transaction.  but the chance that a ud will be uninstalled in the intervening millisecond is
   * small, not worth engineering for.
   * @param userDatasetId
   * @param appDbDataSource
   * @param userDatasetSchemaName
   * @return
   */
  private static boolean checkUserDatasetInstalled(Long userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Checking if user dataset " + userDatasetId + " is installed");
    BasicResultSetHandler handler = new BasicResultSetHandler();
    String sql = "select user_dataset_id from " + userDatasetSchemaName + installedTable + " where user_dataset_id = ?";
    Object[] args = {userDatasetId};
    new SQLRunner(appDbDataSource, sql, "check-user-dataset-exists").executeQuery(args, handler);

    return handler.getNumRows() > 0;
  }
  
  /**
   * Adds a share to the UserDatasetSharedWith table.
   * @param ownerId
   * @param recipientId
   * @param userDatasetId
   * @param appDbDataSource
   * @param userDatasetSchemaName
   * @param tableName
   */
  private static void grantShareAccess(Long ownerId, Long recipientId, Long userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName, String tableName) {
    logger.info("Granting recipient " + recipientId + " access to user dataset " + userDatasetId + " belonging to owner " + ownerId + " in table " + tableName);
    String sql = "insert into " + userDatasetSchemaName + tableName + " (owner_user_id, recipient_user_id, user_dataset_id) values (?, ?, ?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "grant-user-dataset-" + tableName);
    Object[] args = {ownerId, recipientId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }

  private static void grantAccess(Long userId, Long userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName, String tableName) {
    logger.info("Granting access to user dataset " + userDatasetId + " to user " + userId + " in table " + tableName);
    String sql = "insert into " + userDatasetSchemaName + tableName + " (user_id, user_dataset_id) values (?, ?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "grant-user-dataset-" + tableName);
    Object[] args = {userId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }
  
  private static void revokeShareAccess(Long ownerId, Long recipientId, Long userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName, String tableName) {
    logger.info("Revoking access by recipient " + recipientId + " to user dataset " + userDatasetId + " belonging to owner " + ownerId);
    String sql = "delete from " + userDatasetSchemaName + tableName + " where owner_user_id = ? and recipient_user_id = ? and user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-user-dataset-" + tableName);
    Object[] args = {ownerId, recipientId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }

  private static void revokeAllAccess(Long userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Revoking all access to user dataset " + userDatasetId);
    Object[] args = {userDatasetId};
    
    String sql = "delete from " + userDatasetSchemaName + ownerTable + " where user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-all-user-dataset-access-1");
    sqlRunner.executeUpdate(args);
    
    sql = "delete from " + userDatasetSchemaName + sharedTable + " where user_dataset_id = ?";
    sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-all-user-dataset-access-1");
    sqlRunner.executeUpdate(args);
  }

  private static void openEventHandling(Long eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Start handling event: " + eventId);
    String sql = "insert into " + userDatasetSchemaName + eventTable + " (event_id) values (?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "insert-user-dataset-event");
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
  }

  private static void closeEventHandling(Long eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    String sql = "update " + userDatasetSchemaName + eventTable + " set completed = sysdate where event_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "complete-user-dataset-event-handling");
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
    logger.info("Done handling event: " + eventId);
  }
  
  /**
   * Method to handle an event that is either not relevant to this wdk project or is related to an
   * unsupported type.  The event is not installed but it is noted in the dataset as
   * handled so that the event is not repeatedly and unnecessarily processed.
   * @param eventId
   * @param appDbDataSource
   * @param userDatasetSchemaName
   */
  public static void completeEventHandling(Long eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    openEventHandling(eventId, appDbDataSource, userDatasetSchemaName);
    closeEventHandling(eventId, appDbDataSource, userDatasetSchemaName);
  }

}
