package org.gusdb.wdk.model.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelException;

public class TestOntologyPlugin implements OntologyFactoryPlugin {

  @Override
  public TreeNode<OntologyNode> getTree(Map<String, String> parameters, String ontologyName) {
    
    OntologyNode rootContents = new OntologyNode();
    addSingleValueProp(rootContents, "type", "category");
    addSingleValueProp(rootContents, "name", "root");
    TreeNode<OntologyNode> root = new TreeNode<OntologyNode>(rootContents);
    
    OntologyNode kid1contents = new OntologyNode();
    addSingleValueProp(kid1contents, "type", "category");
    addSingleValueProp(kid1contents, "name", "kid1");
    TreeNode<OntologyNode> kid1 = new TreeNode<OntologyNode>(kid1contents);
    root.addChildNode(kid1);
   
    OntologyNode kid2contents = new OntologyNode();
    addSingleValueProp(kid2contents, "type", "category");
    addSingleValueProp(kid2contents, "name", "kid2");
    TreeNode<OntologyNode> kid2 = new TreeNode<OntologyNode>(kid2contents);
    root.addChildNode(kid2);
   
    OntologyNode leaf1contents = new OntologyNode();
    addSingleValueProp(leaf1contents, "type", "individual");
    addSingleValueProp(leaf1contents, "name", "leaf1");
    TreeNode<OntologyNode> leaf1 = new TreeNode<OntologyNode>(leaf1contents);
    kid1.addChildNode(leaf1);
   
    OntologyNode leaf2contents = new OntologyNode();
    addSingleValueProp(leaf2contents, "type", "individual");
    addSingleValueProp(leaf2contents, "name", "leaf2");
    TreeNode<OntologyNode> leaf2 = new TreeNode<OntologyNode>(leaf2contents);
    kid1.addChildNode(leaf2);
   
    OntologyNode leaf3contents = new OntologyNode();
    addSingleValueProp(leaf3contents, "type", "individual");
    addSingleValueProp(leaf3contents, "name", "leaf3");
    TreeNode<OntologyNode> leaf3 = new TreeNode<OntologyNode>(leaf3contents);
    kid2.addChildNode(leaf3);
   
    OntologyNode leaf4contents = new OntologyNode();
    addSingleValueProp(leaf4contents, "type", "individual");
    addSingleValueProp(leaf4contents, "name", "leaf4");
    TreeNode<OntologyNode> leaf4 = new TreeNode<OntologyNode>(leaf4contents);
    kid2.addChildNode(leaf4);
   
    return root;
  }

  @Override
  public void validateParameters(Map<String, String> parameters, String ontologyName) throws WdkModelException {
    // test plugin takes no parameters; any supplied can be ignored
  }
  
  private void addSingleValueProp(OntologyNode contents, String prop, String value) {
    List<String> list = new ArrayList<>();
    list.add(value);
    contents.put(prop, list);
  }

}
