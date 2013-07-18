/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlAttributeValue;
import org.gusdb.wdk.model.xml.XmlRecordInstance;
import org.gusdb.wdk.model.xml.XmlTableValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlRecordBean {

    private XmlRecordInstance record;

    /**
     * 
     */
    public XmlRecordBean(XmlRecordInstance record) {
        this.record = record;
    }

    public String getId() {
	return record.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordInstance#getAttributes()
     */
    public XmlAttributeValueBean[] getAttributes() {
        XmlAttributeValue[] attrs = record.getAttributes();
        XmlAttributeValueBean[] attrBeans = new XmlAttributeValueBean[attrs.length];
        for (int i = 0; i < attrs.length; i++) {
            attrBeans[i] = new XmlAttributeValueBean(attrs[i]);
        }
        return attrBeans;
    }

    public Map<String, String> getAttributesMap() {
	XmlAttributeValueBean[] attrs = getAttributes();
	Map<String, String> attMap = new LinkedHashMap<String, String>();
	for (int i=0; i<attrs.length; i++) {
	    XmlAttributeValueBean att = attrs[i];
	    attMap.put(att.getName(), att.getValue());
	}
	return attMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordInstance#getNonSummaryAttributes()
     */
    public XmlAttributeValueBean[] getNonSummaryAttributes() {
        XmlAttributeValue[] attrs = record.getNonSummaryAttributes();
        XmlAttributeValueBean[] attrBeans = new XmlAttributeValueBean[attrs.length];
        for (int i = 0; i < attrs.length; i++) {
            attrBeans[i] = new XmlAttributeValueBean(attrs[i]);
        }
        return attrBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordInstance#getRecordClass()
     */
    public XmlRecordClassBean getRecordClass() {
        return new XmlRecordClassBean(record.getRecordClass());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordInstance#getSummaryAttributes()
     */
    public XmlAttributeValueBean[] getSummaryAttributes() {
        XmlAttributeValue[] attrs = record.getSummaryAttributes();
        XmlAttributeValueBean[] attrBeans = new XmlAttributeValueBean[attrs.length];
        for (int i = 0; i < attrs.length; i++) {
            attrBeans[i] = new XmlAttributeValueBean(attrs[i]);
        }
        return attrBeans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlRecordInstance#getTables()
     */
    public XmlTableValueBean[] getTables() {
        XmlTableValue[] tables = record.getTables();
        XmlTableValueBean[] tableBeans = new XmlTableValueBean[tables.length];
        for (int i = 0; i < tables.length; i++) {
            tableBeans[i] = new XmlTableValueBean(tables[i]);
        }
        return tableBeans;
    }
}
