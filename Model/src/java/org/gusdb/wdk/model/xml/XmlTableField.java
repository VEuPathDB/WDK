/**
 * 
 */
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

    private List<XmlAttributeField> attributeFieldList = new ArrayList<XmlAttributeField>();
    private Map<String, XmlAttributeField> attributeFields = new LinkedHashMap<String, XmlAttributeField>();

    public XmlTableField() {
        internal = false;
    }

    public void addAttributeField(XmlAttributeField attributeField) {
        attributeFieldList.add(attributeField);
    }

    public XmlAttributeField getAttributeField(String name)
            throws WdkModelException {
        XmlAttributeField attributeField = attributeFields.get(name);
        if (attributeField == null)
            throw new WdkModelException("The AttributeField '" + name
                    + "' does not exist!");
        return attributeField;
    }

    public XmlAttributeField[] getAttributeFields() {
        XmlAttributeField[] fields = new XmlAttributeField[attributeFields.size()];
        attributeFields.values().toArray(fields);
        return fields;
    }

    public int size() {
        return attributeFields.size();
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
        for (XmlAttributeField field : attributeFields.values()) {
            sb.append(field.getName());
            sb.append(", ");
        }
        if (sb.toString().endsWith(", "))
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude attribute fields
        for (XmlAttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFields.containsKey(fieldName))
                    throw new WdkModelException("The xmlAttributeField "
                            + fieldName + " is duplicated in xmlTable "
                            + this.name);
                attributeFields.put(fieldName, field);
            }
        }
        attributeFieldList = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Field#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // do nothing
    }
}
