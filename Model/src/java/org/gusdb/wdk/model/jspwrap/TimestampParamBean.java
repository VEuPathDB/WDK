/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.text.DateFormat;
import java.util.Date;

import org.gusdb.wdk.model.query.param.TimestampParam;

/**
 * @author xingao
 *
 */
public class TimestampParamBean extends ParamBean {

    /**
     * @param param
     */
    public TimestampParamBean(TimestampParam param) {
        super(param);
    }

    public String getNewDateTime() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }
}
