/**
 * 
 */
package org.gusdb.wdk.model.xml;

import org.gusdb.wdk.model.FieldI;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlAttributeField implements FieldI {

    private String name;
    private String displayName;
    private String help;
    private String type;
    private boolean isInternal;
    private int truncate;

    public XmlAttributeField() {
        isInternal = false;
        truncate = 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getName()
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getHelp()
     */
    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getType()
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getIsInternal()
     */
    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean internal) {
        this.isInternal = internal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.FieldI#getTruncate()
     */
    public Integer getTruncate() {
        return truncate;
    }

    public void setTruncate(Integer truncate) {
        this.truncate = truncate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm);
        buf.append(": name='");
        buf.append(getName());
        buf.append("'\r\n  displayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n  help='");
        buf.append(getHelp());
        buf.append("'\r\n");
        return buf.toString();
    }
}