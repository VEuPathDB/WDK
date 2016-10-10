package org.gusdb.wdk.model.user.dataset.event;

import java.util.Collections;
import java.util.Set;

public class UserDatasetEvent {
  private Set<String> projectsFilter; // null if no filter
  private Integer userDatasetId;
  private String userDatasetType;
  
  public UserDatasetEvent(Set<String> projectsFilter, Integer userDatasetId, String userDatasetType) {
    this.projectsFilter = projectsFilter;
    this.userDatasetId = userDatasetId;
    this.userDatasetType = userDatasetType;
  }
  
  public Set<String> getProjectsFilter() { return Collections.unmodifiableSet(projectsFilter); }
  public Integer getUserDatasetId() { return userDatasetId; }
  public String getUserDatasetType() { return userDatasetType; }
}
