package org.gusdb.wdk.model.config;

import org.gusdb.fgputil.db.platform.DBPlatform;

/**
 * An object representation of the {@code &lt;userDB>} tag in the {@code model-config.xml}.
 * The {@link ModelConfigUserDB#userSchema} contains the tables used to store user-specific
 * data.
 *
 * @author xingao
 */
public class ModelConfigUserDB extends ModelConfigDB {

  private String userSchema;

  /**
   * @return the userSchema
   */
  public String getUserSchema() {
    return userSchema;
  }

  /**
   * @param userSchema
   *          the userSchema to set
   */
  public void setUserSchema(String userSchema) {
    this.userSchema = DBPlatform.normalizeSchema(userSchema);
  }

}
