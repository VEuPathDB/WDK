/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.SubType;

/**
 * @author xingao
 *
 */
public class SubTypeBean {

    private SubType subType;
    
    public SubTypeBean(SubType subType) {
        this.subType = subType;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.SubType#getDisplayName()
     */
    public String getDisplayName() {
        return subType.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.SubType#getName()
     */
    public String getName() {
        return subType.getName();
    }
}
