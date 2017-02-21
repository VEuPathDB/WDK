/**
 * 
 */
package org.gusdb.wdk.model.record;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.RngAnnotations;
import org.gusdb.wdk.model.RngAnnotations.FieldSetter;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;


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

  private String _dynColumnsQueryRef;
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
  
  /**
   * Sets an optional reference to a dynamic columns query
   * @param dynamic columns query ref of the form "set.element"
   */
  public void setDynColumnsQueryRef(String dynColumnsQueryRef) throws WdkModelException {
	_dynColumnsQueryRef = dynColumnsQueryRef;
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
  
  /**
   * This resolver collects database defined column attributes that are obtained from a meta column
   * query table as specified by the _dynColumnsQueryRef.
   */
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
	// Continue only if a dynamic columns query reference is provided.  
	if(_dynColumnsQueryRef != null) { 
	  SqlQuery query = (SqlQuery) wdkModel.resolveReference(_dynColumnsQueryRef);
	  String sql = query.getSql();
	  ResultSet resultSet = null;
	  AttributeField attributeField = null;
	  List<FieldSetter> fieldSetters = RngAnnotations.getRngFields(QueryColumnAttributeField.class);
	  try {
	    resultSet = SqlUtils.executeQuery(wdkModel.getAppDb().getDataSource(), sql, query.getFullName() + "__dyn-cols");
	    ResultSetMetaData metaData = resultSet.getMetaData();
	    int columnCount = metaData.getColumnCount();
	    while(resultSet.next()) {  
		  
		  attributeField = new QueryColumnAttributeField();
		  attributeField.excludeResources(wdkModel.getProjectId());
		  
		  List<String> columnNames = new ArrayList<>();
		  for (int i = 1; i <= columnCount; i++ ) {
		    String columnName = metaData.getColumnName(i).toLowerCase();
		    columnNames.add(columnName);
		  }  
		  
		  for(FieldSetter fieldSetter : fieldSetters) {
        	    if(columnNames.contains(fieldSetter.underscoredName)) {
        	    	  Class<?> type = fieldSetter.setter.getParameterTypes()[0];
        	    	  switch(type.getName()) {
        	    		  case "java.lang.String":
        	    			String strFieldValue = resultSet.getString(fieldSetter.underscoredName);
        	    			if(strFieldValue == null && fieldSetter.isRngRequired) {
        	    			  throw new WdkModelException("The field "
             	    	             + fieldSetter.underscoredName
             	    	             + " is required for the QueryColumnAttributeField object");  
        	    			}
        	    			else if(strFieldValue != null) {
        	    			  fieldSetter.setter.invoke(attributeField, strFieldValue);
        	    			}
        	    			break;
        	    		  case "int":
        	    			Integer intFieldValue = resultSet.getInt(fieldSetter.underscoredName);
        	    			if(resultSet.wasNull() && fieldSetter.isRngRequired) {
        	    			  throw new WdkModelException("The field "
                	    	             + fieldSetter.underscoredName
                	    	             + " is required for the QueryColumnAttributeField object");  
        	    			}
        	    			else if(intFieldValue != null) {
        	    			  fieldSetter.setter.invoke(attributeField, intFieldValue);
        	    			}
        	    			break;
        	    		  case "boolean":
        	    			Boolean boolFieldValue = resultSet.getBoolean(fieldSetter.underscoredName);
          	    	    if(resultSet.wasNull() && fieldSetter.isRngRequired) {
          	    	      throw new WdkModelException("The field "
                  	    	         + fieldSetter.underscoredName
                  	    	         + " is required for the QueryColumnAttributeField object");  
          	    		}
          	    		else if(boolFieldValue != null) {
          	    		  fieldSetter.setter.invoke(attributeField, boolFieldValue);
          	    		}
        	    			break;
        	    		  default:
        	    		    throw new WdkModelException("The parameter type " + type.getName() + " is not yet handled.");
        	      }
        	    }
        	    else {
        	    	  if(fieldSetter.isRngRequired) {
        	    		throw new WdkModelException("The field "
        	    	               + fieldSetter.underscoredName
        	    	               + " is required for the QueryColumnAttributeField object");  
        	    	  }
        	    }
          }	
		  // Add the the attribute field map because the attribute field list may already have been trashed by an
		  // excludeResources method
		  this.attributeFieldMap.put(attributeField.getName(), attributeField);
	    }  
  	  }
	  catch(Exception e) {
	    throw new WdkModelException("Unable to resolve database loaded column attributes.", e);
	  }
	}  
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
