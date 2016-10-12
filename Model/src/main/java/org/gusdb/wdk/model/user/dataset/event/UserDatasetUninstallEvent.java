package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetUninstallEvent extends UserDatasetEvent {
  UserDatasetUninstallEvent(Set<String> projectsFilter, Integer userDatasetId, UserDatasetType userDatasetType) {
    super(projectsFilter, userDatasetId, userDatasetType);
  }
}
