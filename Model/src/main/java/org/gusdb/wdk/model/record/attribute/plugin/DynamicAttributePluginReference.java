package org.gusdb.wdk.model.record.attribute.plugin;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.json.JSONArray;
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

  /**
   * Expected incoming format is a JSON array of property objects, each of which has "name" and "value"
   * properties.
   * 
   * @param pluginProperties JSON-formatted property array
   */
  @RngOptional
  public void setPluginProperties(String pluginProperties) {
    try {
      Map<String,String> propMap = new HashMap<>();
      JSONArray propArray = new JSONArray(pluginProperties);
      for (int i = 0; i < propArray.length(); i++) {
        JSONObject prop = propArray.getJSONObject(i);
        propMap.put(prop.getString("name"), prop.getString("value"));
      }
      setProperties(propMap);
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
