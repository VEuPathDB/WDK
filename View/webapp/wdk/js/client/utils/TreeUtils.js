import { seq } from './IterableUtils';

// Helper function to push values into an array, and to return that array.
// `push` returns the value added, so this is useful when we want the array
// back. This is more performant than using `concat` which creates a new array.
let pushInto = (array, ...values) =>
  (array.push(...values), array);

// Tree iterators. These can be used in combination with for-of loops, or
// with the Iterable util functions.
function* preorder(root) {
  yield root;
  for (let child of root.children) {
    yield* preorder(child);
  }
}

function* postorder(root) {
  for (let child of root.children) {
    yield* postorder(child);
  }
  yield root;
}


/**
 * Create a Seq of tree nodes in preorder sequence.
 *
 *              1
 *             / \
 *            /   \
 *           /     \
 *          2       3
 *         / \     /
 *        4   5   6
 *       /       / \
 *      7       8   9
 *
 *     preorder:    1 2 4 7 5 3 6 8 9
 *
 * @param {Object} root
 * @return {Seq}
 */
export let preorderSeq = (root) =>
  seq({
    [Symbol.iterator]() {
      return preorder(root);
    }
  })

/**
 * Create a Seq of tree nodes in postorder sequence.
 *
 *              1
 *             / \
 *            /   \
 *           /     \
 *          2       3
 *         / \     /
 *        4   5   6
 *       /       / \
 *      7       8   9
 *
 *     postorder:   7 4 5 2 8 9 6 3 1
 *
 * @param {Object} root
 * @return {Seq}
 */
export let postorderSeq = (root) =>
  seq({
    [Symbol.iterator]() {
      return postorder(root);
    }
  })

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
