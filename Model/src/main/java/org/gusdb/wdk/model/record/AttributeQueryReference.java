/**
 * 
 */
package org.gusdb.wdk.model.record;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.AttributeMetaQueryHandler;
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

  private String _attributeMetaQueryRef;
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
  public void setAttributeMetaQueryRef(String attributeMetaQueryRef) throws WdkModelException {
	_attributeMetaQueryRef = attributeMetaQueryRef;
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
   * query table as specified by the _attributeMetaQueryRef.
   */
  @Override
  public void resolveReferences(final WdkModel wdkModel) throws WdkModelException {
    // Continue only if an attribute meta query reference is provided.  
    if(_attributeMetaQueryRef != null) {
      try {
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(_attributeMetaQueryRef);
        final List<AttributeField> attributeFields = new ArrayList<>();
        new SQLRunner(wdkModel.getAppDb().getDataSource(), query.getSql(), query.getFullName() + "__dyn-cols")
          .executeQuery(new ResultSetHandler() {
            @Override
            public void handleResult(ResultSet resultSet) throws SQLException {
              try {
                // Call the attribute meta query
                ResultSetMetaData metaData = resultSet.getMetaData();
  
                // Compile a list of database column names - the list will likely be different for
                // every attribute meta query table.
                int columnCount = metaData.getColumnCount();
                List<String> columnNames = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++ ) {
                  String columnName = metaData.getColumnName(i).toLowerCase();
                  columnNames.add(columnName);	
                }
  
                // get field setters to populate
                List<FieldSetter> fieldSetters = RngAnnotations.getRngFields(QueryColumnAttributeField.class);
  
                // Iterate over each row (database loaded attribute)
                while(resultSet.next()) {
                  AttributeField attributeField = new QueryColumnAttributeField();
  
                  // Need to call this here since this attribute field originates from the database
                  attributeField.excludeResources(wdkModel.getProjectId());
  
                  // Populate the attributeField with the attribute meta data
                  AttributeMetaQueryHandler.populate(attributeField, resultSet,
                      metaData, columnNames, fieldSetters);
  
                  attributeFields.add(attributeField);
                }
              }
              catch (WdkModelException e) {
                throw new SQLRunnerException("Error loading dynamic attributes", e);
              }
            }
          });

        // Add the the attribute field map because the attribute field list may already have been trashed by an
        // excludeResources method
        for (AttributeField attributeField : attributeFields) {
          this.attributeFieldMap.put(attributeField.getName(), attributeField);
        }
      }
      catch (SQLRunnerException se) {
        throw new WdkModelException("Unable to resolve database loaded attributes.", se.getCause());
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
