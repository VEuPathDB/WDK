/**
 * 
 */
package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class RenameStrategyForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 1367369312936706460L;
    private String strategyId;
    private String name;
    
    /**
     * @return Returns the customName.
     */
    public String name() {
        return name;
    }
    
    /**
     * @param customName The customName to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return Returns the historyId.
     */
    public String getStrategyId() {
        return strategyId;
    }
    
    /**
     * @param historyId The historyId to set.
     */
    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }
    
}
