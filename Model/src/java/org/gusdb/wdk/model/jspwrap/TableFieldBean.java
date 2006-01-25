/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.TableField;

/**
 * @author  Jerric
 * @created Jan 23, 2006
 */
public class TableFieldBean {

    private TableField field;
    
    /**
     * 
     */
    public TableFieldBean(TableField field) {
        this.field = field;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.TableField#getAttributeFieldMap()
     */
    public Map<String, AttributeFieldBean> getAttributeFieldMap() {
        AttributeField[] fields = field.getAttributeFields();
        Map<String, AttributeFieldBean> fieldBeans = 
            new LinkedHashMap<String, AttributeFieldBean>(fields.length);
        for (AttributeField attributeField : fields) {
            fieldBeans.put(field.getName(), new AttributeFieldBean(attributeField));
        }
        return fieldBeans;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.TableField#getAttributeFields()
     */
    public AttributeFieldBean[] getAttributeFields() {
        AttributeField[] fields = field.getAttributeFields();
        AttributeFieldBean[] fieldBeans = new AttributeFieldBean[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldBeans[i] = new AttributeFieldBean(fields[i]);
        }
        return fieldBeans;
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
     * @see org.gusdb.wdk.model.TableField#getTruncateTo()
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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#toString()
     */
    public String toString() {
        return this.field.toString();
    }

}
