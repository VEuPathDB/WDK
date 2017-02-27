package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetShareEvent extends UserDatasetEvent {
  
  public enum ShareAction {
    GRANT, REVOKE
  }
  
  private Integer ownerId;
  private Integer recipientId;
  private ShareAction action;
  
  UserDatasetShareEvent(Long eventId, Set<String> projectsFilter, Integer userDatasetId, UserDatasetType userDatasetType, Integer ownerId, Integer recipientId, ShareAction action) {
    super(eventId, projectsFilter, userDatasetId, userDatasetType);
    this.ownerId = ownerId;
    this.recipientId = recipientId;
    this.action = action;
  }

  public Integer getOwnerId() { return ownerId; }
  
  public Integer getRecipientId() { return recipientId; }
  
  public ShareAction getAction() { return action; }

}
