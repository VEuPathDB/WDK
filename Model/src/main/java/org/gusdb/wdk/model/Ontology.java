package org.gusdb.wdk.model;

import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;

public abstract class Ontology extends WdkModelBase {
  
  private String name;
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() { return name; }
  
  public abstract TreeNode<Map<String, List<String>>> getTree() throws WdkUserException ;
  
  /**
   * Get the ontology tree.  Throw a WdkUserException if the tree contains circular paths.
   * @return
   * @throws WdkUserException
   */
  public TreeNode<Map<String, List<String>>> getValidatedTree() throws WdkUserException {
    TreeNode<Map<String, List<String>>> tree = getTree();
    List<List<TreeNode<Map<String, List<String>>>>> circularPaths = tree.findCircularPaths();
    if (!circularPaths.isEmpty()) {
      // TODO: print out circular paths
      throw new WdkUserException("Ontology " + getName() + " contains circular paths");
    }
    return tree;
  }
  
  /**
   * Get an ontology tree, filtering away individuals that do not match the key-value filter provided.
   * Also filter away categories that have no children.
   * (Nodes in a WDK ontology are typed as either "categories" or "individuals".  
   * The latter are things assigned into the ontology, such as WDK record attributes.)
   * @param individualFilterKey Individuals must have this property key to be kept
   * @param individualFilterValue Individuals must have this property value for the provided key to be kept
   * @param typeKey The key to use to find the type (individual or category) of a node.  If absent, throw a WdkUserException.
   * @param categoryTypeValue A value for that key to indicate that a node is a category node.
   * @return
   * @throws WdkUserException
   */
  public TreeNode<Map<String, List<String>>> getTree(final String individualFilterKey,
      final String individualFilterValue, final String typeKey, final String categoryTypeValue)
          throws WdkUserException {

    Predicate<TreeNode<Map<String, List<String>>>> predicate = new Predicate<TreeNode<Map<String, List<String>>>>() {
      @Override
      public boolean test(TreeNode<Map<String, List<String>>> node) {
        Map<String, List<String>> contents = node.getContents();
        if (!contents.containsKey(typeKey))
          throw new RuntimeException("Node with the following contents is missing the required property '" +
              typeKey + "' " + contents);
        List<String> typeValues = contents.get(typeKey);
        if (typeValues.size() != 1)
          throw new RuntimeException(
              "Node with the following contents has size != 1 for the required property '" + typeKey + "' " +
                  contents);
        String type = typeValues.get(0);
        if (type.equals(categoryTypeValue)) {
          return !node.isLeaf(); // ie, no children
        }
        else {
          if (!contents.containsKey(individualFilterKey))
            return false;
          List<String> filterValues = contents.get(individualFilterKey);
          for (String value : filterValues)
            if (value.equals(individualFilterValue))
              return true;
          return false;
        }
      }
    };

    try {
      TreeNode<Map<String, List<String>>> tree = getTree().filter(predicate, null, true);
      return tree;
    } catch (Exception ex) {
      throw new WdkUserException(ex);
    }
  }
}
