package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.util.Map;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore;


public class IrodsUserDatasetStore extends JsonUserDatasetStore {

  public IrodsUserDatasetStore() {
    super(new IrodsUserDatasetStoreAdaptor());
  }
  
  @Override
  public void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    super.initialize(configuration, typeHandlers);
    String zone = configuration.get("zone");
    String resource = configuration.get("resource");
    String host = configuration.get("host");
    int port = Integer.parseInt(configuration.get("port"));
    String user = configuration.get("login");
    String password = configuration.get("password");
    IrodsUserDatasetStoreAdaptor.initializeIrods(host,port,user,password,zone,resource);
    checkRootDirExists();
  }
  
  @Override
  public UserDatasetFile getUserDatasetFile(Path path, Integer userDatasetId) {
    return new IrodsUserDatasetFile(path, userDatasetId);
  }

}
