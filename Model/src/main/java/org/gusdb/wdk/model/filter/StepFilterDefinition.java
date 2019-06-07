package org.gusdb.wdk.model.filter;

import java.lang.reflect.InvocationTargetException;
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

  private static final Logger LOG = Logger.getLogger(StepFilterDefinition.class);

  private List<WdkModelText> _propertyList = new ArrayList<>();
  private Map<String, String> _properties = new LinkedHashMap<>();

  private Class<? extends StepFilter> _class;
  private Query _summaryQuery = null;

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    try {
      String className = getImplementation();
      if (className == null) {
        throw new WdkModelException("null implementation for StepFilter '" + getName() + "'");
      }
      LOG.debug("Checking filter '" + getName() + "' implementation class: " + className);
      _class = Class.forName(className).asSubclass(StepFilter.class);
      String summaryQueryRef = _properties.get("summaryQueryRef");
      _summaryQuery = summaryQueryRef != null ? (Query) wdkModel.resolveReference(summaryQueryRef) : null;
    }
    catch (ClassNotFoundException | ClassCastException ex) {
      throw new WdkModelException(ex);
    }
  }

  public StepFilter getStepFilter() throws WdkModelException {
    try {
      StepFilter filter = _class.getDeclaredConstructor().newInstance();
      initializeFilter(filter);
      filter.setSummaryQuery(_summaryQuery);
      return filter;
    }
    catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
        InvocationTargetException | NoSuchMethodException | SecurityException ex) {
      throw new WdkModelException(ex);
    }
  }

  public void addProperty(WdkModelText property) {
    _propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(_properties);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);
    // exclude properties
    for (WdkModelText property : _propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String propName = property.getName();
        String propValue = property.getText();
        if (_properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in step filter " + getStepFilter().getKey());
        _properties.put(propName, propValue);
      }
    }
    _propertyList = null;
  }
}
