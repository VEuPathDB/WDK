package org.gusdb.wdk.model.ontology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




//import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;


public class OntologyFactoryImpl extends WdkModelBase implements OntologyFactory {

  private String name;
  private String pluginClassName;
  private List<WdkModelText> propertyList = new ArrayList<>();

  private OntologyFactoryPlugin plugin;
  private Map<String, String> properties = new LinkedHashMap<>();

  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }

  public void setPluginClass(String pluginClassName) {
    this.pluginClassName = pluginClassName;
  }

  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(this.properties);
  }

  @Override
  public Ontology getOntology() throws WdkModelException {
    TreeNode<OntologyNode> rawTree = plugin.getTree(properties, name);
    return new Ontology(name, rawTree);
  }

  /**
   * Get the ontology tree.  Throw a WdkUserException if the tree contains circular paths (TODO).
   * @return
   * @throws WdkUserException
   */
  @Override
  public Ontology getValidatedOntology() throws WdkModelException {
    Ontology rawOntology = getOntology();
    List<List<TreeNode<OntologyNode>>> circularPaths = rawOntology.findCircularPaths();
    if (!circularPaths.isEmpty()) {
      // TODO: print out circular paths
      throw new WdkModelException("Ontology " + rawOntology.getName() + " contains circular paths.");
    }
    return rawOntology;
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
      throw new WdkModelException("Failed instantiating OntologyFactoryPlugin for class " + pluginClassName, ex);
    }
    
    // validate that we have required properties
    plugin.validateParameters(properties, getName());
  }

  private OntologyFactoryPlugin getPlugin()
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        
    if (plugin == null) {
      Class<? extends OntologyFactoryPlugin> pluginClass = Class.forName(pluginClassName).asSubclass(
          OntologyFactoryPlugin.class);
      plugin = pluginClass.newInstance();
    }
    return plugin;
  }

}
