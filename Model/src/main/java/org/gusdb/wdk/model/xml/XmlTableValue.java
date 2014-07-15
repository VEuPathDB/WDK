/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlTableValue {

    private XmlTableField tableField;
    private String name;
    private List<XmlRowValue> rows;

    /**
     * 
     */
    public XmlTableValue() {
        super();
        rows = new ArrayList<XmlRowValue>();
    }

    /**
     * @return Returns the tableField.
     */
    public XmlTableField getTableField() {
        return this.tableField;
    }

    /**
     * this method will called by XmlRecordInstance
     * 
     * @param tableField
     *                The tableField to set.
     */
    public void setTableField(XmlTableField tableField) {
        this.tableField = tableField;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getColumns()
     */
    public XmlAttributeField[] getAttributeFields() {
        return this.tableField.getAttributeFields();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getDisplayName()
     */
    public String getDisplayName() {
        return this.tableField.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getHelp()
     */
    public String getHelp() {
        return this.tableField.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getIsInternal()
     */
    public boolean isInternal() {
        return this.tableField.isInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableField#getName()
     */
    public String getName() {
        if (tableField == null) return name;
        else return tableField.getName();
    }

    /**
     * Return an array of all rows in this table. Each row is a map of column
     * name and xmlAttribute values
     * 
     * @return
     */
    public XmlRowValue[] getRows() {
        XmlRowValue[] rowArray = new XmlRowValue[rows.size()];
        rows.toArray(rowArray);
        return rowArray;
    }

    public void addRow(XmlRowValue row) {
        this.rows.add(row);
    }

    public int size() {
        return rows.size();
    }

    /**
     * @return print out the complete information of the table
     */
    public String print() {
        StringBuffer buf = new StringBuffer(this.getClass().getName());
        buf.append(":\tname='");
        buf.append(getName());
        buf.append("'\r\n\tdisplayName='");
        buf.append(getDisplayName());
        buf.append("'\r\n\thelp='");
        buf.append(getHelp());
        buf.append("'\r\n");
        // print the table fields & values
        buf.append(toString());
        return buf.toString();
    }

    /*
     * (non-Javadoc) print out the table values in tabular format
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        // print out field names
        XmlAttributeField[] fields = getAttributeFields();
        for (XmlAttributeField field : fields) {
            sb.append(field.getName());
            sb.append("\t");
        }
        sb.append("\r\n");

        // print out the row information
        for (XmlRowValue row : rows) {
            sb.append(row.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
