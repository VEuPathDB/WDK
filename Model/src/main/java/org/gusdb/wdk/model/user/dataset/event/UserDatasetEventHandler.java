package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetAccessControlEvent.AccessControlAction;
import org.apache.log4j.Logger;


public class UserDatasetEventHandler {

  private static final Logger logger = Logger.getLogger(UserDatasetEventHandler.class);

  public static void handleInstallEvent (UserDatasetInstallEvent event, UserDatasetTypeHandler typeHandler, UserDatasetStore userDatasetStore, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir) throws WdkModelException {
    
    logger.info("Installing user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
    UserDataset userDataset = userDatasetStore.getUserDataset(event.getOwnerUserId(), event.getUserDatasetId());

    String sql = "insert into " + userDatasetSchemaName + ".InstalledUserDataset (user_dataset_id, name) values (?, ?)";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {event.getUserDatasetId(), userDataset.getMeta().getName()};
    sqlRunner.executeUpdate(args);

    typeHandler.installInAppDb(userDataset, tmpDir);
    grantAccess(event.getOwnerUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }
  
  public static void handleUninstallEvent (UserDatasetUninstallEvent event, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName, Path tmpDir) throws WdkModelException {

    logger.info("Uninstalling user dataset " + event.getUserDatasetId());
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    revokeAllAccess(event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    typeHandler.uninstallInAppDb(event.getUserDatasetId(), tmpDir);
    String sql = "delete from " + userDatasetSchemaName + ".InstalledUserDataset where user_dataset_id = ?";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {event.getUserDatasetId()};
    sqlRunner.executeUpdate(args);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);
  }
  
  public static void handleAccessControlEvent (UserDatasetAccessControlEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {

    logger.info("Updating access to user dataset " + event.getUserDatasetId() );
    openEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

    if (event.getAction() == AccessControlAction.GRANT) 
      grantAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    else 
      revokeAccess(event.getUserId(), event.getUserDatasetId(), appDbDataSource,userDatasetSchemaName);
    closeEventHandling(event.getEventId(), appDbDataSource, userDatasetSchemaName);

  }
  
  private static void grantAccess(Integer userId, Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Granting access to user dataset " + userDatasetId + " to user " + userId);
    String sql = "insert into " + userDatasetSchemaName + ".UserDatasetAccessControl (user_id, user_dataset_id) values (?, ?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {userId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }
  
  private static void revokeAccess(Integer userId, Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Revoking access to user dataset " + userDatasetId + " from user " + userId);
    String sql = "delete from " + userDatasetSchemaName + ".UserDatasetAccessControl where user_id = ? and user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {userId, userDatasetId};
    sqlRunner.executeUpdate(args);
  }
  private static void revokeAllAccess(Integer userDatasetId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Revoking all access to user dataset " + userDatasetId);
   String sql = "delete from " + userDatasetSchemaName + ".UserDatasetAccessControl where user_dataset_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {userDatasetId};
    sqlRunner.executeUpdate(args);
  }

  private static void openEventHandling(Integer eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    logger.info("Start handling event: " + eventId);
    String sql = "insert into " + userDatasetSchemaName + ".UserDatasetEvent (event_id) values (?)";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
  }

  private static void closeEventHandling(Integer eventId, DataSource appDbDataSource, String userDatasetSchemaName) {
    String sql = "update " + userDatasetSchemaName + ".UserDatasetEvent set completed = sysdate where event_id = ?";
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {eventId};
    sqlRunner.executeUpdate(args);
    logger.info("Done handling event: " + eventId);
  }


}
