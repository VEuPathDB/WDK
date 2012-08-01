/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;

/**
 * @author xingao
 * 
 */
public abstract class AttributeValueContainer implements AttributeValueMap {
	
    private static final Logger logger = Logger.getLogger(AttributeValueContainer.class);

    protected abstract Map<String, AttributeField> getAttributeFieldMap();

    protected abstract void fillColumnAttributeValues(Query attributeQuery)
            throws WdkModelException, WdkUserException;

    protected abstract PrimaryKeyAttributeValue getPrimaryKey();

    private Map<String, AttributeValue> attributeValueCache = new LinkedHashMap<String, AttributeValue>();

    public AttributeValue getAttributeValue(String fieldName)
            throws WdkModelException, WdkUserException {
        // get the field from the cache; primary key always exists in the cache
        Map<String, AttributeField> fields = getAttributeFieldMap();
        AttributeField field = fields.get(fieldName);
        if (field == null)
            throw new WdkModelException("The attribute field [" + fieldName
                    + "] cannot be found");

        if (attributeValueCache.containsKey(fieldName))
            return attributeValueCache.get(fieldName);

        // create the cache
        AttributeValue value;
        if (field instanceof LinkAttributeField) {
            value = new LinkAttributeValue((LinkAttributeField) field, this);
            attributeValueCache.put(fieldName, value);
        } else if (field instanceof TextAttributeField) {
            value = new TextAttributeValue((TextAttributeField) field, this);
            attributeValueCache.put(fieldName, value);
        } else if (field instanceof PrimaryKeyAttributeField) {
            value = getPrimaryKey();
            attributeValueCache.put(fieldName, value);
        } else if (field instanceof ColumnAttributeField) {
            Query query = ((ColumnAttributeField) field).getColumn().getQuery();

            logger.debug("filling attribute values from query " + query.getFullName());
            for (Column column : query.getColumns()) {
                logger.debug("column: " + column.getName());
            }
            if (query instanceof SqlQuery)
                logger.debug("SQL: \n" + ((SqlQuery)query).getSql());

            fillColumnAttributeValues(query);
            if (!attributeValueCache.containsKey(fieldName))
            // something is wrong here, need further investigation.
                throw new WdkModelException("Field exists, but the value "
                        + "doesn't, need investigation: " + field.getName());
            value = attributeValueCache.get(fieldName);
        } else {
            throw new WdkModelException(
                    "unsupported attribute field type for : " + fieldName);
        }
        return value;
    }

    public void addAttributeValue(AttributeValue value) {
        attributeValueCache.put(value.getAttributeField().getName(), value);
    }
}
