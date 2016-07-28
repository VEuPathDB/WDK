package org.gusdb.wdk.model.user.dataset.filesys;

import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;

/**
 * An implementation of JsonUserDatasetStore that uses the java nio Files operations
 * @author steve
 *
 */

public class FilesysUserDatasetStore extends org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore {
    public FilesysUserDatasetStore() {
      super(new FilesysUserDatasetStoreAdaptor());
    }
    
    public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
      super.initialize(configuration, typeHandlers);
      checkRootDirExists();
    }
} 


