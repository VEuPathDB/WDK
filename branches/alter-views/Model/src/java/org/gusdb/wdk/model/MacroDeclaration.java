/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.json.JSONException;

/**
 * @author xingao
 *
 */
public class MacroDeclaration extends WdkModelBase {

    private String name;
    private boolean usedByModel = true;
    private boolean usedByJsp = true;
    private boolean usedByPerl = true;
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public void setUsedBy(String usedBy) {
        if (usedBy == null || usedBy.trim().length() == 0) return;
        
        usedByModel = usedByJsp = usedByPerl = false;
        String[] parts = usedBy.split(",");
        for (String part : parts) {
            part = part.trim();
            if ("model".equalsIgnoreCase(part)) usedByModel = true;
            else if ("jsp".equalsIgnoreCase(part)) usedByJsp = true;
            else if ("perl".equalsIgnoreCase(part)) usedByPerl = true;
        }
    }

    /**
     * @return the usedByModel
     */
    public boolean isUsedByModel() {
        return usedByModel;
    }

    /**
     * @return the usedByJsp
     */
    public boolean isUsedByJsp() {
        return usedByJsp;
    }

    /**
     * @return the usedByPerl
     */
    public boolean isUsedByPerl() {
        return usedByPerl;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException,
            WdkUserException {
        // do nothing
    }

}
