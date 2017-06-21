package org.gusdb.wdk.model.record.attribute.plugin;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.json.JSONObject;

public class DynamicAttributePluginReference extends AttributePluginReference {

  @RngOptional
  public void setPluginName(String pluginName) {
    setName(pluginName);
  }

  @RngOptional
  public void setPluginDisplay(String pluginDisplay) {
    setDisplay(pluginDisplay);
  }

  @RngOptional
  public void setPluginDescription(String pluginDescription) {
    setDescription(pluginDescription);
  }

  @RngOptional
  public void setPluginImplementation(String pluginImplementation) {
    setImplementation(pluginImplementation);
  }

  @RngOptional
  public void setPluginView(String pluginView) {
    setView(pluginView);
  }

  @RngOptional
  public void setPluginProperties(String pluginProperties) {
    setProperties(JsonUtil.parseProperties(new JSONObject(pluginProperties)));
  }

  public boolean hasAllDynamicFields() {
    return (
        getName() != null &&
        getDisplay() != null &&
        getDescription() != null &&
        getImplementation() != null &&
        getView() != null &&
        getProperties() != null
    );
  }

}
