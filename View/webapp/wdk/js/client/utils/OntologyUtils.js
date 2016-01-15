import {
  compactRootNodes,
  pruneDescendantNodes
} from './TreeUtils';

let nodeIsLeaf = node => node.children.length === 0;

/**
 * Get a sub-tree from an Ontology. The `leafPredicate` function
 * is used to find the leaves of the tree to return.
 *
 * @param {Ontology} ontology
 * @param {Function} leafPredicate
 */
export function getTree(ontology, leafPredicate) {
  let nodePredicate = node => !nodeIsLeaf(node) || leafPredicate(node);
  return compactRootNodes(
    pruneDescendantNodes(ontology.tree, nodePredicate));
}

