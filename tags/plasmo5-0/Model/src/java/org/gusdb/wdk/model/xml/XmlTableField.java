/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.FieldI;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlTableField implements FieldI {

    private String name;
    private String displayName;
    private String help;
    private String type;
    private boolean isInternal;
    private int truncate;

    private Map<String, XmlAttributeField> columns;

    public XmlTableField() {
        isInternal = false;
        columns = new HashMap<String, XmlAttributeField>();
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

    public void addColumn(XmlAttributeField column) {
        columns.put(column.getName(), column);
    }

    public XmlAttributeField getColumn(String name) throws WdkModelException {
        XmlAttributeField column = columns.get(name);
        if (column == null)
            throw new WdkModelException("The column of name " + name
                    + " not exists!");
        return column;
    }

    public XmlAttributeField[] getColumns() {
        XmlAttributeField[] cols = new XmlAttributeField[columns.size()];
        columns.values().toArray(cols);
        return cols;
    }

    public int size() {
        return columns.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(" : ");
        for (XmlAttributeField column : columns.values()) {
            sb.append(column.getName());
            sb.append(", ");
        }
        if (sb.toString().endsWith(", "))
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }
}
