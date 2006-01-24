/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.HashMap;
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
        attributeFields = new HashMap<String, AttributeField>();
    }

    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public AttributeQueryReference(String twoPartName) throws WdkModelException {
        super(twoPartName);
        attributeFields = new HashMap<String, AttributeField>();
    }

    public void addAttributeField(AttributeField attributeField) {
        attributeFields.put(attributeField.getName(), attributeField);
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new HashMap<String, AttributeField>(attributeFields);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] array = new AttributeField[attributeFields.size()];
        attributeFields.values().toArray(array);
        return array;
    }
}
