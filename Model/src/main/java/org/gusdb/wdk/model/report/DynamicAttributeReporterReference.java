package org.gusdb.wdk.model.report;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.prettyPrint;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DynamicAttributeReporterReference extends AttributeReporterRef {
  private static final Logger LOG = Logger.getLogger(DynamicAttributeReporterReference.class);

  private String _attributeName;

  /**
   * This is a trick to get the field populator to send this class the attribute name (NOT the plugin name).
   * Note we call super.setName() in setPluginName() below to set the plugin name in the parent class.
   */
  @Override
  @RngOptional
  public void setName(String attributeName) {
    _attributeName = attributeName;
  }

  @RngOptional
  public void setReporterName(String reporterName) {
    super.setName(reporterName);
  }

  @RngOptional
  public void setReporterDisplay(String reporterDisplay) {
    setDisplayName(reporterDisplay);
  }

  @RngOptional
  public void setReporterDescription(String reporterDescriptionString) {
    WdkModelText reporterDescription = new WdkModelText();
    reporterDescription.setText(reporterDescriptionString);
    setDescription(reporterDescription);
  }

  @RngOptional
  public void setReporterImplementation(String reporterImplementation) {
    setImplementation(reporterImplementation);
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
    String attributeName = _attributeName == null ? "unknown" : _attributeName;
    String errorMessage = "Could not parse plugin properties for attribute '" + attributeName +
        "'. JSON array or object required. Value starts on next line:" + NL + pluginProperties + NL;
    JsonType jsonType = JsonType.parse(pluginProperties);
    switch(jsonType.getType()) {
      case ARRAY:
        try {
          JSONArray propArray = jsonType.getJSONArray();
          for (int i = 0; i < propArray.length(); i++) {
            JSONObject prop = propArray.getJSONObject(i);
            WdkModelText property = new WdkModelText();
            property.setName(prop.getString("name"));
            property.setText(prop.getString("value"));
            addProperty(property);
          }
        }
        catch (JSONException e) {
          LOG.error(errorMessage, e);
          throw e;
        }
        break;
      case OBJECT:
        // Uncomment to add support for JSON object form of properties
        //setProperties(JsonUtil.parseProperties(jsonType.getJSONObject()));
        //break;
      default:
        throw new WdkModelException(errorMessage);
    }
  }

  public boolean hasAllDynamicFields() {
    return (
        getName() != null &&
            getDisplayName() != null &&
            getDescription() != null &&
            getImplementation() != null
    );
  }

  public String getDynamicFieldsAsString() {
    Map<String,String> properties = getProperties();
    String propsPrint = properties == null ? null : prettyPrint(properties, FormatUtil.Style.MULTI_LINE);
    return new StringBuilder("{").append(NL)
        .append("name:           ").append(getName()).append(NL)
        .append("displayName:        ").append(getDisplayName()).append(NL)
        .append("description:    ").append(getDescription()).append(NL)
        .append("implementation: ").append(getImplementation()).append(NL)
        .append("properties:     ").append(propsPrint).append(NL)
        .append("}").append(NL).toString();
  }
}
