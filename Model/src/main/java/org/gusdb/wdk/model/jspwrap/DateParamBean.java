package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.DateParam;

/**
 * @author jerric
 */
public class DateParamBean extends ParamBean<DateParam> {

    /**
     * @param param
     */
    public DateParamBean(DateParam param) {
        super(param);
    }

    public String getMinDate() {
        return _param.getMinDate();
    }

    public String getMaxDate() {
        return _param.getMaxDate();
    }

    public String getRegex() {
        return _param.getRegex();
    }
}
