package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.TreeNode;

public class JavaOntology extends Ontology {
  
  private static final Logger logger = Logger.getLogger(Ontology.class);


  // values from XML
  private String implementationClassName;
  private List<WdkModelText> propertyList = new ArrayList<>();

  private JavaOntologyPlugin plugin;
  private Map<String, String> properties = new LinkedHashMap<>();

  public void setImplementation(String implementationClassName) {
    this.implementationClassName = implementationClassName;
  }
  
  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(this.properties);
  }

  @Override
  public TreeNode<Map<String, List<String>>> getTree() {
    return plugin.getTree(properties);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude properties
    for (WdkModelText property : propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String propName = property.getName();
        String propValue = property.getText();
        if (properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in ontology " + getName());
        properties.put(propName, propValue);
        logger.trace("reporter property: [" + propName + "]='" + propValue
            + "'");
      }
    }
    propertyList = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
   * .WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    try {
      plugin = getPlugin();
    } catch (Exception ex) {
      throw new WdkModelException("Failed instantiating JavaOntology plugin for class " + implementationClassName, ex);
    }
    
    // validate that we have required properties
    plugin.validateProperties(properties);
  }

  private JavaOntologyPlugin getPlugin()
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Class<? extends JavaOntologyPlugin> pluginClass = Class.forName(implementationClassName).asSubclass(
        JavaOntologyPlugin.class);
    JavaOntologyPlugin plugin = pluginClass.newInstance();

    return plugin;
  }

}
