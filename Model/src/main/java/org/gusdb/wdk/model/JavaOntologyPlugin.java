package org.gusdb.wdk.model;

import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;

public interface JavaOntologyPlugin {
  public TreeNode<Map<String, List<String>>> getTree(Map<String, String> properties);
  public void validateProperties(Map<String, String> properties);
}
