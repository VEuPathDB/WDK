/**
 * 
 */
package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class RenameUserAnswerForm extends ActionForm {

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
    public String getUserAnswerId() {
        return userAnswerId;
    }
    
    /**
     * @param userAnswerId The userAnswerId to set.
     */
    public void setUserAnswerId(String userAnswerId) {
        this.userAnswerId = userAnswerId;
    }
    
}
