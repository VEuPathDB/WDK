package org.gusdb.wdk.model;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.gusdb.wdk.model.RngAnnotations.FieldSetter;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

/**
 * This class contains a method to populate the column and attribute field objects associated
 * with a database loaded attribute.
 * @author crisl-adm
 *
 */
public class AttributeMetaQueryHandler {

  /**
   * 	
   * @param obj - a column or queryColumnAttributeField object
   * @param resultSet - result set for an attributeMetaQuery
   * @param metaData - The meta data associated with the result set
   * @param columnNames - The names of columns in the resultSet - which may be different
   * between various attribute meta query references
   * @param fieldSetters - list of setter meta data for fields supported by column or
   * queryColumnAttributeField objects.  Includes such information as whether the field
   * is required, the setter method, the corresponding name of the field in the database table.
   * @throws WdkModelException
   */
  public static void populate(Object obj,
		  ResultSet resultSet,
		  ResultSetMetaData metaData,
		  List<String> columnNames,
		  List<FieldSetter> fieldSetters) throws WdkModelException {
	  
	try {
		
	  // This method only populates column and queryColumnAttributeField objects	
	  if(!(obj instanceof Column) && !(obj instanceof QueryColumnAttributeField)) {
	    throw new WdkModelException("The attribute meta query reference can only populate"
	    		+ " Column and QueryColumnAttributeField objects.");
	  }
		  
	  // Iterate over all the possible fields for the given object (Column or QueryColumnAttributeFiled)
	  for(FieldSetter fieldSetter : fieldSetters) {
		  
		// If the fieldSetter's database column name matches one available via the attribute meta query,
		// retrieve the value for the column and invoke its setter.  If the value is null but the 
		// fieldSetter data indicates that that field is required for the object, throw an exception.
		// Note that only string, boolean and ints are handled presently.  
	    if(columnNames.contains(fieldSetter.underscoredName)) {
          Class<?> type = fieldSetter.setter.getParameterTypes()[0];
       	  switch(type.getName()) {
       	    case "java.lang.String":
       	      String strFieldValue = resultSet.getString(fieldSetter.underscoredName);
       	    	  if(strFieldValue == null && fieldSetter.isRngRequired) {
       	        throw new WdkModelException("The field "
                	             + fieldSetter.underscoredName
                	             + " is required for the " + obj.getClass().getName() + " object");  
       	    	  }
       	    	  else if(strFieldValue != null) {
       	    	    fieldSetter.setter.invoke(obj, strFieldValue);
       	    	  }
       	    	  break;
       	    case "int":
       	      Integer intFieldValue = resultSet.getInt(fieldSetter.underscoredName);
       	      // Make a distinction between null and 0
       	    	  if(resultSet.wasNull() && fieldSetter.isRngRequired) {
       	    	    throw new WdkModelException("The field "
                               + fieldSetter.underscoredName
                 	           + " is required for the " + obj.getClass().getName() + " object");  
       	    	  }
       	    	  else if(intFieldValue != null) {
       	    	    fieldSetter.setter.invoke(obj, intFieldValue);
       	    	  }
       	    	  break;
       	    case "boolean":
       	      Boolean boolFieldValue = resultSet.getBoolean(fieldSetter.underscoredName);
       	      // Make a distinction between null and false (0)
         	  if(resultSet.wasNull() && fieldSetter.isRngRequired) {
         	    throw new WdkModelException("The field "
               	     	       + fieldSetter.underscoredName
               	     	       + " is required for the " + obj.getClass().getName() + " object");  
         	  }
         	  else if(boolFieldValue != null) {
         	    fieldSetter.setter.invoke(obj, boolFieldValue);
         	  }
       	    	  break;
       	    default:
       	      throw new WdkModelException("The parameter type " + type.getName() + " is not yet handled.");
       	  }
        }
        else {
        	  // If the fieldSetter indicates that the field is required but no corresponding column
        	  // is found in the attribute meta query, throw an exception.
       	  if(fieldSetter.isRngRequired) {
            throw new WdkModelException("The field "
       	                   + fieldSetter.underscoredName
       	   	               + " is required for the " + obj.getClass().getName() + " object");  
       	  }
        }
	  }
	}  
	catch(SQLException | InvocationTargetException | IllegalAccessException e) {
	  throw new WdkModelException("Unable to resolve database loaded column attributes.", e);
	}
  }	
}
