/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.xml.XmlAttributeValue;
import org.gusdb.wdk.model.xml.XmlRowValue;
import org.gusdb.wdk.model.xml.XmlTableValue;

/**
 * @author Jerric
 * @created Oct 19, 2005
 */
public class XmlTableValueBean {

    private XmlTableValue table;

    /**
     * 
     */
    public XmlTableValueBean(XmlTableValue table) {
        this.table = table;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getColumns()
     */
    public XmlAttributeFieldBean[] getAttributeFields() {
        XmlTableFieldBean field = new XmlTableFieldBean(table.getTableField());
        return field.getAttributeFields();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getDisplayName()
     */
    public String getDisplayName() {
        return this.table.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getHelp()
     */
    public String getHelp() {
        return this.table.getHelp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getIsInternal()
     */
    public boolean isInternal() {
        return this.table.isInternal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getName()
     */
    public String getName() {
        return this.table.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getRows()
     */
    public List<XmlAttributeValueBean[]> getRows() {
        List<XmlAttributeValueBean[]> rows = new ArrayList<XmlAttributeValueBean[]>();
        for (XmlRowValue row : table.getRows()) {
            XmlAttributeValue[] columns = row.getColumns();
            XmlAttributeValueBean[] rowBean = new XmlAttributeValueBean[columns.length];
            for (int i = 0; i < columns.length; i++) {
                rowBean[i] = new XmlAttributeValueBean(columns[i]);
            }
            rows.add(rowBean);
        }
        return rows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.xml.XmlTableValue#getTableField()
     */
    public XmlTableFieldBean getTableField() {
        return new XmlTableFieldBean(table.getTableField());
    }
}
