/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.NumberRangeParam;


public class NumberRangeParamBean extends ParamBean<NumberRangeParam> {

    private NumberRangeParam _param;
    /**
     * @param param
     */
    public NumberRangeParamBean(NumberRangeParam param) {
        super(param);
        _param = param;
    }

    public Integer getPrecision() {
        return _param.getNumDecimalPlaces();
    }
    
    public boolean getInteger() {
        return _param.isInteger();
    }
    
    public Long getMin() {
      return _param.getMin();
    }
    
    public Long getMax() {
      return _param.getMax();
    }

    public String getRegex() {
        return _param.getRegex();
    }
}
