package org.gusdb.wdk.errors;

import org.json.JSONObject;

public class ClientErrorBundle implements ErrorBundle {

  private final String _errorDetailsAsString;

  public ClientErrorBundle(JSONObject errorJson) {
    _errorDetailsAsString = errorJson.toString(2);
  }

  @Override
  public boolean hasErrors() {
    return true;
  }

  @Override
  public String getDetailedDescription() {
    return _errorDetailsAsString;
  }

}
