package org.gusdb.wdk.model.user.dataset.event.model;

import java.util.Collections;
import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetInstallEvent extends UserDatasetEvent
{

  private final Set < UserDatasetDependency > dependencies;

  private final Long ownerUserId;

  public UserDatasetInstallEvent(
    Long eventId,
    Set < String > projectsFilter,
    Long userDatasetId,
    UserDatasetType userDatasetType,
    Long ownerUserId,
    Set < UserDatasetDependency > dependencies
  ) {
    super(eventId, UserDatasetEventType.INSTALL, projectsFilter, userDatasetId, userDatasetType);
    this.dependencies = dependencies;
    this.ownerUserId  = ownerUserId;
  }

  public Set < UserDatasetDependency > getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  public Long getOwnerUserId() {
    return ownerUserId;
  }
}
