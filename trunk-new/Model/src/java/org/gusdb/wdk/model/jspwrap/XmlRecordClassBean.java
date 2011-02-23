/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlTableField;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlRecordClassBean {

    private XmlRecordClass recordClass;

    /**
     * 
     */
    public XmlRecordClassBean(XmlRecordClass recordClass) {
        this.recordClass = recordClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClass#getAttributeFields()
     */
    public XmlAttributeFieldBean[] getAttributeFields() {
        XmlAttributeField[] fields = recordClass.getAttributeFields();
        XmlAttributeFieldBean[] fieldBeans = new XmlAttributeFieldBean[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldBeans[i] = new XmlAttributeFieldBean(fields[i]);
        }
        return fieldBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClass#getFullName()
     */
    public String getFullName() {
        return this.recordClass.getFullName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClass#getTableFields()
     */
    public XmlTableFieldBean[] getTableFields() {
        XmlTableField[] fields = recordClass.getTableFields();
        XmlTableFieldBean[] fieldBeans = new XmlTableFieldBean[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldBeans[i] = new XmlTableFieldBean(fields[i]);
        }
        return fieldBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordClass#getType()
     */
    public String getType() {
        return this.recordClass.getType();
    }
}
