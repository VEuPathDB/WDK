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

  // eclipse-generated hashCode
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_version == null) ? 0 : _version.hashCode());
    return result;
  }

  // eclipse-generated equals
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserDatasetType other = (UserDatasetType) obj;
    if (_name == null) {
      if (other._name != null)
        return false;
    }
    else if (!_name.equals(other._name))
      return false;
    if (_version == null) {
      if (other._version != null)
        return false;
    }
    else if (!_version.equals(other._version))
      return false;
    return true;
  }
}
