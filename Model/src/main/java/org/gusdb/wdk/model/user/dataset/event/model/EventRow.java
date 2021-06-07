package org.gusdb.wdk.model.user.dataset.event.model;

import java.time.LocalDateTime;

import org.gusdb.wdk.model.user.dataset.UserDatasetType;

/**
 * Class modeling a single row in the "userdatasetevent" table.
 */
public class EventRow
{
  private final long            eventID;
  private final long            userDatasetID;
  private final UserDatasetType type;

  private LocalDateTime          handledTime;
  private UserDatasetEventStatus status;

  public EventRow(long eventID, long userDatasetID, UserDatasetType type) {
    this.eventID       = eventID;
    this.userDatasetID = userDatasetID;
    this.type          = type;

    this.handledTime = LocalDateTime.now();
    this.status      = UserDatasetEventStatus.PROCESSING;
  }

  public EventRow(
    long eventID,
    long userDatasetID,
    LocalDateTime handledTime,
    UserDatasetEventStatus status,
    UserDatasetType type
  ) {
    this.eventID       = eventID;
    this.userDatasetID = userDatasetID;
    this.handledTime   = handledTime;
    this.status        = status;
    this.type          = type;
  }

  public long getEventID() {
    return eventID;
  }

  public long getUserDatasetID() {
    return userDatasetID;
  }

  public UserDatasetType getType() {
    return type;
  }

  public LocalDateTime getHandledTime() {
    return handledTime;
  }

  public EventRow setHandledTime(LocalDateTime handledTime) {
    this.handledTime = handledTime;
    return this;
  }

  public UserDatasetEventStatus getStatus() {
    return status;
  }

  public EventRow setStatus(UserDatasetEventStatus status) {
    this.status = status;
    return this;
  }
}
