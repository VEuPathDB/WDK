package org.gusdb.wdk.model.user.dataset.event;

import java.util.Collections;
import java.util.Set;

import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;

public class UserDatasetInstallEvent extends UserDatasetEvent {
  private Set<UserDatasetDependency> dependencies;
  
  UserDatasetInstallEvent(Set<String> projectsFilter, Integer userDatasetId, String userDatasetType, Set<UserDatasetDependency> dependencies) {
    super(projectsFilter, userDatasetId, userDatasetType);
    this.dependencies = dependencies;
  }

  public Set<UserDatasetDependency> getDependencies() { return Collections.unmodifiableSet(dependencies); }
}
