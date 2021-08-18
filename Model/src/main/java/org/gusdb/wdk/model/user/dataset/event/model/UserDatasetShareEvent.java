package org.gusdb.wdk.model.user.dataset.event.model;

import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

public class UserDatasetShareEvent extends UserDatasetEvent {

  public enum ShareAction {
    GRANT,
    REVOKE
  }

  private final Long        ownerId;
  private final Long        recipientId;
  private final ShareAction action;

  public UserDatasetShareEvent(
    Long eventId,
    Set < String > projectsFilter,
    Long userDatasetId,
    UserDatasetType userDatasetType,
    Long ownerId,
    Long recipientId,
    ShareAction action
  ) {
    super(eventId, UserDatasetEventType.SHARE, projectsFilter, userDatasetId, userDatasetType);
    this.ownerId     = ownerId;
    this.recipientId = recipientId;
    this.action      = action;
  }

  public Long getOwnerId() {
    return ownerId;
  }

  public Long getRecipientId() {
    return recipientId;
  }

  public ShareAction getAction() {
    return action;
  }
}
