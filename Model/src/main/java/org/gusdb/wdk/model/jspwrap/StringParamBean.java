/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.StringParam;

/**
 * @author jerric
 * 
 */
public class StringParamBean extends ParamBean<StringParam> {

    /**
     * @param param
     */
    public StringParamBean(StringParam param) {
        super(param);
    }

    public int getLength() {
        return param.getLength();
    }
    
    public boolean getMultiLine() {
        return param.getMultiLine();
    }
}
