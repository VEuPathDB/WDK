/**
 * 
 */
package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;

/**
 * <p>
 * an object representation of the @{code <recordClass>/<attributeQuery>}, it
 * provides a reference to an attribute {@link Query}, and
 * {@link AttributeField}s can be defined in this tag.
 * </p>
 * 
 * <p>
 * The attribute {@link Query} of a {@link RecordClass} must be a
 * {@link SqlQuery}, with no {@link Param}s. The attribute {@link Query} should
 * return all possible records a record type can have in the system, and each
 * record must be unique in the result.
 * </p>
 * 
 * <p>
 * The attribute {@link Query> must also have all the primary key
 * {@link Column}s defined, but defining a {@link ColumnAttributeField} for
 * those columns is optional.
 * </p>
 * 
 * <p>
 * At runtime, the attribute query will be used by WDK in two contexts: on
 * summary page & on record page. Please refer to the {@link Query} class for
 * how to define attribute queries.
 * </p>
 * 
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
          throw new WdkModelException("The attributeField " + fieldName
              + " is duplicated in queryRef " + this.getTwoPartName());
        attributeFieldMap.put(fieldName, field);
      }
    }
    attributeFieldList = null;
  }
}
