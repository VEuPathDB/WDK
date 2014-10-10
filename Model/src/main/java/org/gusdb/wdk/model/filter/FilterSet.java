package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class FilterSet extends WdkModelBase implements ModelSetI<AbstractFilterReference> {

  private List<AbstractFilterReference> _filterReferencesList = new ArrayList<>();
  private Map<String, AbstractFilterReference> _filterReferenceMap = null;

  private String _name;

  public FilterSet(FilterSet base) {
    super(base);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public AbstractFilterReference getElement(String elementName) {
    return _filterReferenceMap.get(elementName);
  }

  public void addFilter(FilterReference reference) {
    _filterReferencesList.add(reference);
  }

  public void addColumnFilter(ColumnFilterReference reference) {
    _filterReferencesList.add(reference);
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    // do nothing
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    _filterReferenceMap = new LinkedHashMap<>();
    for (AbstractFilterReference reference : _filterReferencesList) {
      if (reference.include(projectId)) {
        reference.excludeResources(projectId);

        // check if the reference of the same project already exists
        if (_filterReferenceMap.containsKey(reference.getName()))
          throw new WdkModelException("Filter reference " + reference.getName() +
              " is duplicated for project " + projectId);

        _filterReferenceMap.put(reference.getName(), reference);
      }
    }
    _filterReferencesList.clear();
    _filterReferencesList = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    for (AbstractFilterReference filterReference : _filterReferenceMap.values()) {
      filterReference.resolveReferences(wdkModel);
    }
  }

}
