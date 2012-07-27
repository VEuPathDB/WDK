/**
 * 
 */
package org.gusdb.wdk.model;

import org.gusdb.wdk.model.dbms.DBPlatform;

/**
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
     *            the userSchema to set
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
     *            the wdkEngineSchema to set
     */
    public void setWdkEngineSchema(String wdkEngineSchema) {
        this.wdkEngineSchema = DBPlatform.normalizeSchema(wdkEngineSchema);
    }
}
