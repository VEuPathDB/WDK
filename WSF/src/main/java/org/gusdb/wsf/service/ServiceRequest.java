package org.gusdb.wsf.service;

import org.gusdb.wsf.plugin.PluginRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 */
public class ServiceRequest extends PluginRequest {

  private static final String PLUGIN_KEY = "plugin";

  private String pluginClass;

  public ServiceRequest() {
    super();
  }

  public ServiceRequest(ServiceRequest request) {
    super(request);
    this.pluginClass = request.pluginClass;
  }

  public ServiceRequest(String jsonString) throws ServiceModelException {
    try {
      JSONObject jsRequest = new JSONObject(jsonString);
      parseJSON(jsRequest);
      this.pluginClass = jsRequest.getString(PLUGIN_KEY);
    }
    catch (JSONException ex) {
      throw new ServiceModelException(ex);
    }
  }

  /**
   * the full class name of the WSF plugin. The service will instantiate a
   * plugin instance from this class name, and invoke it.
   *
   * @return the pluginClass
   */
  public String getPluginClass() {
    return pluginClass;
  }

  /**
   * @param pluginClass
   *   the pluginClass to set
   */
  public void setPluginClass(String pluginClass) {
    this.pluginClass = pluginClass;
  }

  @Override
  protected JSONObject getJSON() throws JSONException {
    JSONObject jsRequest = super.getJSON();
    jsRequest.put(PLUGIN_KEY, pluginClass);
    return jsRequest;
  }
}
