package org.gusdb.wdk.model.user.dataset.irods;

import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetStore;

public class IrodsUserDatasetStore extends JsonUserDatasetStore {

  public IrodsUserDatasetStore() {
    super(new IrodsUserDatasetStoreAdaptor());
  }

}
