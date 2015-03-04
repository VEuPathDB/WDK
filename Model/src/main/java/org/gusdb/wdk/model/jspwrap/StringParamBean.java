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

    private StringParam _param;
    /**
     * @param param
     */
    public StringParamBean(StringParam param) {
        super(param);
        _param = param;
    }

    public int getLength() {
        return _param.getLength();
    }
    
    public boolean getMultiLine() {
        return _param.getMultiLine();
    }

    public String getRegex() {
        return _param.getRegex();
    }
}
