/**
 * 
 */
package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class RenameHistoryForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 1367369312936706460L;
    private String historyId;
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
     * @return Returns the historyId.
     */
    public String getHistoryId() {
        return historyId;
    }
    
    /**
     * @param historyId The historyId to set.
     */
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }
    
}
