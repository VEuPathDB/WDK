package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.filter.ColumnFilter;
import org.gusdb.wdk.model.filter.ColumnFilterDefinition;
import org.gusdb.wdk.model.filter.FilterDefinition;
import org.gusdb.wdk.model.filter.FilterReference;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;

/**
 * This is an {@link AttributeField} that maps an underlying {@link Column} from an attribute or table
 * {@link Query}.
 * 
 * @author jerric
 * 
 */
public class ColumnAttributeField extends AttributeField {

  private Column _column;

  private final List<FilterReference> _filterReferences = new ArrayList<>();
  private final Map<String, ColumnFilter> _columnFilters = new LinkedHashMap<>();

  public ColumnAttributeField() {
    super();
  }

  /**
   * @return Returns the column.
   */
  public Column getColumn() {
    return this._column;
  }

  /**
   * @param column
   *          The column to set.
   */
  public void setColumn(Column column) {
    this._column = column;
  }

  public void addFilterReference(FilterReference reference) {
    _filterReferences.add(reference);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    List<FilterReference> list = new ArrayList<>();
    for (FilterReference reference : _filterReferences) {
      if (reference.include(projectId)) {
        reference.excludeResources(projectId);
        list.add(reference);
      }
    }
    _filterReferences.clear();
    _filterReferences.addAll(list);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.Field#presolveReferences(org.gusdb.wdk.model.WdkModel )
   */
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // verify the name
    if (!name.equals(_column.getName()))
      throw new WdkModelException("The name of the ColumnAttributeField" + " '" + name +
          "' does not match the column name '" + _column.getName() + "'");

    // resolve the column filters
    for (FilterReference reference : _filterReferences) {
      String name = reference.getName();
      FilterDefinition definition = (FilterDefinition) wdkModel.resolveReference(name);
      if (definition instanceof ColumnFilterDefinition) {
        ColumnFilter filter = ((ColumnFilterDefinition) definition).getColumnFilter(this);
        if (_columnFilters.containsKey(filter.getKey()))
          throw new WdkModelException("Same filter \"" + name + "\" is referenced in attribute " + getName() +
              " of recordClass " + recordClass.getFullName() + " twice.");
        _columnFilters.put(filter.getKey(), filter);
      }
      else {
        throw new WdkModelException("The filter \"" + name + "\" is not a columnFilter on attribute " +
            getName() + " of recordClass " + recordClass.getFullName());
      }
    }
    _filterReferences.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributeField#getDependents()
   */
  @Override
  protected Collection<AttributeField> getDependents() {
    return new ArrayList<AttributeField>();
  }

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() {
    Map<String, ColumnAttributeField> fields = new LinkedHashMap<String, ColumnAttributeField>();
    fields.put(name, this);
    return fields;
  }

  public Collection<ColumnFilter> getColumnFilters() {
    return _columnFilters.values();
  }
  
  public void addColumnFilter(ColumnFilter filter) {
    _columnFilters.put(filter.getKey(), filter);
  }
}
