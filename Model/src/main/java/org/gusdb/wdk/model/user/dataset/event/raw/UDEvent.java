package org.gusdb.wdk.model.user.dataset.event.raw;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UDEvent
{
  // JSON Property Keys
  private static final String KeyEventID      = "eventId";
  private static final String KeyDataFiles    = "dataFiles";
  private static final String KeyCreated      = "created";
  private static final String KeyDependencies = "dependencies";
  private static final String KeyDatasetID    = "datasetId";
  private static final String KeyOwner        = "owner";
  private static final String KeyType         = "type";
  private static final String KeyEvent        = "event";
  private static final String KeyProjects     = "projects";
  private static final String KeySize         = "size";
  private static final String KeyRecipient    = "recipient";
  private static final String KeyAction       = "action";

  // Instance Properties
  private long        eventID;
  private long        created;
  private long        datasetID;
  private long        owner;
  private UDEventType event;
  private long        size;
  private UDType      type;

  // The following 2 fields are only present for "share" events.
  private long          recipient;
  private UDShareAction action;

  private List<UDDataFile>   dataFiles;
  private List<UDDependency> dependencies;
  private Set<String>        projects;

  @JsonGetter(KeyEventID)
  public long getEventID() {
    return eventID;
  }

  @JsonSetter(KeyEventID)
  public UDEvent setEventID(long eventID) {
    this.eventID = eventID;
    return this;
  }

  @JsonGetter(KeyCreated)
  public long getCreated() {
    return created;
  }

  @JsonSetter(KeyCreated)
  public UDEvent setCreated(long created) {
    this.created = created;
    return this;
  }

  @JsonGetter(KeyDatasetID)
  public long getDatasetID() {
    return datasetID;
  }

  @JsonSetter(KeyDatasetID)
  public UDEvent setDatasetID(JsonNode datasetID) {
    return datasetID.isTextual()
      ? setDatasetID(datasetID.textValue())
      : setDatasetID(datasetID.longValue());
  }

  @JsonIgnore
  public UDEvent setDatasetID(String datasetID) {
    this.datasetID = Long.parseLong(datasetID);
    return this;
  }

  @JsonIgnore
  public UDEvent setDatasetID(long datasetID) {
    this.datasetID = datasetID;
    return this;
  }

  @JsonGetter(KeyOwner)
  public long getOwner() {
    return owner;
  }

  @JsonSetter(KeyOwner)
  public UDEvent setOwner(JsonNode owner) {
    return owner.isTextual()
      ? setOwner(owner.textValue())
      : setOwner(owner.longValue());
  }

  @JsonIgnore
  public UDEvent setOwner(String owner) {
    this.owner = Long.parseLong(owner);
    return this;
  }

  @JsonIgnore
  public UDEvent setOwner(long owner) {
    this.owner = owner;
    return this;
  }

  @JsonGetter(KeyEvent)
  public UDEventType getEvent() {
    return event;
  }

  @JsonSetter(KeyEvent)
  public UDEvent setEvent(UDEventType event) {
    this.event = event;
    return this;
  }

  @JsonGetter(KeySize)
  public long getSize() {
    return size;
  }

  @JsonSetter(KeySize)
  public UDEvent setSize(long size) {
    this.size = size;
    return this;
  }

  @JsonGetter(KeyType)
  public UDType getType() {
    return type;
  }

  @JsonSetter(KeyType)
  public UDEvent setType(UDType type) {
    this.type = type;
    return this;
  }

  @JsonGetter(KeyDataFiles)
  public List<UDDataFile> getDataFiles() {
    return dataFiles;
  }

  @JsonSetter(KeyDataFiles)
  public UDEvent setDataFiles(List<UDDataFile> dataFiles) {
    this.dataFiles = dataFiles;
    return this;
  }

  @JsonGetter(KeyDependencies)
  public List<UDDependency> getDependencies() {
    return dependencies;
  }

  @JsonSetter(KeyDependencies)
  public UDEvent setDependencies(List<UDDependency> dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  @JsonGetter(KeyProjects)
  public Set<String> getProjects() {
    return projects;
  }

  @JsonSetter(KeyProjects)
  public UDEvent setProjects(Set<String> projects) {
    this.projects = projects;
    return this;
  }

  @JsonGetter(KeyRecipient)
  public long getRecipient() {
    return recipient;
  }

  @JsonSetter(KeyRecipient)
  public UDEvent setRecipient(JsonNode recipient) {
    return recipient.isTextual()
      ? setRecipient(recipient.textValue())
      : setRecipient(recipient.longValue());
  }

  @JsonIgnore
  public UDEvent setRecipient(String recipient) {
    this.recipient = Long.parseLong(recipient);
    return this;
  }

  @JsonIgnore
  public UDEvent setRecipient(long recipient) {
    this.recipient = recipient;
    return this;
  }

  @JsonGetter(KeyAction)
  public UDShareAction getAction() {
    return action;
  }

  @JsonSetter(KeyAction)
  public UDEvent setAction(UDShareAction action) {
    this.action = action;
    return this;
  }
}
