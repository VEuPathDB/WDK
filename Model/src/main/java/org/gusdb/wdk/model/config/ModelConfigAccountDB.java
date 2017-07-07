package org.gusdb.wdk.model.config;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.fgputil.db.platform.DBPlatform;

public class ModelConfigAccountDB extends ModelConfigDB {

  private String _accountSchema;
  private List<UserPropertyName> _userPropertyNames = new ArrayList<UserPropertyName>();

  public void setAccountSchema(String accountSchema) {
    _accountSchema = DBPlatform.normalizeSchema(accountSchema);
  }

  public String getAccountSchema() {
    return _accountSchema;
  }

  public void addUserPropertyName(UserPropertyName property) {
    _userPropertyNames.add(property);
  }

  public List<UserPropertyName> getUserPropertyNames() {
    return _userPropertyNames;
  }
}
