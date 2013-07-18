package org.gusdb.wdk.model.config;

import org.gusdb.fgputil.db.platform.DBPlatform;

/**
 * An object representation of the {@code <userDB>} tag in the
 * {@code model-config.xml}. Two schema are used for host wdk tables, and the
 * {@link ModelConfigUserDB#userSchema} has the tables used to user specific
 * data, while the {@link ModelConfigUserDB#wdkEngineSchema} has the tables used
 * to store the data shared between users.
 * 
 * @author xingao
 * 
 */
public class ModelConfigUserDB extends ModelConfigDB {

  private String userSchema;
  private String wdkEngineSchema;

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

  /**
   * @return the wdkEngineSchema
   */
  public String getWdkEngineSchema() {
    return wdkEngineSchema;
  }

  /**
   * @param wdkEngineSchema
   *          the wdkEngineSchema to set
   */
  public void setWdkEngineSchema(String wdkEngineSchema) {
    this.wdkEngineSchema = DBPlatform.normalizeSchema(wdkEngineSchema);
  }
}
