package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.WdkModelException;

/**
 * A wrapper on a {@link Param} that provides simplified access for consumption
 * by a view
 */
public class ParamBean {

    protected Param param;

    public ParamBean(Param param) {
        this.param = param;
    }

    public String getName() {
        return param.getName();
    }

    public String getId() {
        return param.getId();
    }

    public String getFullName() {
        return param.getFullName();
    }

    public String getPrompt() {
        return param.getPrompt();
    }

    public String getHelp() {
        return param.getHelp();
    }

    public String getDefault() {
        return param.getDefault();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isReadonly()
     */
    public boolean getIsReadonly() {
        return this.param.isReadonly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#isVisible()
     */
    public boolean getIsVisible() {
        return this.param.isVisible();
    }
    

    /**
     * for controller
     */
    public String validateValue(Object val) throws WdkModelException {
        return param.validateValue(val);
    }
}
