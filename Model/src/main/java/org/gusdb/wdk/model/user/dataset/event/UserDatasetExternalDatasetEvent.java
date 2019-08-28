package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetExternalDatasetEvent extends UserDatasetEvent {

  public enum ExternalDatasetAction {
    CREATE, DELETE
  }

  private Long userId;
  private ExternalDatasetAction action;

  public UserDatasetExternalDatasetEvent(Long eventId, Set<String> projectsFilter, Long userDatasetId, UserDatasetType userDatasetType, Long userId, ExternalDatasetAction action) {
    super(eventId, projectsFilter, userDatasetId, userDatasetType);
    this.userId = userId;
    this.action = action;
  }

  public Long getUserId() { return userId; }

  public ExternalDatasetAction getAction() { return action; }

}
