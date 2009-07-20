/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jerric
 * @created Jan 18, 2006
 */
public class AttributeQueryReference extends Reference {

    private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
    private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();

    /**
     * 
     */
    public AttributeQueryReference() {}

    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public AttributeQueryReference(String twoPartName) throws WdkModelException {
        super(twoPartName);
    }

    public void addAttributeField(AttributeField attributeField) {
        attributeFieldList.add(attributeField);
    }

    public Map<String, AttributeField> getAttributeFieldMap() {
        return new LinkedHashMap<String, AttributeField>(attributeFieldMap);
    }

    public AttributeField[] getAttributeFields() {
        AttributeField[] array = new AttributeField[attributeFieldMap.size()];
        attributeFieldMap.values().toArray(array);
        return array;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Reference#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        super.excludeResources(projectId);

        // exclude attribute fields
        for (AttributeField field : attributeFieldList) {
            if (field.include(projectId)) {
                field.excludeResources(projectId);
                String fieldName = field.getName();
                if (attributeFieldMap.containsKey(fieldName))
                    throw new WdkModelException("The attributeField "
                            + fieldName + " is duplicated in queryRef "
                            + this.getTwoPartName());
                attributeFieldMap.put(fieldName, field);
            }
        }
        attributeFieldList = null;
    }
}
