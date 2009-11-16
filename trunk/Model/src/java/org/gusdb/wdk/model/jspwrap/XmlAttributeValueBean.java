/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.xml.XmlAttributeValue;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlAttributeValueBean {

    private XmlAttributeValue attribute;

    /**
     * 
     */
    public XmlAttributeValueBean(XmlAttributeValue attribute) {
        this.attribute = attribute;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getAttributeField()
     */
    public XmlAttributeFieldBean getAttributeField() {
        return new XmlAttributeFieldBean(attribute.getAttributeField());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getBriefValue()
     */
    public String getBriefValue() {
        return this.attribute.getBriefValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getDisplayName()
     */
    public String getDisplayName() {
        return this.attribute.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getHelp()
     */
    public String getHelp() {
        return this.attribute.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getIsInternal()
     */
    public boolean isInternal() {
        return this.attribute.isInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getName()
     */
    public String getName() {
        return this.attribute.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#getValue()
     */
    public String getValue() {
        return this.attribute.getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeValue#isSummary()
     */
    public boolean isSummary() {
        return this.attribute.isSummary();
    }
}
