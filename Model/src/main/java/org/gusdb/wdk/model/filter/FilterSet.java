package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class FilterSet extends WdkModelBase implements ModelSetI<FilterReference> {

  private List<FilterReference> filterReferencesList = new ArrayList<>();
  private Map<String, FilterReference> filterReferenceMap = null;
  
  private String name;

  public FilterSet(FilterSet base) {
    super(base);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public FilterReference getElement(String elementName) {
    return filterReferenceMap.get(elementName);
  }
  
  public void addFilter(FilterReference reference) {
    filterReferencesList.add(reference);
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    // do nothing
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    
    filterReferenceMap = new LinkedHashMap<>();
    for (FilterReference reference :filterReferencesList) {
      if (reference.include(projectId)) {
        reference.excludeResources(projectId);
        
        // check if the reference of the same project already exists
        if (filterReferenceMap.containsKey(reference.getName()))
          throw new WdkModelException("Filter reference " + reference.getName() + " is duplicated for project " + projectId);
        
        filterReferenceMap.put(reference.getName(), reference);
      }
    }
    filterReferencesList.clear();
    filterReferencesList = null;
  }
  
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    // TODO Auto-generated method stub
    super.resolveReferences(wdkModel);
  }
  
}
