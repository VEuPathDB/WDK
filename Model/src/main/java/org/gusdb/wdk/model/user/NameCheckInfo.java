package org.gusdb.wdk.model.user;

public class NameCheckInfo {

  boolean _nameExists;
  boolean _isPublic;
  String _description;

  public NameCheckInfo(boolean nameExists, boolean isPublic, String description) {
    _nameExists = nameExists;
    _isPublic = isPublic;
    _description = description;
  }

  public boolean nameExists() {
    return _nameExists;
  }

  public boolean isPublic() {
    return _isPublic;
  }

  public String getDescription() {
    return _description;
  }
}