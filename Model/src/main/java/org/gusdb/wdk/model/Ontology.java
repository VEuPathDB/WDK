package org.gusdb.wdk.model;

import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;

public abstract class Ontology extends WdkModelBase {
  
  private String name;
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() { return name; }
  
  abstract TreeNode<Map<String, List<String>>> getTree();
  
  TreeNode<Map<String, List<String>>> getTree(Map<String,List<String>> filter) { 
    // TODO: apply filter
    return getTree();
  }
}
