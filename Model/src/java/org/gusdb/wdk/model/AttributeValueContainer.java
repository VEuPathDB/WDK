/**
 * 
 */
package org.gusdb.wdk.model;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.query.Query;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public abstract class AttributeValueContainer {

    protected abstract Map<String, AttributeField> getAttributeFieldMap();
    
    protected abstract void fillColumnAttributeValues(Query attributeQuery)
            throws WdkModelException, NoSuchAlgorithmException, JSONException,
            SQLException, WdkUserException;

    protected PrimaryKeyAttributeValue primaryKey;

    private Map<String, AttributeValue> attributeValueCache;

    AttributeValueContainer(
            PrimaryKeyAttributeValue primaryKey) {
        attributeValueCache = new LinkedHashMap<String, AttributeValue>();

        attributeValueCache.put(primaryKey.getAttributeField().getName(),
                primaryKey);
    }

    public AttributeValue getAttributeValue(String fieldName)
            throws WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException, WdkUserException {
        // get the field from the cache; primary key always exists in the cache
        Map<String, AttributeField> fields = getAttributeFieldMap();
        AttributeField field = fields.get(fieldName);
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
            value = primaryKey;
            attributeValueCache.put(fieldName, value);
        } else if (field instanceof ColumnAttributeField) {
            Query query = ((ColumnAttributeField) field).getColumn().getQuery();
            fillColumnAttributeValues(query);
            value = attributeValueCache.get(fieldName);
        } else {
            throw new WdkModelException(
                    "unsupported attribute field type for : " + fieldName);
        }
        return value;
    }

    protected void addColumnAttributeValue(ColumnAttributeValue value) {
        attributeValueCache.put(value.getAttributeField().getName(), value);
    }
}