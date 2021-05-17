package org.gusdb.wdk.model.user.dataset;

/**
 * A simple data container describing the dependency of a user dataset on an
 * application resource (such as data in the application database).
 *
 * @author steve
 */
public class UserDatasetDependency {
  private String resourceIdentifier;
  private String resourceVersion;
  private String resourceDisplayName;

  public UserDatasetDependency() {} // for subclasses that want to initialize using the setters

  public UserDatasetDependency(String resourceIdentifier, String resourceVersion, String resourceDisplayName) {
    this.resourceIdentifier = resourceIdentifier;
    this.resourceVersion = resourceVersion;
    this.resourceDisplayName = resourceDisplayName;
  }

  public void setResourceIdentifier(String resourceIdentifier) {
    this.resourceIdentifier = resourceIdentifier;
  }

  public void setResourceVersion(String resourceVersion) {
    this.resourceVersion = resourceVersion;
  }

  public void setResourceDisplayName(String resourceDisplayName) {
    this.resourceDisplayName = resourceDisplayName;
  }

  /**
   * The identifier of the resource depended on
   */
  public String getResourceIdentifier() {
    return resourceIdentifier;
  }

  /**
   * The version of the resource depended on
   */
  public String getResourceVersion() {
    return resourceVersion;
  }

  /**
   * A human readable form of the resource, for display to end user
   */
  public String getResourceDisplayName() {
    return resourceDisplayName;
  }
}
