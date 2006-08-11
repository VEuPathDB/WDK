/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.AttributeField;

public class AttributeFieldBean {

    private AttributeField field;

    /**
     * 
     */
    public AttributeFieldBean(AttributeField field) {
        this.field = field;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getDisplayName()
     */
    public String getDisplayName() {
        return this.field.getDisplayName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getHelp()
     */
    public String getHelp() {
        return this.field.getHelp();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getInReportMaker()
     */
    public boolean getInReportMaker() {
        return this.field.getInReportMaker();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getInternal()
     */
    public boolean getInternal() {
        return this.field.getInternal();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getName()
     */
    public String getName() {
        return this.field.getName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getTruncateTo()
     */
    public int getTruncateTo() {
        return this.field.getTruncateTo();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getType()
     */
    public String getType() {
        return this.field.getType();
    }

}
