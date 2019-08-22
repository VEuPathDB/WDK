package org.gusdb.wdk.model.user.dataset.event;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetShareEvent extends UserDatasetEvent {

  public enum ShareAction {
    GRANT, REVOKE
  }

  private Long ownerId;
  private Long recipientId;
  private ShareAction action;

  UserDatasetShareEvent(Long eventId, Set<String> projectsFilter, Long userDatasetId, UserDatasetType userDatasetType, Long ownerId, Long recipientId, ShareAction action) {
    super(eventId, projectsFilter, userDatasetId, userDatasetType);
    this.ownerId = ownerId;
    this.recipientId = recipientId;
    this.action = action;
  }

  public Long getOwnerId() { return ownerId; }

  public Long getRecipientId() { return recipientId; }

  public ShareAction getAction() { return action; }
}
