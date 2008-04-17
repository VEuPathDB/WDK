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
     */
    public EnumParamBean getSubTypeParam() {
        return new EnumParamBean(subType.getSubTypeParam());
    }
}
