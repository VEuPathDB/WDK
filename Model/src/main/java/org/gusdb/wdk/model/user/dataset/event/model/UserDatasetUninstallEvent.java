package org.gusdb.wdk.model.user.dataset.event.model;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEvent;

public class UserDatasetUninstallEvent extends UserDatasetEvent {
  public UserDatasetUninstallEvent(
    Long eventId,
    Set<String> projectsFilter,
    Long userDatasetId,
    UserDatasetType userDatasetType
  ) {
    super(eventId, UserDatasetEventType.UNINSTALL, projectsFilter, userDatasetId, userDatasetType);
  }
}
