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
 */
public class XmlRecordClass extends WdkModelBase {

    private String _name;
    private String _type;
    private String _idPrefix;
    private String _delimiter;
    private String _attributeOrdering;

    private List<XmlAttributeField> _attributeFieldList = new ArrayList<XmlAttributeField>();
    private Map<String, XmlAttributeField> _attributeFields = new LinkedHashMap<String, XmlAttributeField>();

    private List<XmlTableField> _tableFieldList = new ArrayList<XmlTableField>();
    private Map<String, XmlTableField> _tableFields = new LinkedHashMap<String, XmlTableField>();

    private XmlRecordClassSet _recordClassSet;

    /**
     * @return Returns the attributeOrdering.
     */
    public String getAttributeOrdering() {
        return _attributeOrdering;
    }

    /**
     * @param attributeOrdering
     * The attributeOrdering to set.
     */
    public void setAttributeOrdering(String attributeOrdering) {
        _attributeOrdering = attributeOrdering;
    }

    /**
     * @return Returns the delimiter.
     */
    public String getDelimiter() {
        return _delimiter;
    }

    /**
     * @param delimiter
     * The delimiter to set.
     */
    public void setDelimiter(String delimiter) {
        _delimiter = delimiter;
    }

    /**
     * @return Returns the idPrefix.
     */
    public String getIdPrefix() {
        return _idPrefix;
    }

    /**
     * @param idPrefix
     * The idPrefix to set.
     */
    public void setIdPrefix(String idPrefix) {
        _idPrefix = idPrefix;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    public String getFullName() {
        if (_recordClassSet == null) return getName();
        else return _recordClassSet.getName() + "." + getName();
    }

    /**
     * @param name
     * The name to set.
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return _type;
    }

    /**
     * @param type
     * The type to set.
     */
    public void setType(String type) {
        _type = type;
    }

    public void addAttributeField(XmlAttributeField field) {
        _attributeFieldList.add(field);
    }

    public XmlAttributeField getAttributeField(String name)
            throws WdkModelException {
        XmlAttributeField field = _attributeFields.get(name);
        if (field == null)
            throw new WdkModelException("Attempting to access an attribute '"
                    + name + "' of XmlRecordClass " + getFullName()
                    + " but it has none.");
        return field;
    }

    public XmlAttributeField[] getAttributeFields() {
        XmlAttributeField[] fields = new XmlAttributeField[_attributeFields.size()];
        _attributeFields.values().toArray(fields);
        return fields;
    }

    public void addTableField(XmlTableField field) {
        _tableFieldList.add(field);
    }

    public XmlTableField getTableField(String name) throws WdkModelException {
        XmlTableField field = _tableFields.get(name);
        if (field == null)
            throw new WdkModelException("Table field not found: " + name);
        return field;
    }

    public XmlTableField[] getTableFields() {
        XmlTableField[] fields = new XmlTableField[_tableFields.size()];
        _tableFields.values().toArray(fields);
        return fields;
    }

    public XmlRecordClassSet getRecordClassSet() {
        return _recordClassSet;
    }

    /**
     * this method is called by XmlRecordClassSet in setting resource stage
     * 
     * @param recordClassSet
     */
    public void setRecordClassSet(XmlRecordClassSet recordClassSet) {
        _recordClassSet = recordClassSet;
    }

    @Override
    public void resolveReferences(WdkModel model) {
    // do nothing at this time
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("XmlRecordClass: name='");
        buf.append(_name);
        buf.append("'\r\n--- Attributes ---");
        for (XmlAttributeField attribute : _attributeFields.values()) {
            buf.append("\r\n");
            buf.append(attribute.getName());
        }
        buf.append("\r\n--- Tables ---");
        for (XmlTableField table : _tableFields.values()) {
            buf.append("\r\n");
            buf.append(table);
        }
        buf.append("\r\n");
        return buf.toString();
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude attribute field
        for (XmlAttributeField field : _attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (_attributeFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlAttributeField "
                            + fieldName + " is duplicated in xmlRecordClass "
                            + this.getFullName());
                _attributeFields.put(fieldName, field);
            }
        }
        _attributeFieldList = null;

        // exclude table fields
        for (XmlTableField field : _tableFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (_tableFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlTableField "
                            + fieldName + " is duplicated in xmlRecordClass "
                            + this.getFullName());
                _tableFields.put(fieldName, field);
            }
        }
        _tableFieldList = null;
    }
}
