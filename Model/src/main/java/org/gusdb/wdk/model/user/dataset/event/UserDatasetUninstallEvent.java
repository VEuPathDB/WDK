package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

public class UserDatasetUninstallEvent extends UserDatasetEvent {
  UserDatasetUninstallEvent(Set<String> projectsFilter, Integer userDatasetId, String userDatasetType) {
    super(projectsFilter, userDatasetId, userDatasetType);
  }
}
