package org.gusdb.wsf.client;

import org.gusdb.wsf.plugin.PluginRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 */
@SuppressWarnings("hiding")
public class ClientRequest extends PluginRequest {

  public static final String PLUGIN_KEY = "plugin";
  public static final String PROJECT_KEY = PluginRequest.PROJECT_KEY;
  public static final String COLUMNS_ARRAY_KEY = PluginRequest.COLUMNS_ARRAY_KEY;
  public static final String PARAMETER_MAP_KEY = PluginRequest.PARAMETER_MAP_KEY;
  public static final String CONTEXT_MAP_KEY = PluginRequest.CONTEXT_MAP_KEY;

  private String pluginClass;

  public ClientRequest() {
    super();
  }

  public ClientRequest(ClientRequest request) {
    super(request);
    this.pluginClass = request.pluginClass;
  }

  public ClientRequest(String jsonString) throws ClientModelException {
    try {
      JSONObject jsRequest = new JSONObject(jsonString);
      parseJSON(jsRequest);
      this.pluginClass = jsRequest.getString(PLUGIN_KEY);
    }
    catch (JSONException ex) {
      throw new ClientModelException(ex);
    }
  }

  /**
   * the full class name of the WSF plugin. The service will instantiate a plugin instance from this class
   * name, and invoke it.
   *
   * @return the pluginClass
   */
  public String getPluginClass() {
    return pluginClass;
  }

  /**
   * @param pluginClass
   *          the pluginClass to set
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
