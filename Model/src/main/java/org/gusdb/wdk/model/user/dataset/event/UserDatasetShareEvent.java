package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetShareEvent extends UserDatasetEvent {
  
  public enum ShareAction {
    GRANT, REVOKE
  }
  
  private Integer userId;
  private ShareAction action;
  
  UserDatasetShareEvent(Integer eventId, Set<String> projectsFilter, Integer userDatasetId, UserDatasetType userDatasetType, Integer userId, ShareAction action) {
    super(eventId, projectsFilter, userDatasetId, userDatasetType);
    this.userId = userId;
    this.action = action;
  }

  public Integer getUserId() { return userId; }
  
  public ShareAction getAction() { return action; }

}
