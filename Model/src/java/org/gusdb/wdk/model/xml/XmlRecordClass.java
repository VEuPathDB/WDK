/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlRecordClass extends WdkModelBase {

    private String name;
    private String type;
    private String idPrefix;
    private String delimiter;
    private String attributeOrdering;

    private List<XmlAttributeField> attributeFieldList = new ArrayList<XmlAttributeField>();
    private Map<String, XmlAttributeField> attributeFields = new LinkedHashMap<String, XmlAttributeField>();

    private List<XmlTableField> tableFieldList = new ArrayList<XmlTableField>();
    private Map<String, XmlTableField> tableFields = new LinkedHashMap<String, XmlTableField>();

    private XmlRecordClassSet recordClassSet;

    /**
     * @return Returns the attributeOrdering.
     */
    public String getAttributeOrdering() {
        return this.attributeOrdering;
    }

    /**
     * @param attributeOrdering
     * The attributeOrdering to set.
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
     * @param delimiter
     * The delimiter to set.
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
     * @param idPrefix
     * The idPrefix to set.
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
     * @param name
     * The name to set.
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
     * @param type
     * The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    public void addAttributeField(XmlAttributeField field) {
        attributeFieldList.add(field);
    }

    public XmlAttributeField getAttributeField(String name)
            throws WdkModelException {
        XmlAttributeField field = attributeFields.get(name);
        if (field == null)
            throw new WdkModelException("Attempting to access an attribute '"
                    + name + "' of XmlRecordClass " + getFullName()
                    + " but it has none.");
        return field;
    }

    public XmlAttributeField[] getAttributeFields() {
        XmlAttributeField[] fields = new XmlAttributeField[attributeFields.size()];
        attributeFields.values().toArray(fields);
        return fields;
    }

    public void addTableField(XmlTableField field) {
        tableFieldList.add(field);
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

    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude attribute field
        for (XmlAttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlAttributeField "
                            + fieldName + " is duplicated in xmlRecordClass "
                            + this.getFullName());
                attributeFields.put(fieldName, field);
            }
        }
        attributeFieldList = null;

        // exclude table fields
        for (XmlTableField field : tableFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (tableFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlTableField "
                            + fieldName + " is duplicated in xmlRecordClass "
                            + this.getFullName());
                tableFields.put(fieldName, field);
            }
        }
        tableFieldList = null;
    }
}
