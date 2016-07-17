package org.gusdb.wdk.model.user.dataset.filesys;

/**
 * An implementation of JsonUserDatasetStore that uses the java nio Files operations
 * @author steve
 *
 */

public class FilesysUserDatasetStore extends org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore {
    public FilesysUserDatasetStore() {
      super(new FilesysUserDatasetStoreAdaptor());
    }
} 


