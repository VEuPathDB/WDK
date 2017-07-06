package org.gusdb.wdk.model.record.attribute.plugin;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.prettyPrint;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.WdkModelException;
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

  public boolean hasBeenAssignedProperties() {
    return getProperties() != null;
  }

  /**
   * Expected incoming format is a JSON array of property objects, each of which has "name" and "value"
   * properties.
   * 
   * @param pluginProperties JSON-formatted property array
   * @throws WdkModelException if passed properties cannot be parsed
   */
  @RngOptional
  public void setPluginProperties(String pluginProperties) throws WdkModelException {
    String errorMessage = "Could not parse plugin properties. " +
        "JSON array or object required. Value starts on next line:" + NL + pluginProperties + NL;
    JsonType jsonType = JsonType.parse(pluginProperties);
    switch(jsonType.getType()) {
      case ARRAY:
        try {
          Map<String,String> propMap = new HashMap<>();
          JSONArray propArray = jsonType.getJSONArray();
          for (int i = 0; i < propArray.length(); i++) {
            JSONObject prop = propArray.getJSONObject(i);
            propMap.put(prop.getString("name"), prop.getString("value"));
          }
          setProperties(propMap);
        }
        catch (JSONException e) {
          LOG.error(errorMessage, e);
          throw e;
        }
        break;
      case OBJECT:
        setProperties(JsonUtil.parseProperties(jsonType.getJSONObject()));
        break;
      default:
        throw new WdkModelException(errorMessage);
    }
  }

  public boolean hasAllDynamicFields() {
    return (
        getName() != null &&
        getDisplay() != null &&
        getDescription() != null &&
        getImplementation() != null &&
        getView() != null
    );
  }

  public String getDynamicFieldsAsString() {
    Map<String,String> properties = getProperties();
    String propsPrint = properties == null ? null : prettyPrint(properties, Style.MULTI_LINE);
    return new StringBuilder("{").append(NL)
        .append("name:           ").append(getName()).append(NL)
        .append("display:        ").append(getDisplay()).append(NL)
        .append("description:    ").append(getDescription()).append(NL)
        .append("implementation: ").append(getImplementation()).append(NL)
        .append("view:           ").append(getView()).append(NL)
        .append("properties:     ").append(propsPrint).append(NL)
        .append("}").append(NL).toString();
  }
}
