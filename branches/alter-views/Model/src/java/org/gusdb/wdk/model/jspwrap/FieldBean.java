/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Field;


/**
 * @author xingao
 *
 */
public abstract class FieldBean {

    protected Field field;
    
    protected FieldBean(Field field) {
        this.field = field;
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getDisplayName()
     */
    public String getDisplayName() {
        return field.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getHelp()
     */
    public String getHelp() {
        return field.getHelp();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getInReportMaker()
     */
    public boolean isInReportMaker() {
        return field.isInReportMaker();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getInternal()
     */
    public boolean isInternal() {
        return field.isInternal();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getName()
     */
    public String getName() {
        return field.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getTruncateTo()
     */
    public int getTruncateTo() {
        return field.getTruncateTo();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#getType()
     */
    public String getType() {
        return field.getType();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.Field#toString()
     */
    public String toString() {
        return field.toString();
    }
}
