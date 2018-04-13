package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
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
 */
public class QueryColumnAttributeField extends ColumnAttributeField {

  private Column _column;

  private List<FilterReference> _filterReferences = new ArrayList<>();
  private Map<String, ColumnFilter> _columnFilters = new LinkedHashMap<>();

  @Override
  public ColumnAttributeField clone() {
    return (ColumnAttributeField) super.clone();
  }

  /**
   * @return Returns the column.
   */
  public Column getColumn() {
    return _column;
  }

  /**
   * @param column
   *          The column to set.
   */
  @RngUndefined
  public void setColumn(Column column) {
    _column = column;
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
    _filterReferences = list;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    // verify the name
    if (!_name.equals(_column.getName())) {
      throw new WdkModelException("The name of the ColumnAttributeField" + " '" + _name +
          "' does not match the column name '" + _column.getName() + "'");
    }

    // resolve the column filters
    for (FilterReference reference : _filterReferences) {
      reference.resolveReferences(wdkModel);
      String name = reference.getName();
      FilterDefinition definition = (FilterDefinition) wdkModel.resolveReference(name);
      if (definition instanceof ColumnFilterDefinition) {
        ColumnFilter filter = ((ColumnFilterDefinition) definition).getColumnFilter(this);
        if (_columnFilters.containsKey(filter.getKey()))
          throw new WdkModelException("Non-unique column filter key '" + filter.getKey() +
              "' in attribute " + getName() + " of " + _containerName);
        _columnFilters.put(filter.getKey(), filter);
      }
      else {
        throw new WdkModelException("The filter \"" + name + "\" is not a columnFilter on attribute " +
            getName() + " of " + _containerName);
      }
    }
    _filterReferences.clear();
  }

  public Collection<ColumnFilter> getColumnFilters() {
    return _columnFilters.values();
  }
}
