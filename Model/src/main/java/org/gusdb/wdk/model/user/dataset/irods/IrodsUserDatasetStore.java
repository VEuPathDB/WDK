package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore;

public class IrodsUserDatasetStore extends JsonUserDatasetStore {

  @Override
  public void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers, String wdkTempDirName) throws WdkModelException {
    super.initialize(configuration, typeHandlers, wdkTempDirName);
    String zone = configuration.get("zone");
    String resource = configuration.get("resource");
    String host = configuration.get("host");
    int port = Integer.parseInt(configuration.get("port"));
    String user = configuration.get("login");
    String password = configuration.get("password");
    IrodsUserDatasetStoreAdaptor.initializeIrods(host,port,user,password,zone,resource);
    try(IrodsUserDatasetSession session = getSession()) {
      session.checkRootDirExists();
      _id = session.initializeUserDatasetStoreId();
    }
  }

  @Override
  public IrodsUserDatasetSession getSession() throws WdkModelException {
    return new IrodsUserDatasetSession(_usersRootDir, getWdkTempDirName());
  }
  
  @Override
  public IrodsUserDatasetSession getSession(Path usersRootDir) throws WdkModelException {
    return new IrodsUserDatasetSession(usersRootDir, getWdkTempDirName());
  }

}
