package org.gusdb.wdk.model.ontology;

import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public interface OntologyFactoryPlugin {

  /**
   * Create the ontology tree.  This method reads the provided parameters, such as the name of an .owl file.  It then
   * creates the ontology, in the form of a tree of TreeNodes.  Each TreeNode has as its contents a Map&lt;String, List&lt;String>>.  In other words, 
   * a set of properties, where each one has as a value a set of strings.  
   * @param parameters
   * @param ontologyName
   * @param wdkModel
   * @return
   */
  public TreeNode<OntologyNode> getTree(Map<String, String> parameters, String ontologyName, WdkModel wdkModel) throws WdkModelException;

  /**
   * Validate the parameters that will be provided to the plugin.  This is called by the WDK when it creates its model.
   * @param parameters
   * @param ontologyName
   */
  public void validateParameters(Map<String, String> parameters, String ontologyName) throws WdkModelException;
}
