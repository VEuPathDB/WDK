package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.Query;

public class StepFilterDefinition extends FilterDefinition {

  private List<WdkModelText> propertyList = new ArrayList<>();
  private Map<String, String> properties = new LinkedHashMap<>();
  private Query summaryQuery = null;
  private static final Logger LOG = Logger.getLogger(StepFilterDefinition.class);

  private Class<? extends StepFilter> _class;

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    try {
      String className = getImplementation();
      if (className == null) throw new WdkModelException("null implementation for StepFilter '" + getName() + "'");
      LOG.debug("Checking filter '" + getName() + "' implementation class: " + className);
      _class = Class.forName(className).asSubclass(StepFilter.class);
      String summaryQueryRef = properties.get("summaryQueryRef");
      summaryQuery = summaryQueryRef != null ? (Query) wdkModel.resolveReference(summaryQueryRef) : null;
    }
    catch (ClassNotFoundException | ClassCastException ex) {
      throw new WdkModelException(ex);
    }
  }

  public StepFilter getStepFilter() throws WdkModelException {
    try {
      StepFilter filter = _class.newInstance();
      initializeFilter(filter);
      filter.setSummaryQuery(summaryQuery);
      return filter;
    }
    catch (InstantiationException | IllegalAccessException ex) {
      throw new WdkModelException(ex);
    }
  }
  
  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(this.properties);
  }
  
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    // exclude properties
    for (WdkModelText property : propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String propName = property.getName();
        String propValue = property.getText();
        if (properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in step filter " + getStepFilter().getKey());
        properties.put(propName, propValue);
      }
    }
    propertyList = null;
  }  
}
