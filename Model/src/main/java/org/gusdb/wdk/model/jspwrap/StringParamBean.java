package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.StringParam;

/**
 * @author jerric
 */
public class StringParamBean extends ParamBean<StringParam> {

    /**
     * @param param
     */
    public StringParamBean(StringParam param) {
        super(param);
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

    public boolean isNumber() { return _param.isNumber(); }
}
