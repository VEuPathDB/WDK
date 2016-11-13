package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetExternalDatasetEvent;
import org.gusdb.wdk.model.user.dataset.UserDatasetExternalDatasetEvent.ExternalDatasetAction;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetShareEvent.ShareAction;
import org.apache.log4j.Logger;


public class UserDatasetEventHandler {

  private static final Logger logger = Logger.getLogger(UserDatasetEventHandler.class);
  
  private static final String installedTable = "InstalledUserDataset";
  private static final String ownerTable = "UserDatasetOwner";
  private static final String sharedTable = "UserDatasetSharedWith";
  private static final String externalTable = "UserDatasetExternalDataset";
  private static final String eventTable = "UserDatasetEvent";

  public static void handleInstallEvent (UserDatasetInstallEvent event, UserDatasetTypeHandler typeHandler, UserDatasetStore userDatasetStore, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir) throws WdkModelException {

    logger.info("Installing user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
    UserDataset userDataset = userDatasetStore.getUserDataset(event.getOwnerUserId(), event.getUserDatasetId());

    String sql = "insert into " + userDatasetSchemaName + "." + installedTable + " (user_dataset_id, name) values (?, ?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "insert-user-dataset-row");
    Object[] args = {event.getUserDatasetId(), userDataset.getMeta().getName()};
    sqlRunner.executeUpdate(args);

    typeHandler.installInAppDb(userDataset, tmpDir);
    grantAccess(event.getOwnerUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName, "UserDatasetOwner");
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  public static void handleUninstallEvent (UserDatasetUninstallEvent event, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir) throws WdkModelException {

    logger.info("Uninstalling user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    revokeAllAccess(event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    typeHandler.uninstallInAppDb(event.getUserDatasetId(), tmpDir);
    String sql = "delete from " + userDatasetSchemaName + "." + installedTable + " where user_dataset_id = ?";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "delete-user-dataset-row");
    Object[] args = {event.getUserDatasetId()};
    sqlRunner.executeUpdate(args);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  public static void handleShareEvent (UserDatasetShareEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {

    logger.info("Updating share of user dataset " + event.getUserDatasetId() );
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    if (event.getAction() == ShareAction.GRANT) 
      grantAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName, sharedTable);
    else 
      revokeAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName, sharedTable);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  public static void handleExternalDatasetEvent (UserDatasetExternalDatasetEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {

    logger.info("Updating access to user dataset " + event.getUserDatasetId() );
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    if (event.getAction() == ExternalDatasetAction.CREATE) 
      grantAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName, externalTable);
    else 
      revokeAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName, externalTable);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }

  private static void grantAccess(Integer userId, Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName, String tableName) {
    logger.info("Granting access to user dataset " + userDatasetId + " to user " + userId + " in table " + tableName);
    String sql = "insert into " + userDatasetSchemaName + "." + tableName + " (user_id, user_dataset_id) values (?, ?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "grant-user-dataset-" + tableName);
    Object[] args = {userId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }

  private static void revokeAccess(Integer userId, Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName, String tableName) {
    logger.info("Revoking access to user dataset " + userDatasetId + " from user " + userId);
    String sql = "delete from " + userDatasetSchemaName + "." + tableName + " where user_id = ? and user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-user-dataset-" + tableName);
    Object[] args = {userId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }

  private static void revokeAllAccess(Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Revoking all access to user dataset " + userDatasetId);
    Object[] args = {userDatasetId};
    
    String sql = "delete from " + userDatasetSchemaName + "." + ownerTable + " where user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-all-user-dataset-access-1");
    sqlRunner.executeUpdate(args);
    
    sql = "delete from " + userDatasetSchemaName + "." + sharedTable + " where user_dataset_id = ?";
    sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-all-user-dataset-access-1");
    sqlRunner.executeUpdate(args);

    sql = "delete from " + userDatasetSchemaName + "." + externalTable + " where user_dataset_id = ?";
    sqlRunner = new SQLRunner(appDbDataSource, sql, "revoke-all-user-dataset-access-1");
    sqlRunner.executeUpdate(args);
  }

  private static void openEventHandling(Integer eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Start handling event: " + eventId);
    String sql = "insert into " + userDatasetSchemaName + "." + eventTable + " (event_id) values (?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "insert-user-dataset-event");
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
  }

  private static void closeEventHandling(Integer eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    String sql = "update " + userDatasetSchemaName + "." + eventTable + " set completed = sysdate where event_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql, "complete-user-dataset-event-handling");
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
    logger.info("Done handling event: " + eventId);
  }

}
