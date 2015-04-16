package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.ModelSetI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public class FilterSet extends WdkModelBase implements ModelSetI<FilterDefinition> {

  public static final String WDK_FILTER_SET = "WdkFilters";

  public static FilterSet getWdkFilterSet() {
    FilterSet filterSet = new FilterSet();
    filterSet.setName(WDK_FILTER_SET);
    
    // also create the default filters provided by WDK
    filterSet.addStepFilter(StrategyFilter.getDefinition());
    filterSet.addColumnFilter(ListColumnFilter.getDefinition());
    
    return filterSet;
  }

  private List<FilterDefinition> _filterDefinitionList = new ArrayList<>();
  private Map<String, FilterDefinition> _filterDefinitionMap = null;

  private String _name;

  public FilterSet() {}

  public FilterSet(FilterSet base) {
    super(base);
  }

  @Override
  public String getName() {
    return _name;
  }
  
  public void setName(String name) {
    _name = name;
  }

  @Override
  public FilterDefinition getElement(String elementName) {
    return _filterDefinitionMap.get(elementName);
  }

  public void addStepFilter(StepFilterDefinition definition) {
    _filterDefinitionList.add(definition);
  }

  public void addColumnFilter(ColumnFilterDefinition definition) {
    _filterDefinitionList.add(definition);
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    // do nothing
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    _filterDefinitionMap = new LinkedHashMap<>();
    for (FilterDefinition definition : _filterDefinitionList) {
      if (definition.include(projectId)) {
        definition.excludeResources(projectId);

        // check if the reference of the same project already exists
        if (_filterDefinitionMap.containsKey(definition.getName()))
          throw new WdkModelException("Filter reference " + definition.getName() +
              " is duplicated for project " + projectId);

        _filterDefinitionMap.put(definition.getName(), definition);
      }
    }
    _filterDefinitionList.clear();
    _filterDefinitionList = null;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);

    for (FilterDefinition definition : _filterDefinitionMap.values()) {
      definition.resolveReferences(wdkModel);
    }
  }

}
