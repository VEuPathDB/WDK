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

    String sql = "insert into " + userDatasetSchemaName + ".InstalledUserDataset (user_dataset_id) values (?)";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {event.getUserDatasetId()};
    //    sqlRunner.executeUpdate(args);

    UserDataset userDataset = userDatasetStore.getUserDataset(event.getOwnerUserId(), event.getUserDatasetId());
    typeHandler.installInAppDb(userDataset, tmpDir);
  }
  
  public static void handleUninstallEvent (UserDatasetUninstallEvent event, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName) throws WdkModelException {
    typeHandler.uninstallInAppDb(event.getUserDatasetId());
    String sql = "delete from " + userDatasetSchemaName + ".InstalledUserDataset where dataset_id = ?";

    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {event.getUserDatasetId()};
    sqlRunner.executeUpdate(args);

  }
  
  public static void handleAccessControlEvent (UserDatasetAccessControlEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {
    String sql;
    if (event.getAction() == AccessControlAction.GRANT) {
      sql = "insert into " + userDatasetSchemaName + ".UserDatasetAccessControl (user_id, user_dataset_id) values (?, ?)";
    } else {  // REVOKE
      sql = "delete from " + userDatasetSchemaName + ".UserDatasetAccessControl where user_id = ? and dataset_id = ?";
    }
    SQLRunner sqlRunner = new SQLRunner(appDbDataSource, sql);
    Object[] args = {event.getUserId(), event.getUserDatasetId()};
    sqlRunner.executeUpdate(args);
  }
  
}
