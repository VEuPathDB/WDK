package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public class ParamHandlerReference extends WdkModelBase {

  private Param param;
  private String implementation;
  private List<WdkModelText> propertyList;
  private Map<String, String> properties;

  public ParamHandlerReference() {
    propertyList = new ArrayList<>();
    properties = new HashMap<String, String>();
  }

  public ParamHandlerReference(Param param, ParamHandlerReference reference) {
    super(reference);
    this.param = param;
    this.implementation = reference.implementation;
    this.propertyList = (reference.propertyList != null)
        ? new ArrayList<WdkModelText>(reference.propertyList) : null;
    this.properties = new HashMap<>(reference.properties);
  }

  public Param getParam() {
    return param;
  }

  public void setParam(Param param) {
    this.param = param;
  }

  /**
   * @return the implementation
   */
  public String getImplementation() {
    return implementation;
  }

  /**
   * @param implementation
   *          the implementation to set
   */
  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude properties
    for (WdkModelText text : propertyList) {
      if (text.include(projectId)) {
        text.excludeResources(projectId);
        if (properties.containsKey(text.getName()))
          throw new WdkModelException("The property [" + text.getName()
              + "] is duplicated in paramHandler " + implementation
              + " of param [" + param.getFullName() + "].");
        properties.put(text.getName(), text.getText());
      }
    }
    propertyList = null;
  }
}
