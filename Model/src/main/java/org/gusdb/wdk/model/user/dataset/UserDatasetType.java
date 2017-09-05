package org.gusdb.wdk.model.user.dataset;

/**
 * Describes the type of a dataset.  Should be constructed only by the factory.
 * 
 * @author steve
 */
public class UserDatasetType {

  private String _name;
  private String _version;

  public UserDatasetType(String name, String version) {
    _name = name;
    _version = version;
  }

  public String getName() { return _name; }
  public String getVersion() { return _version; }

  @Override
  public String toString() {
    return "(type=" + _name + ", version=" + _version + ")";
  }
}
