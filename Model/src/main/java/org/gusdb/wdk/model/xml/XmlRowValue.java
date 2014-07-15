/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 14, 2005
 */
public class XmlRowValue {

    Map<String, XmlAttributeValue> columns;

    /**
     * 
     */
    public XmlRowValue() {
        columns = new LinkedHashMap<String, XmlAttributeValue>();
    }

    public void addColumn(XmlAttributeValue column) {
        columns.put(column.getName(), column);
    }

    public XmlAttributeValue[] getColumns() {
        XmlAttributeValue[] colArray = new XmlAttributeValue[columns.size()];
        columns.values().toArray(colArray);
        return colArray;
    }

    public XmlAttributeValue getColumn(String name) throws WdkModelException {
        XmlAttributeValue column = columns.get(name);
        if (column == null)
            throw new WdkModelException("The column of name " + name
                    + " not exists!");
        return column;
    }
    
    public boolean hasColumn(String name) {
        return columns.containsKey(name);
    }

    /**
     * @return print out the attribute in this row, line by line
     * 
     */
    public String print() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append(": ");
        for (XmlAttributeValue attribute : columns.values()) {
            sb.append("\r\n\t");
            sb.append(attribute.toString()); // the name/value tuple
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /*
     * (non-Javadoc) print out the values of the attributes only, in one line
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (XmlAttributeValue attribute : columns.values()) {
            sb.append(attribute.getValue());
            sb.append("\t");
        }
        return sb.toString().trim();
    }
}
