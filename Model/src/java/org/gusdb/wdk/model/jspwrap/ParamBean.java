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
        try {
            return param.getDefault();
        } catch (WdkModelException e) {
            throw new RuntimeException(e);
        }
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
     * @return
     * @see org.gusdb.wdk.model.Param#getGroup()
     */
    public GroupBean getGroup() {
        return new GroupBean(param.getGroup());
    }

    /**
     * for controller
     */
    public String validateValue(Object val) throws WdkModelException {
        return param.validateValue(val);
    }

    /**
     * @param value
     * @return
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.Param#compressValue(java.lang.Object)
     */
    public String compressValue(Object value) throws WdkModelException {
        return param.compressValue(value);
    }
    

    /**
     * @param value
     * @return
     * @throws WdkModelException
     */
    public Object decompressValue(String value) throws WdkModelException {
        return param.decompressValue(value);
    }
}
