/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.record.Field;


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
     * @see org.gusdb.wdk.model.record.Field#getDisplayName()
     */
    public String getDisplayName() {
        return field.getDisplayName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getHelp()
     */
    public String getHelp() {
        return field.getHelp();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getInReportMaker()
     */
    public boolean isInReportMaker() {
        return field.isInReportMaker();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getInternal()
     */
    public boolean isInternal() {
        return field.isInternal();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getName()
     */
    public String getName() {
        return field.getName();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getTruncateTo()
     */
    public int getTruncateTo() {
        return field.getTruncateTo();
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#getType()
     */
    public String getType() {
        return field.getType();
    }

    public RecordClassBean getRecordClass() {
      return new RecordClassBean(field.getRecordClass());
    }

    /**
     * @return
     * @see org.gusdb.wdk.model.record.Field#toString()
     */
    @Override
    public String toString() {
        return field.toString();
    }
}
