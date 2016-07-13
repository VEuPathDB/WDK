package org.gusdb.wdk.model.user.dataset;

/**
 * Describes the type of a dataset.  Should be constructed only by the factory.
 * @author steve
 *
 */
public class UserDatasetType {
  private String name;
  private String version;
  
  UserDatasetType(String name, String version) {
    this.name = name;
    this.version = version;
  }
  
  public String getName() { return name; }
  public String getVersion() { return version; }
}



