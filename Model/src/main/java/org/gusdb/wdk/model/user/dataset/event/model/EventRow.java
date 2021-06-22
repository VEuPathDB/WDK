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
  private final UserDatasetType userDatasetType;

  private UserDatasetEventType   eventType;
  private LocalDateTime          handledTime;
  private UserDatasetEventStatus status;

  public EventRow(
    long eventID,
    long userDatasetID,
    UserDatasetEventType eventType,
    UserDatasetType userDatasetType
  ) {
    this.eventID         = eventID;
    this.userDatasetID   = userDatasetID;
    this.eventType       = eventType;
    this.userDatasetType = userDatasetType;

    this.handledTime = LocalDateTime.now();
    this.status      = UserDatasetEventStatus.PROCESSING;
  }

  public EventRow(
    long eventID,
    long userDatasetID,
    LocalDateTime handledTime,
    UserDatasetEventStatus status,
    UserDatasetEventType eventType,
    UserDatasetType userDatasetType
  ) {
    this.eventID         = eventID;
    this.userDatasetID   = userDatasetID;
    this.handledTime     = handledTime;
    this.status          = status;
    this.eventType       = eventType;
    this.userDatasetType = userDatasetType;
  }

  public long getEventID() {
    return eventID;
  }

  public long getUserDatasetID() {
    return userDatasetID;
  }

  public UserDatasetEventType getEventType() {
    return eventType;
  }

  public UserDatasetType getUserDatasetType() {
    return userDatasetType;
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
