/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlRecordClass {

    private String name;
    private String type;
    private String idPrefix;
    private String delimiter;
    private String attributeOrdering;

    private Map<String, XmlAttributeField> attributeFields;
    private Map<String, XmlTableField> tableFields;

    private XmlRecordClassSet recordClassSet;

    /**
     * 
     */
    public XmlRecordClass() {
        attributeFields = new LinkedHashMap<String, XmlAttributeField>();
        tableFields = new LinkedHashMap<String, XmlTableField>();
    }

    /**
     * @return Returns the attributeOrdering.
     */
    public String getAttributeOrdering() {
        return this.attributeOrdering;
    }

    /**
     * @param attributeOrdering The attributeOrdering to set.
     */
    public void setAttributeOrdering(String attributeOrdering) {
        this.attributeOrdering = attributeOrdering;
    }

    /**
     * @return Returns the delimiter.
     */
    public String getDelimiter() {
        return this.delimiter;
    }

    /**
     * @param delimiter The delimiter to set.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * @return Returns the idPrefix.
     */
    public String getIdPrefix() {
        return this.idPrefix;
    }

    /**
     * @param idPrefix The idPrefix to set.
     */
    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    public String getFullName() {
        if (recordClassSet == null) return getName();
        else return recordClassSet.getName() + "." + getName();
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    public void addAttributeField(XmlAttributeField field) {
        attributeFields.put(field.getName(), field);
    }

    public XmlAttributeField getAttributeField(String name)
            throws WdkModelException {
        XmlAttributeField field = attributeFields.get(name);
        if (field == null)
            throw new WdkModelException("Attempting to access an attribute '" +
					name + "' of XmlRecordClass " + 
					getFullName() + " but it has none.");
        return field;
    }

    public XmlAttributeField[] getAttributeFields() {
        XmlAttributeField[] fields = new XmlAttributeField[attributeFields.size()];
        attributeFields.values().toArray(fields);
        return fields;
    }

    public void addTableField(XmlTableField field) {
        tableFields.put(field.getName(), field);
    }

    public XmlTableField getTableField(String name) throws WdkModelException {
        XmlTableField field = tableFields.get(name);
        if (field == null)
            throw new WdkModelException("Table field not found: " + name);
        return field;
    }

    public XmlTableField[] getTableFields() {
        XmlTableField[] fields = new XmlTableField[tableFields.size()];
        tableFields.values().toArray(fields);
        return fields;
    }

    public XmlRecordClassSet getRecordClassSet() {
        return recordClassSet;
    }

    /**
     * this method is called by XmlRecordClassSet in setting resource stage
     * 
     * @param recordClassSet
     */
    public void setRecordClassSet(XmlRecordClassSet recordClassSet) {
        this.recordClassSet = recordClassSet;
    }

    public void resolveReferences(WdkModel model) {
    // do nothing at this time
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlRecordClass: name='");
        buf.append(name);
        buf.append("'\r\n--- Attributes ---");
        for (XmlAttributeField attribute : attributeFields.values()) {
            buf.append("\r\n");
            buf.append(attribute.getName());
        }
        buf.append("\r\n--- Tables ---");
        for (XmlTableField table : tableFields.values()) {
            buf.append("\r\n");
            buf.append(table);
        }
        buf.append("\r\n");
        return buf.toString();
    }
}
