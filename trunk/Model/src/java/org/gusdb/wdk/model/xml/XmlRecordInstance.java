/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlRecordInstance {

    private static long recordCount = 0;

    private String recordID;
    private String projectID;
    private String projectName;
    private XmlRecordClass recordClass;
    private Map<String, XmlAttributeValue> attributes;
    private Map<String, XmlTableValue> tables;

    /**
     * 
     */
    public XmlRecordInstance() {
        // initialize a unique ID for this record
        recordID = Long.toString(++recordCount);
        attributes = new HashMap<String, XmlAttributeValue>();
        tables = new HashMap<String, XmlTableValue>();
    }

    /**
     * @return Returns the recordClass.
     */
    public XmlRecordClass getRecordClass() {
        return this.recordClass;
    }

    /**
     * this method is called by the XmlAnswer in setting resource stage
     * 
     * @param recordClass The recordClass to set.
     */
    public void setRecordClass(XmlRecordClass recordClass) {
        this.recordClass = recordClass;
    }

    /**
     * @return Returns the recordID.
     */
    public String getId() {
        return this.recordID;
    }

    /**
     * @param recordID The recordID to set.
     */
    public void setId(String recordID) {
        this.recordID = recordID;
    }

    /**
     * @return Returns the projectID.
     */
    public String getProjectID() {
        return this.projectID;
    }

    /**
     * @return Returns the projectName.
     */
    public String getProjectName() {
        return this.projectName;
    }

    public void setProject(String projectID, String projectName) {
        this.projectID = projectID;
        this.projectName = projectName;
    }

    public XmlAttributeValue[] getAttributes() {
        XmlAttributeValue[] attrArray = new XmlAttributeValue[attributes.size()];
        attributes.values().toArray(attrArray);
        return attrArray;
    }

    public XmlAttributeValue getAttribute(String name) throws WdkModelException {
        XmlAttributeValue attribute = attributes.get(name);
        if (attribute == null)
            throw new WdkModelException("Attribute " + name + " not found in "
                    + recordClass.getName() + "#" + recordID);
        return attribute;
    }

    public void addAttribute(XmlAttributeValue attribute) {
        this.attributes.put(attribute.getName(), attribute);
    }

    public XmlTableValue[] getTables() {
        XmlTableValue[] tabArray = new XmlTableValue[tables.size()];
        tables.values().toArray(tabArray);
        return tabArray;
    }

    public XmlTableValue getTable(String name) throws WdkModelException {
        XmlTableValue table = tables.get(name);
        if (table == null)
            throw new WdkModelException("Table " + name + " not found in "
                    + recordClass.getName() + "#" + recordID);
        return table;
    }

    public void addTable(XmlTableValue table) {
        tables.put(table.getName(), table);
    }

    public XmlAttributeValue[] getSummaryAttributes() {
        List<XmlAttributeValue> summaries = new ArrayList<XmlAttributeValue>();
        for (XmlAttributeValue attribute : attributes.values()) {
            if (attribute.isSummary()) summaries.add(attribute);
        }
        XmlAttributeValue[] attrArray = new XmlAttributeValue[summaries.size()];
        summaries.toArray(attrArray);
        return attrArray;
    }

    public XmlAttributeValue[] getNonSummaryAttributes() {
        List<XmlAttributeValue> nonSummaries = new ArrayList<XmlAttributeValue>();
        for (XmlAttributeValue attribute : attributes.values()) {
            if (!attribute.isSummary()) nonSummaries.add(attribute);
        }
        XmlAttributeValue[] attrArray = new XmlAttributeValue[nonSummaries.size()];
        nonSummaries.toArray(attrArray);
        return attrArray;
    }

    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
        // resolve attribute fields
        for (XmlAttributeValue attribute : attributes.values()) {
            XmlAttributeField field = 
		recordClass.getAttributeField(attribute.getName());
            attribute.setAttributeField(field);
        }
        // resolve table fields
        for (XmlTableValue table : tables.values()) {
            XmlTableField tableField = 
		recordClass.getTableField(table.getName());
            table.setTableField(tableField);

            // resolve column fields
            for (XmlRowValue row : table.getRows()) {
                for (XmlAttributeValue column : row.getColumns()) {
                    XmlAttributeField colField = 
			tableField.getAttributeField(column.getName());
                    column.setAttributeField(colField);
                }
            }
        }

        for (XmlTableField field : recordClass.getTableFields()) {
            if (!tables.containsKey(field.getName())) continue;

            // check column consistency
            XmlTableValue table = tables.get(field.getName());
            XmlAttributeField[] columns = field.getAttributeFields();
            for (XmlAttributeField column : columns) {
                for (XmlRowValue row : table.getRows()) {
                    if (!row.hasColumn(column.getName()))
                        throw new WdkModelException("Column "
                                + column.getName() + " defined in the table "
                                + table.getName() + " of Record Class, but "
                                + "not found in the table of the record "
                                + recordID);
                }
            }
        }
    }

    /**
     * @return print out the complete information of a reord
     */
    public String print() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append(":\r\n");

        // print out summary attribute values line by line
        XmlAttributeValue[] summaries = getSummaryAttributes();
        sb.append("========== Summary Attributes ==========\r\n");
        for (XmlAttributeValue attribute : summaries) {
            sb.append(attribute.toString());
            sb.append("\r\n");
        }

        // print out summary attribute values line by line
        XmlAttributeValue[] nonSummaries = getNonSummaryAttributes();
        sb.append("\r\n========== Non-Summary Attributes ==========\r\n");
        for (XmlAttributeValue attribute : nonSummaries) {
            sb.append(attribute.toString());
            sb.append("\r\n");
        }

        // print out table attributes
        XmlTableValue[] tables = getTables();
        sb.append("\r\n========== Table Attributes ==========\r\n");
        for (XmlTableValue table : tables) {
            sb.append("Table : ");
            sb.append(table.getName());
            sb.append("\r\n");
            sb.append(table.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    /**
     * @return print out the names and values of summary attributes, in tabular
     *         format
     */
    public String printSummary() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append(":\r\n\t");

        // print out summary attribute values line by line
        XmlAttributeValue[] summaries = getSummaryAttributes();
        for (XmlAttributeValue attribute : summaries) {
            sb.append(attribute.getName());
            sb.append("\t");
        }
        sb.append("\r\n\t");
        for (XmlAttributeValue attribute : summaries) {
            sb.append(attribute.getValue());
            sb.append("\t");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /*
     * (non-Javadoc) just print out the summary attribute values, in one line,
     * split by tab
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        XmlAttributeValue[] summaries = getSummaryAttributes();
        for (XmlAttributeValue attribute : summaries) {
            sb.append(attribute.getValue());
            sb.append("\t");
        }
        return sb.toString().trim();
    }
}
