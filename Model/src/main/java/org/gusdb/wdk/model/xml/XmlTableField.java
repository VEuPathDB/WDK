package org.gusdb.wdk.model.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.Field;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlTableField extends Field {

    private List<XmlAttributeField> _attributeFieldList = new ArrayList<XmlAttributeField>();
    private Map<String, XmlAttributeField> _attributeFields = new LinkedHashMap<String, XmlAttributeField>();

    public XmlTableField() {
        _internal = false;
    }

    public void addAttributeField(XmlAttributeField attributeField) {
        _attributeFieldList.add(attributeField);
    }

    public XmlAttributeField getAttributeField(String name)
            throws WdkModelException {
        XmlAttributeField attributeField = _attributeFields.get(name);
        if (attributeField == null)
            throw new WdkModelException("The AttributeField '" + name
                    + "' does not exist!");
        return attributeField;
    }

    public XmlAttributeField[] getAttributeFields() {
        XmlAttributeField[] fields = new XmlAttributeField[_attributeFields.size()];
        _attributeFields.values().toArray(fields);
        return fields;
    }

    public int size() {
        return _attributeFields.size();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName());
        sb.append(" : ");
        for (XmlAttributeField field : _attributeFields.values()) {
            sb.append(field.getName());
            sb.append(", ");
        }
        if (sb.toString().endsWith(", "))
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude attribute fields
        for (XmlAttributeField field : _attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (_attributeFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlAttributeField "
                            + fieldName + " is duplicated in xmlTable "
                            + _name);
                _attributeFields.put(fieldName, field);
            }
        }
        _attributeFieldList = null;
    }

    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // do nothing
    }
}
