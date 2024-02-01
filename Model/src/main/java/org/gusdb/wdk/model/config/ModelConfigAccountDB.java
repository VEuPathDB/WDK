package org.gusdb.wdk.model.config;

import org.gusdb.fgputil.db.platform.DBPlatform;

public class ModelConfigAccountDB extends ModelConfigDB {

  private String _accountSchema;

  public void setAccountSchema(String accountSchema) {
    _accountSchema = DBPlatform.normalizeSchema(accountSchema);
  }

  public String getAccountSchema() {
    return _accountSchema;
  }
}
