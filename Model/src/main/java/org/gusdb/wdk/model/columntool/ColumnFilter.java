package org.gusdb.wdk.model.columntool;

import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONObject;

public interface ColumnFilter extends ColumnToolElement<ColumnFilter> {

  public JSONObject validateConfig(JSONObject config) throws WdkUserException;

  public void setConfig(JSONObject value);

  public String buildSqlWhere();
  
}