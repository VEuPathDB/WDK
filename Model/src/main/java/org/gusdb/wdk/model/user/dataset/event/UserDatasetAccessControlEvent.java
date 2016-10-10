package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

public class UserDatasetAccessControlEvent extends UserDatasetEvent {
  
  public enum AccessControlAction {
    GRANT, REVOKE
  }
  
  private Integer userId;
  private AccessControlAction action;
  
  UserDatasetAccessControlEvent(Set<String> projectsFilter, Integer userDatasetId, String userDatasetType, Integer userId, AccessControlAction action) {
    super(projectsFilter, userDatasetId, userDatasetType);
    this.userId = userId;
    this.action = action;
  }

  public Integer getUserId() { return userId; }
  
  public AccessControlAction getAction() { return action; }
}
