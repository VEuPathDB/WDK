/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.Utilities;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlAttributeValue {

    private String name;
    private XmlAttributeField attributeField;
    private String value;
    private boolean isSummary;

    /**
     * 
     */
    public XmlAttributeValue() {
        isSummary = true;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return this.value;
    }

    public String getBriefValue() {
        // prepare truncation length
        int truncate = attributeField.getTruncateTo();
        if (truncate == 0) truncate = Utilities.TRUNCATE_DEFAULT;

        if (value == null || truncate >= value.length()) return value;
        else return value.substring(0, truncate) + ". . .";
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return Returns the attributeField.
     */
    public XmlAttributeField getAttributeField() {
        return this.attributeField;
    }

    /**
     * This function is called by XmlRecordInstance in setting resource stage
     * 
     * @param attributeField
     */
    public void setAttributeField(XmlAttributeField attributeField) {
        this.attributeField = attributeField;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getDisplayName()
     */
    public String getDisplayName() {
        return this.attributeField.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getHelp()
     */
    public String getHelp() {
        return this.attributeField.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getIsInternal()
     */
    public boolean isInternal() {
        return this.attributeField.isInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlAttributeField#getName()
     */
    public String getName() {
        if (attributeField == null) return name;
        else return attributeField.getName();
    }

    /**
     * @return Returns the isSummary.
     */
    public boolean isSummary() {
        return this.isSummary;
    }

    /**
     * This method will be called by XmlAnswer
     * @param isSummary The isSummary to set.
     */
    public void setSummary(boolean isSummary) {
        this.isSummary = isSummary;
    }
    
    /**
     * @return print out the complete information of an attribute, line by line
     */
    public String print() {
        StringBuffer buf = new StringBuffer(this.getClass().getName());
        buf.append(":\r\n\tname='");
        buf.append(getName());
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\thelp='");
        buf.append(getHelp());
        buf.append("'\r\n\tisSummary?='");
        buf.append(isSummary());
        buf.append("'\r\n\tvalue='");
        buf.append(getValue());
        buf.append("'\r\n");
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * print out the name/value tuple only, in one line
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(" : ");
        sb.append(getValue());
        return sb.toString();
    }
}
