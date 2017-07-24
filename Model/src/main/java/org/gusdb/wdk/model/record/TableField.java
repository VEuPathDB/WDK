package org.gusdb.wdk.model.record;

import static org.gusdb.wdk.model.AttributeMetaQueryHandler.getDynamicallyDefinedAttributes;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.AttributeMetaQueryHandler;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.record.attribute.DerivedAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;

/**
 * A table field defines a table of data associated with a recordClass. It defines what attributes the table
 * will have. column, link, and text attributes are allowed in the table field.
 * 
 * A table field is linked to a table query that provides the values for the table. Please refer to the Query
 * class for how to define table queries.
 * 
 * @author jerric
 */
public class TableField extends Field implements AttributeFieldContainer {

  private String _queryTwoPartName;
  private String _attributeMetaQueryTwoPartName;
  private Query _unwrappedQuery;
  private Query _wrappedQuery;
  private List<AttributeField> _attributeFieldList = new ArrayList<AttributeField>();
  private Map<String, AttributeField> _attributeFieldMap = new LinkedHashMap<String, AttributeField>();

  private List<WdkModelText> _descriptions = new ArrayList<WdkModelText>();
  private String _description;
  private String _categoryName;

  private RecordClass _recordClass;

  public void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
    setContainerName(recordClass.getFullName());
  }

  public Query getUnwrappedQuery() {
    return _unwrappedQuery;
  }

  public Query getWrappedQuery() {
    return _wrappedQuery;
  }

  public void setQueryRef(String queryRef) {
    _queryTwoPartName = queryRef;
  }

  public String getQueryRef() {
    return _queryTwoPartName;
  }
  
  public void setAttributeMetaQueryRef(String attributeMetaQueryRef) {
    _attributeMetaQueryTwoPartName = attributeMetaQueryRef;
  }
  
  public String getAttributeMetaQueryRef() {
    return _attributeMetaQueryTwoPartName;
  }

  public void addAttributeField(AttributeField attributeField) {
    if (attributeField instanceof DerivedAttributeField) {
      ((DerivedAttributeField)attributeField).setContainer(this);
    }
    _attributeFieldList.add(attributeField);
  }

  @Override
  public AttributeField[] getAttributeFields() {
    return getAttributeFields(FieldScope.ALL);
  }

  public AttributeField[] getAttributeFields(FieldScope scope) {
    Map<String, AttributeField> fieldMap = getAttributeFieldMap(scope);
    AttributeField[] array = new AttributeField[fieldMap.size()];
    fieldMap.values().toArray(array);
    return array;
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  /**
   * @return the sortingAttributeMap
   */
  @Override
  public Map<String, Boolean> getSortingAttributeMap() {
    return null;
  }

  @Override
  public Map<String, AttributeField> getAttributeFieldMap() {
    return getAttributeFieldMap(FieldScope.ALL);
  }

  public String getDescription() {
    return (_description == null) ? "" : _description;
  }

  public Map<String, AttributeField> getAttributeFieldMap(FieldScope scope) {
    Map<String, AttributeField> map = new LinkedHashMap<String, AttributeField>();
    for (AttributeField field : _attributeFieldMap.values()) {
      if (scope.isFieldInScope(field)) {
        map.put(field.getName(), field);
      }
    }
    return map;
  }

  public AttributeField getAttributeField(String fieldName) {
    return _attributeFieldMap.get(fieldName);
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (_resolved)
      return;
    super.resolveReferences(wdkModel);

    // resolve Query
    _unwrappedQuery = (Query) wdkModel.resolveReference(_queryTwoPartName);

    // validate the table query
    _recordClass.validateBulkQuery(_unwrappedQuery);

    // prepare the query and add primary key params
    String[] paramNames = _recordClass.getPrimaryKeyDefinition().getColumnRefs();
    _wrappedQuery = RecordClass.prepareQuery(wdkModel, _unwrappedQuery, paramNames);

    Column[] columns = _wrappedQuery.getColumns();
    for (Column column : columns) {
      AttributeField field = _attributeFieldMap.get(column.getName());
      if (field != null && field instanceof QueryColumnAttributeField) {
        ((QueryColumnAttributeField) field).setColumn(column);
      } // else, it's okay to have unmatched columns
    }

    for (AttributeField field : _attributeFieldMap.values()) {
      field.setContainerName(_recordClass.getFullName() + "." + _name);
    }
    
    // Continue only if an table attribute meta query reference is provided.  
  	if(_attributeMetaQueryTwoPartName != null) {
  	  for (Map<String,Object> row : getDynamicallyDefinedAttributes(_attributeMetaQueryTwoPartName, wdkModel)) {
  	    AttributeField attributeField = new QueryColumnAttributeField();

  	    // Need to call this explicitly since this attribute field originates from the database
  	    attributeField.excludeResources(wdkModel.getProjectId());

  	    // Populate the attributeField with the attribute meta data
  	    AttributeMetaQueryHandler.populate(attributeField, row);
  	    
  	    // Add the new attributeField to the map
  	    _attributeFieldMap.put(attributeField.getName(), attributeField);
  	  }  

  		/**
  	  try {	
  	    SqlQuery query = (SqlQuery) wdkModel.resolveReference(_attributeMetaQueryTwoPartName);
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
                   metaData, columnNames, fieldSetters, wdkModel);
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
          _attributeFieldMap.put(attributeField.getName(), attributeField);
        }
  	  }  
  	  catch (WdkModelException e) {
  	    throw new SQLRunnerException("Error loading sql-based table attributes", e);
      } **/
  	}
    

    _resolved = true;
  }

  @Override
  public int getTruncateTo() {
    throw new UnsupportedOperationException("getTruncate does not apply to TableField");
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude descriptions
    boolean hasDescription = false;
    for (WdkModelText description : _descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException("The table field " + _name + " of recordClass " +
              _recordClass.getFullName() + " has more than one description for project " + projectId);
        }
        else {
          _description = description.getText();
          hasDescription = true;
        }
      }
    }
    _descriptions = null;

    // exclude attributes
    for (AttributeField field : _attributeFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();
        if (_attributeFieldMap.containsKey(fieldName))
          throw new WdkModelException("The attributeField " + fieldName + " is duplicated in table " + _name);
        _attributeFieldMap.put(fieldName, field);
      }
    }
    _attributeFieldList = null;
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    super.printDependencyContent(writer, indent);

    // print attribute fields
    writer.println(indent + "<attributes count=\"" + _attributeFieldMap.size() + "\">");
    String[] attributeNames = _attributeFieldMap.keySet().toArray(new String[0]);
    Arrays.sort(attributeNames);
    String indent1 = indent + WdkModel.INDENT;
    for (String attributeName : attributeNames) {
      _attributeFieldMap.get(attributeName).printDependency(writer, indent1);
    }

    // print table queries
    _wrappedQuery.printDependency(writer, indent);
  }

  /**
   * @return attribute category name
   */
  public String getAttributeCategory() {
    return _categoryName;
  }

  /**
   * @param categoryName
   *          attribute category name
   */
  public void setAttributeCategory(String categoryName) {
    _categoryName = categoryName;
  }

}
