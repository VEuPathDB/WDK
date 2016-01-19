let pushInto = (array, ...values) =>
  (array.push(...values), array);

/**
 * Given a node `root`, returns a new node such that all leaves pass
 * `leafPredicate`. If no descendant of `root` passes, and if `root` does not
 * pass, then `undefined` will be returned.
 *
 * @param {Object} root Root node.
 * @param {Array} root.children An array of nodes.
 * @param {Function} leafPredicate Predicate function to determine if a node will be a leaf.
 * @return {Object}
 */
export let pruneTreeByLeaves = (root, leafPredicate) => {
  let clonedRoot = Object.assign({}, root, {
    children: (root.children || []).map(c => pruneTreeByLeaves(c, leafPredicate)).filter(c => c != null)
  })

  // If any children match the leaf predicate, we will return the clonedRoot.
  // Or, if the clonedRoot matched the leafPredicate, we will return the clonedRoot.
  if (clonedRoot.children.length > 0 || leafPredicate(clonedRoot)) {
    return clonedRoot;
  }
}

/**
 * For any node in a tree that does not pass `nodePredicate`, replace it with
 * its children. A new tree will be returned.
 *
 * @param {Object} root Root node of a tree.
 * @param {Function} nodePredicate Predicate function to determine if a node
 * will be kept.
 * @return {Object}
 */
export let pruneDescendantNodes = (root, nodePredicte) =>
  Object.assign({}, root, {
    children: pruneNodes(root.children, nodePredicte)
  })

/**
 * Recursively replace any node that does not pass `nodePredicate` with its
 * children. A new array of nodes will be returned.
 *
 * @param {Array} nodes An array of nodes. This will typically be the children
 * of a node in a tree.
 * @param {Function} nodePredicate Predicate function to determine if a node
 * will be kept.
 * @return {Array}
 */
export let pruneNodes = (nodes, nodePredicate) =>
  nodes.reduce((prunedNodes, node) => {
    let prunedNode = pruneDescendantNodes(node, nodePredicate);
    return nodePredicate(prunedNode)
      ? pushInto(prunedNodes, prunedNode)
      : pushInto(prunedNodes, ...prunedNode.children);
  }, [])

/**
 * If the root node has only one child, replace the root node with it's child.
 *
 * @param {Object} root Root node of a tree
 * @return {Object} Tree
 */
export let compactRootNodes = (root) =>
  root.children.length === 1 ? compactRootNodes(root.children[0])
  : root
