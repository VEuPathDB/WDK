package org.gusdb.wdk.model.record.attribute.plugin;

import static org.gusdb.fgputil.FormatUtil.NL;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.json.JSONException;
import org.json.JSONObject;

public class DynamicAttributePluginReference extends AttributePluginReference {

  private static final Logger LOG = Logger.getLogger(DynamicAttributePluginReference.class);

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
    try {
      setProperties(JsonUtil.parseProperties(new JSONObject(pluginProperties)));
    }
    catch (JSONException e) {
      LOG.error("Could not parse plugin properties.  Value starts on next line:" + NL + pluginProperties + NL, e);
      throw e;
    }
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
