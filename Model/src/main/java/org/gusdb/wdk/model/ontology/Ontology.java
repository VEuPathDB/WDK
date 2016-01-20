package org.gusdb.wdk.model.ontology;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkUserException;

public class Ontology extends TreeNode<OntologyNode> {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(Ontology.class);
  
  private String _name;

  public Ontology(String name, TreeNode<OntologyNode> root) {
    super(root.getContents());
    _name = name;
    addAllChildNodes(root.getChildNodes());
  }

  public String getName() {
    return _name;
  }

  /**
   * This method finds nodes that meet the passed criteria (i.e. matches all
   * passed property values) and returns a list of paths to those nodes, where
   * a path is made up of the nodes in the tree from the root to the matching
   * node.
   * 
   * @param criteria map of properties nodes passing should contain
   * @return list of paths to matching nodes
   */
  public List<List<OntologyNode>> getAllPaths(Map<String,String> criteria) {
    List<List<OntologyNode>> paths = new ArrayList<>();
    Deque<OntologyNode> nodeStack = new ArrayDeque<>();
    addPaths(paths, nodeStack, new PropertyPredicate(criteria), this);
    return paths;
  }

  private static void addPaths(List<List<OntologyNode>> paths, Deque<OntologyNode> nodeStack,
      Predicate<OntologyNode> predicate, TreeNode<OntologyNode> node) {
    nodeStack.push(node.getContents());
    if (predicate.test(node.getContents())) {
      // need special method here to get list of nodes in bottom-up order
      paths.add(getPathAsList(nodeStack));
    }
    for (TreeNode<OntologyNode> child : node.getChildNodes()) {
      addPaths(paths, nodeStack, predicate, child);
    }
    nodeStack.pop();
  }

  private static List<OntologyNode> getPathAsList(Deque<OntologyNode> nodeStack) {
    List<OntologyNode> path = new ArrayList<>();
    Iterator<OntologyNode> iter = nodeStack.descendingIterator();
    while (iter.hasNext()) {
      path.add(iter.next());
    }
    return path;
  }

  /**********************************************************************************************/
  /******** EVERYTHING BELOW IS UNTESTED CODE THAT MAY OR MAY NOT WORK; USE WITH CAUTION ********/
  /**********************************************************************************************/


  /**
   * Get an ontology tree, filtering away individuals that do not match the key-value filter provided.
   * Also filter away categories that have no children.
   * (Nodes in a WDK ontology are typed as either "categories" or "individuals".  
   * The latter are things assigned into the ontology, such as WDK record attributes.)
   * 
   * @param individualFilterKey Individuals must have this property key to be kept
   * @param individualFilterValue Individuals must have this property value for the provided key to be kept
   * @param typeKey The key to use to find the type (individual or category) of a node.  If absent, throw a WdkUserException.
   * @param categoryTypeValue A value for that key to indicate that a node is a category node.
   * @return
   * @throws WdkUserException
   */
  public TreeNode<OntologyNode> getTree(final String individualFilterKey,
      final String individualFilterValue, final String typeKey, final String categoryTypeValue)
          throws WdkUserException {

    Predicate<TreeNode<OntologyNode>> predicate = new Predicate<TreeNode<OntologyNode>>() {
      @Override
      public boolean test(TreeNode<OntologyNode> node) {
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
   
    TreeNode<OntologyNode> tree = steveFilter(this, predicate, null, true);
    return tree;
  }

  /**
   * Return a copy of this TreeNode, with children pruned to include
   * only those that satisfy the predicates, recursively.
   * 
   * @param nodePred predicate to test nodes against
   * @param pred predicate to test node contents against
   * @param keepAllValidKids set to true if kids of a failed node should be propagated to its parent
   * @return null if this TreeNode fails the predicates, otherwise, a copy of this TreeNode, with children pruned to include only those that satisfy the predicates
   */
  private static <T> TreeNode<T> steveFilter(TreeNode<T> root, Predicate<TreeNode<T>> nodePred, Predicate<T> pred, boolean keepAllValidKids) {
    if ((nodePred == null || nodePred.test(root)) &&
        (pred == null || pred.test(root.getContents()))) {
     return filterSub(root, nodePred, pred, keepAllValidKids);
    } else return null;
  }

  private static <T> TreeNode<T> filterSub(TreeNode<T> node, Predicate<TreeNode<T>> nodePred, Predicate<T> pred, boolean keepAllValidKids) {
    // make a list of copies of my children, each updated with their filtered children
    List<TreeNode<T>> newChildren = new ArrayList<TreeNode<T>>();
    for (TreeNode<T> childNode : node.getChildNodes()) {
      newChildren.add(steveFilter(childNode, nodePred, pred, keepAllValidKids));
    }

    // make a copy of me
    TreeNode<T> newNode = new TreeNode<T>(node.getContents());

    // add to my copy the copies of my children that satisfy the filter
    for (TreeNode<T> newChild : newChildren) {
      if ((nodePred == null || nodePred.test(newChild)) &&
          (pred == null || pred.test(newChild.getContents()))) {
        newNode.addChildNode(newChild);
      } else if (keepAllValidKids) {
        for (TreeNode<T> grandKid : newChild.getChildNodes())
        newNode.addChildNode(grandKid);
      }
    }
    return newNode;
  }

  /*
  public class TreeNodeFilterResult {
    TreeNode<T> node;
    boolean isValid;
  }

  public TreeNodeFilterResult filter2(Predicate<TreeNode<?>> nodePred, Predicate<T> pred,
      boolean keepAllValidNodes) {

    TreeNodeFilterResult result = new TreeNodeFilterResult();
    result.node = new TreeNode<T>(_nodeContents);

    for (TreeNode<T> child : _childNodes) {
      TreeNodeFilterResult childResult = child.filter2(nodePred, pred, keepAllValidNodes);
      if (childResult.isValid)
        result.node.addChildNode(childResult.node);
      else if (keepAllValidNodes)
        for (TreeNode<T> grandKid : childResult.node.getChildNodes())
          result.node.addChildNode(grandKid);
    }

    result.isValid =  (nodePred == null || nodePred.test(result.node)) &&
        (pred == null || pred.test(result.node.getContents()));
    return result;
  }
  */
}
