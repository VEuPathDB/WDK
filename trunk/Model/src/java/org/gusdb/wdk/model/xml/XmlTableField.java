/**
 * 
 */
package org.gusdb.wdk.model.xml;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.Field;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric
 * @created Oct 11, 2005
 */
public class XmlTableField extends Field {

    private Map<String, XmlAttributeField> attributeFields;

    public XmlTableField() {
        super();
        internal = false;
        attributeFields = new HashMap<String, XmlAttributeField>();
    }

    public void addAttributeField(XmlAttributeField attributeField) {
        attributeFields.put(attributeField.getName(), attributeField);
    }

    public XmlAttributeField getAttributeField(String name) throws WdkModelException {
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
}
