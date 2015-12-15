package org.gusdb.wdk.model.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelException;

public class TestJavaOntologyPlugin implements JavaOntologyPlugin {

  @Override
  public TreeNode<Map<String, List<String>>> getTree(Map<String, String> parameters) {
    
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
   
    return null;
  }

  @Override
  public void validateParameters(Map<String, String> parameters) throws WdkModelException {
    // TODO Auto-generated method stub

  }
  
  private void addSingleValueProp(Map<String, List<String>> contents, String prop, String value) {
    ArrayList<String> list = new ArrayList<String>();
    list.add(value);
    contents.put(prop, list);
  }

}
