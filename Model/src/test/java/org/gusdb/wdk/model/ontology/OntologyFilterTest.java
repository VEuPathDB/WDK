package org.gusdb.wdk.model.ontology;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.junit.Test;

/**
 * Contains an implementation of an ontology filtering method that not only
 * prune leaves that match a predicate but also "flatten" branches with only one
 * child and remove branches that are left with no individuals after pruning.
 * 
 * This is to support our new ontology framework for object categorization.
 * 
 * @author rdoherty
 */
public class OntologyFilterTest {

  /**
   * Flattens categories in the passed ontology tree nodes that meet some
   * criteria.  If a category node passes the predicate, then it will be
   * removed, and its children will be inherited by its parent.  Thus, some
   * nodes returned may be in the original list or may be children of nodes
   * in the original list.
   * 
   * @param roots roots of trees to be operated on
   * @param predicate test for whether to remove category
   * @return a list of flattened nodes
   */
  public static List<TreeNode<OntologyNode>> flattenCategories(
      List<TreeNode<OntologyNode>> roots, final Predicate<OntologyNode> predicate) {

    // create a dummy parent to contain the roots
    final OntologyNode dummyNode = new OntologyNode();
    TreeNode<OntologyNode> masterRoot = new TreeNode<>(dummyNode);
    masterRoot.addAllChildNodes(roots);

    // create a custom predicate to test categories against; if node passes, it will be removed
    final Predicate<OntologyNode> customPred = new Predicate<OntologyNode>() {
      @Override
      public boolean test(OntologyNode obj) {
        // don't remove master node
        if (obj == dummyNode) return false;
        // only remove categories
        // FIXME: previously this code assumed a node would know if it was a category vs individual; this is broken without that
        //if (!obj.isCategory()) return false;
        // use the passed predicate
        return predicate.test(obj);
      }
    };

    // use a structure mapper to flatten the tree; removed nodes' children will be added to their respective parents
    return masterRoot.mapStructure(new StructureMapper<OntologyNode, TreeNode<OntologyNode>>() {
      @Override
      public TreeNode<OntologyNode> map(OntologyNode obj, List<TreeNode<OntologyNode>> mappedChildren) {
        // need to test each child to see if it should be removed, then inherit its children if it should
        TreeNode<OntologyNode> replacement = new TreeNode<>(obj);
        for (TreeNode<OntologyNode> child : mappedChildren) {
          if (customPred.test(child.getContents())) {
            // child will be removed; inherit child node's children
            for (TreeNode<OntologyNode> grandchild : child.getChildNodes()) {
              replacement.addChildNode(grandchild);
            }
          }
          else {
            // child should not be removed; add to replacement's children
            replacement.addChildNode(child);
          }
        }
        return replacement;
      }
    }).getChildNodes();
  }

  @Test
  public void testOntologyUsage() {
    // dummy values
    final String KEY = "property name";
    final String VALUE = "a value that should be kept";

    // dummy objects
    OntologyNode rootContent = new OntologyNode();
    rootContent.put(KEY, new ListBuilder<String>(VALUE).toList());
    TreeNode<OntologyNode> masterOntology = new TreeNode<>(rootContent);

    // What we want is to define a predicate on individuals, trim the tree based
    // on the predicate, then "flatten" the tree so that any categories that no
    // longer have individuals are removed (recursively), and any categories
    // that only have one individual are removed, passing their individual to
    // their parent (also recursively).

    final Predicate<OntologyNode> TEST_PREDICATE = new Predicate<OntologyNode>() {
      @Override
      public boolean test(OntologyNode obj) {
        List<String> values = obj.get(KEY);
        return values != null && values.contains(VALUE);
      }
    };

    // will remove leaf (non-category) nodes that don't pass the predicate's test
    TreeNode<OntologyNode> filteredOntology = Ontology.getFilteredOntology(masterOntology, TEST_PREDICATE, true);

     assertNotNull(filteredOntology);
  }
}
