package org.gusdb.wsf.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class PluginExecutor {

  private static final Logger LOG = Logger.getLogger(PluginExecutor.class);

  public int execute(String pluginClassName, PluginRequest request, PluginResponse response)
      throws PluginModelException, PluginUserException, DelayedResultException {
    LOG.info("Invoking: " + pluginClassName + ", projectId: " + request.getProjectId());
    LOG.debug("request: " + request.toString());

    // use reflection to load the plugin object
    LOG.debug("Loading object " + pluginClassName);

    LOG.info("Creating plugin " + pluginClassName);

    try {
      Class<? extends Plugin> pluginClass = Class.forName(pluginClassName).asSubclass(Plugin.class);
      Plugin plugin = pluginClass.getDeclaredConstructor().newInstance();

      // initialize plugin
      plugin.initialize(request);

      // invoke the plugin
      LOG.debug("Invoking Plugin " + pluginClassName);
      return invokePlugin(plugin, request, response);
    }
    catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
        IllegalArgumentException | InvocationTargetException |
        NoSuchMethodException | SecurityException ex) {
      throw new PluginModelException(ex);
    }
  }

  private int invokePlugin(Plugin plugin, PluginRequest request, PluginResponse response)
      throws PluginModelException, PluginUserException, DelayedResultException {
    // validate required parameters
    LOG.debug("validing required params...");
    validateRequiredParameters(plugin, request);

    // validate columns
    LOG.debug("validating columns...");
    validateColumns(plugin, request);

    // validate parameters
    LOG.debug("validating params...");
    plugin.validateParameters(request);

    // execute the main function, and obtain result
    LOG.debug("invoking plugin...");
    return plugin.invoke(request, response);
  }

  private void validateRequiredParameters(Plugin plugin, PluginRequest request) throws PluginUserException {
    String[] reqParams = plugin.getRequiredParameterNames();

    // validate parameters
    Map<String, String> params = request.getParams();
    for (String param : reqParams) {
      if (!params.containsKey(param)) {
        throw new PluginUserException("The required parameter is missing: " + param);
      }
    }
  }

  private void validateColumns(Plugin plugin, PluginRequest request) throws PluginUserException, PluginModelException {
    String[] expectedColumns = request.getOrderedColumns();
    String[] requiredColumns = plugin.getColumns(request);

    Set<String> colSet = new HashSet<String>();
    for (String col : expectedColumns) {
      colSet.add(col);
    }
    for (String col : requiredColumns) {
      if (!colSet.contains(col)) {
        throw new PluginUserException("The required column is missing: " + col);
      }
    }
  }

}
