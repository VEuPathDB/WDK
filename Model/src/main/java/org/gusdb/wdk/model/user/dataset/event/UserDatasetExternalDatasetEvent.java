package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetExternalDatasetEvent extends UserDatasetEvent {
  
  public enum ExternalDatasetAction {
    CREATE, DELETE
  }
  
  private Integer userId;
  private ExternalDatasetAction action;
  
  public UserDatasetExternalDatasetEvent(Long eventId, Set<String> projectsFilter, Integer userDatasetId, UserDatasetType userDatasetType, Integer userId, ExternalDatasetAction action) {
    super(eventId, projectsFilter, userDatasetId, userDatasetType);
    this.userId = userId;
    this.action = action;
  }

  public Integer getUserId() { return userId; }
  
  public ExternalDatasetAction getAction() { return action; }

}
