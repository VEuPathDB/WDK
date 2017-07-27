/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.NumberParam;

/**
 * @author jerric
 * 
 */
public class NumberParamBean extends ParamBean<NumberParam> {

    private NumberParam _param;
    /**
     * @param param
     */
    public NumberParamBean(NumberParam param) {
        super(param);
        _param = param;
    }

    public Integer getPrecision() {
        return _param.getNumDecimalPlaces();
    }
    
    public boolean getInteger() {
        return _param.isInteger();
    }
    
    public Double getMin() {
      return _param.getMin();
    }
    
    public Double getMax() {
      return _param.getMax();
    }

    public String getRegex() {
        return _param.getRegex();
    }
}
