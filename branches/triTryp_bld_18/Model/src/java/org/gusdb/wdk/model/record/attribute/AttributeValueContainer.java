/**
 * 
 */
package org.gusdb.wdk.model.record.attribute;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;

/**
 * <p>
 * This container will cache the {@link AttributeValue}s once they are retrieved
 * for future reuse.
 * </p>
 * 
 * <p>
 * It creates the actual {@link AttributeValue} objects, and when an
 * {@link AttributeField} has embedded other {@link AttributeField}s, the
 * corresponding {@link AttributeValue} will use the container to resolve the
 * values of those embedded {@link AttributeField}s.
 * </p>
 * 
 * @author xingao
 * 
 */
public abstract class AttributeValueContainer implements AttributeValueMap {

  private static final Logger logger = Logger.getLogger(AttributeValueContainer.class);

  protected abstract Map<String, AttributeField> getAttributeFieldMap();

  protected abstract void fillColumnAttributeValues(Query attributeQuery)
      throws WdkModelException;

  /**
   * The container cannot create primaryKeyValue since it is already been
   * created somewhere else; instead, it relies on the sub-class to find the
   * primaryKeyValue for the record instance from correct sources.
   * 
   * @return
   */
  protected abstract PrimaryKeyAttributeValue getPrimaryKey();

  private Map<String, AttributeValue> attributeValueCache = new LinkedHashMap<String, AttributeValue>();

  /**
   * Get existing attribute value from cache, or create one if the value doesn't
   * exist.
   * 
   * @see org.gusdb.wdk.model.record.attribute.AttributeValueMap#getAttributeValue(java.lang.String)
   */
  public AttributeValue getAttributeValue(String fieldName)
      throws WdkModelException {
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
        logger.trace("column: " + column.getName());
      }
      if (query instanceof SqlQuery)
        logger.debug("SQL: \n" + ((SqlQuery) query).getSql());

      fillColumnAttributeValues(query);
      if (!attributeValueCache.containsKey(fieldName))
        // something is wrong here, need further investigation.
        throw new WdkModelException("Field exists, but the value "
            + "doesn't, need investigation: " + field.getName());
      value = attributeValueCache.get(fieldName);
    } else {
      throw new WdkModelException("unsupported attribute field type for : "
          + fieldName);
    }
    return value;
  }

  /**
   * force to cache an attribute value.
   * 
   * @see org.gusdb.wdk.model.record.attribute.AttributeValueMap#addAttributeValue(org.gusdb.wdk.model.record.attribute.AttributeValue)
   */
  public void addAttributeValue(AttributeValue value) {
    attributeValueCache.put(value.getAttributeField().getName(), value);
  }
}
