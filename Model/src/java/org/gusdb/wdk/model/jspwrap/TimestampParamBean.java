/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.query.param.TimestampParam;

/**
 * @author xingao
 *
 */
public class TimestampParamBean extends ParamBean<TimestampParam> {

  private final TimestampParam timestampParam;
  
    /**
     * @param param
     */
    public TimestampParamBean(TimestampParam param) {
        super(param);
        this.timestampParam = param;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.query.param.TimestampParam#getInterval()
     */
    public long getInterval() {
      return timestampParam.getInterval();
    }
}
