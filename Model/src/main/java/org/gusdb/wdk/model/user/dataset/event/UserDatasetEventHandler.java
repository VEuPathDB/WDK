package org.gusdb.wdk.model.user.dataset.event;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetAccessControlEvent.AccessControlAction;

public class UserDatasetEventHandler {

  public static void handleEvent (UserDatasetInstallEvent event, UserDataset userDataset, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName) {
    typeHandler.installInAppDb(userDataset, appDbDataSource, userDatasetSchemaName);
  }
  
  public static void handleEvent (UserDatasetUninstallEvent event, UserDatasetTypeHandler typeHandler, DataSource appDbDataSource, String userDatasetSchemaName) {
    typeHandler.uninstallInAppDb(event.getUserDatasetId(), appDbDataSource, userDatasetSchemaName);
  }
  
  public static void handleEvent (UserDatasetAccessControlEvent event, DataSource appDbDataSource, String userDatasetSchemaName) {
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
