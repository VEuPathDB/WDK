/**
 * 
 */
package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class RenameStepForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 1367369312936706460L;
    private String userAnswerId;
    private String customName;
    
    /**
     * @return Returns the customName.
     */
    public String getCustomName() {
        return customName;
    }
    
    /**
     * @param customName The customName to set.
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
    /**
     * @return Returns the userAnswerId.
     */
    public String getStepId() {
        return userAnswerId;
    }
    
    /**
     * @param userAnswerId The userAnswerId to set.
     */
    public void setStepId(String userAnswerId) {
        this.userAnswerId = userAnswerId;
    }
    
}
