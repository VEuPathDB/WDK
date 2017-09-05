package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.NumberRangeParam;

public class NumberRangeParamBean extends ParamBean<NumberRangeParam> {

    /**
     * @param param
     */
    public NumberRangeParamBean(NumberRangeParam param) {
        super(param);
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

    public Double getStep() {
        return _param.getStep();
    }

    public String getRegex() {
        return _param.getRegex();
    }
}
