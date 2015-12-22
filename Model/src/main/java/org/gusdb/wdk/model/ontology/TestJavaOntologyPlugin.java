package org.gusdb.wdk.model.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelException;

public class TestJavaOntologyPlugin implements JavaOntologyPlugin {

  @Override
  public TreeNode<Map<String, List<String>>> getTree(Map<String, String> parameters, String ontologyName) {
    
    Map<String, List<String>> rootContents = new HashMap<String, List<String>>();
    addSingleValueProp(rootContents, "type", "category");
    addSingleValueProp(rootContents, "name", "root");
    TreeNode<Map<String, List<String>>> root = new TreeNode<Map<String, List<String>>>(rootContents);
    
    Map<String, List<String>> kid1contents = new HashMap<String, List<String>>();
    addSingleValueProp(kid1contents, "type", "category");
    addSingleValueProp(kid1contents, "name", "kid1");
    TreeNode<Map<String, List<String>>> kid1 = new TreeNode<Map<String, List<String>>>(kid1contents);
    root.addChildNode(kid1);
   
    Map<String, List<String>> kid2contents = new HashMap<String, List<String>>();
    addSingleValueProp(kid2contents, "type", "category");
    addSingleValueProp(kid2contents, "name", "kid2");
    TreeNode<Map<String, List<String>>> kid2 = new TreeNode<Map<String, List<String>>>(kid2contents);
    root.addChildNode(kid2);
   
    Map<String, List<String>> leaf1contents = new HashMap<String, List<String>>();
    addSingleValueProp(leaf1contents, "type", "individual");
    addSingleValueProp(leaf1contents, "name", "leaf1");
    TreeNode<Map<String, List<String>>> leaf1 = new TreeNode<Map<String, List<String>>>(leaf1contents);
    kid1.addChildNode(leaf1);
   
    Map<String, List<String>> leaf2contents = new HashMap<String, List<String>>();
    addSingleValueProp(leaf2contents, "type", "individual");
    addSingleValueProp(leaf2contents, "name", "leaf2");
    TreeNode<Map<String, List<String>>> leaf2 = new TreeNode<Map<String, List<String>>>(leaf2contents);
    kid1.addChildNode(leaf2);
   
    Map<String, List<String>> leaf3contents = new HashMap<String, List<String>>();
    addSingleValueProp(leaf3contents, "type", "individual");
    addSingleValueProp(leaf3contents, "name", "leaf3");
    TreeNode<Map<String, List<String>>> leaf3 = new TreeNode<Map<String, List<String>>>(leaf3contents);
    kid2.addChildNode(leaf3);
   
    Map<String, List<String>> leaf4contents = new HashMap<String, List<String>>();
    addSingleValueProp(leaf4contents, "type", "individual");
    addSingleValueProp(leaf4contents, "name", "leaf4");
    TreeNode<Map<String, List<String>>> leaf4 = new TreeNode<Map<String, List<String>>>(leaf4contents);
    kid2.addChildNode(leaf4);
   
    return root;
  }

  @Override
  public void validateParameters(Map<String, String> parameters, String ontologyName) throws WdkModelException {
    // TODO Auto-generated method stub

  }
  
  private void addSingleValueProp(Map<String, List<String>> contents, String prop, String value) {
    ArrayList<String> list = new ArrayList<String>();
    list.add(value);
    contents.put(prop, list);
  }

}
