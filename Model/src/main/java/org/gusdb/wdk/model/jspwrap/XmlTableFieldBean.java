/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlTableField;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlTableFieldBean {

    private XmlTableField field;

    /**
     * 
     */
    public XmlTableFieldBean(XmlTableField field) {
        this.field = field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getColumns()
     */
    public XmlAttributeFieldBean[] getAttributeFields() {
        XmlAttributeField[] fields = this.field.getAttributeFields();
        XmlAttributeFieldBean[] beans = new XmlAttributeFieldBean[fields.length];
        for (int i = 0; i < fields.length; i++) {
            beans[i] = new XmlAttributeFieldBean(fields[i]);
        }
        return beans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getDisplayName()
     */
    public String getDisplayName() {
        return this.field.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getHelp()
     */
    public String getHelp() {
        return this.field.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getIsInternal()
     */
    public boolean isInternal() {
        return this.field.isInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getName()
     */
    public String getName() {
        return this.field.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getTruncate()
     */
    public Integer getTruncateTo() {
        return this.field.getTruncateTo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getType()
     */
    public String getType() {
        return this.field.getType();
    }

}
