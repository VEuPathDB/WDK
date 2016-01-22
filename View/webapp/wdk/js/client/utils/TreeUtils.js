// Helper function to push values into an array, and to return that array.
// `push` returns the value added, so this is useful when we want the array
// back. This is more performant than using `concat` which creates a new array.
let pushInto = (array, ...values) =>
  (array.push(...values), array);

/**
 * Reduce a tree to a single value.
 *
 * @param {Function} fn Reducer function called with two arguments for each
 * node: (accumulatedValue, node)
 * @param {any} value Seed value used for the initial accumulatedValue of `fn`.
 * If ommitted, the root node will be used for the initialValue, and the first
 * child of the root node will be used for the first node.
 * @param {Object} root Root node of tree.
 */
export let reduce = (fn, value, root) =>
  root == undefined ? value.children.reduce(reduce.bind(null, fn), value)
  : root.children.reduce(reduce.bind(null, fn), fn(value, root))

/**
 * Like reduce, but iterate bottom-up.
 *
 * @param {Function} fn Reducer function called with two arguments for each
 * node: (accumulatedValue, node)
 * @param {any} value Seed value used for the initial accumulatedValue of `fn`.
 * If ommitted, the root node will be used for the initialValue, and the first
 * child of the root node will be used for the first node.
 * @param {Object} root Root node of tree.
 */
export let reduceBottom = (fn, value, root) =>
  root === undefined ? fn(value.children.reduce(reduceBottom.bind(null, fn)), value)
  : fn(root.children.reduce(reduceBottom.bind(null, fn), value), root)

/**
 * Create an array of nodes that satisfy a condition.
 *
 * @param {Function} fn Predicate function. Nodes for which this returns true
 * will be included in the returned list.
 * @param {Object} root Root of tree.
 */
export let filter = (fn, root) =>
  reduce((items, node) => fn(node) ? pushInto(items, node) : items, [], root)

/**
 * Return the first node that satisfies a condition.
 *
 * @param {Function} fn Predicate function.
 * @param {Object} root Root of tree.
 */
export let find = (fn, root) =>
  reduce((found, node) => found == null && fn(node) ? node : found, null, root)

/**
 * For any node in a tree that does not pass `nodePredicate`, replace it with
 * its children. A new tree will be returned.
 *
 * @param {Function} fn Predicate function to determine if a node
 * will be kept.
 * @param {Object} root Root node of a tree.
 * @return {Object}
 */
export let pruneDescendantNodes = (fn, root) =>
  Object.assign({}, root, {
    children: pruneNodes(fn, root.children)
  })

/**
 * Recursively replace any node that does not pass `nodePredicate` with its
 * children. A new array of nodes will be returned.
 *
 * @param {Function} fn Predicate function to determine if a node
 * will be kept.
 * @param {Array} nodes An array of nodes. This will typically be the children
 * of a node in a tree.
 * @return {Array}
 */
export let pruneNodes = (fn, nodes) =>
  nodes.reduce((prunedNodes, node) => {
    let prunedNode = pruneDescendantNodes(fn, node);
    return fn(prunedNode)
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

  
//Utility functions for CheckboxTree React component

  /** 
   * Simple convenience method to identify nodes that are leaves
   * @param {Object} node representing root of subtree (possibly a leaf)
   * @return {Boolean} indicates true if the node is a leaf and false otherwise
   */
  export let isLeafNode = node => node.children.length === 0;

  /**
   * Using recursion to return all the leaf node ids for the given node.
   * @param {Object} node representing root of subtree
   * @param {Array} initial list of leaf node ids (optional)
   * @return {Array} updated list of leaf node ids
   */
  export let getLeaves = (node, leaves=[]) => {
   if(!isLeafNode(node)) {
     node.children.map(function(child) {

       // push only leaf nodes into the array
       if(isLeafNode(child)) {
         leaves.push(child);
       }
       getLeaves(child,leaves);
     });
   }
   return leaves;
  };