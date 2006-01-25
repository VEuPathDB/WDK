/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Jerric
 * @created Jan 18, 2006
 */
public class AttributeQueryReference extends Reference {

    private Map<String, AttributeField> attributeFields;

    /**
     * 
     */
    public AttributeQueryReference() {
        super();
        attributeFields = new LinkedHashMap<String, AttributeField>();
    }

    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public AttributeQueryReference(String twoPartName) throws WdkModelException {
        super(twoPartName);
        attributeFields = new LinkedHashMap<String, AttributeField>();
    }

    public void addAttributeField(AttributeField attributeField) {
        attributeFields.put(attributeField.getName(), attributeField);
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new LinkedHashMap<String, AttributeField>(attributeFields);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] array = new AttributeField[attributeFields.size()];
        attributeFields.values().toArray(array);
        return array;
    }
}
