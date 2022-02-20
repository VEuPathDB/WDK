package org.gusdb.wdk.model.columntool;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelText;

public class ImplementationRef {

  private String _implementationClass;
  private Map<String,String> _properties = new HashMap<>();

  public void setImplementation(String impl) {
    _implementationClass = impl;
  }

  public String getImplementation() {
    return _implementationClass;
  }

  public void addProperty(WdkModelText prop) {
    _properties.put(prop.getName(), prop.getText());
  }

  public Map<String,String> getProperties() {
    return _properties;
  }
}
