/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.DateParam;

/**
 * @author jerric
 * 
 */
public class DateParamBean extends ParamBean<DateParam> {

    private DateParam _param;
    /**
     * @param param
     */
    public DateParamBean(DateParam param) {
        super(param);
        _param = param;
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
