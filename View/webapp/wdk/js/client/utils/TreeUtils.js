import { seq, map } from './IterableUtils';

// Helper function to push values into an array, and to return that array.
// `push` returns the value added, so this is useful when we want the array
// back. This is more performant than using `concat` which creates a new array.
let pushInto = (array, ...values) =>
  (array.push(...values), array);

// Shallow comparison of two arrays
let shallowEqual = (array1, array2) => {
  if (array1.length !== array2.length) return false;
  for (let i = 0; i < array1.length; i++) {
    if (array1[i] !== array2[i]) return false;
  }
  return true;
}

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
 * A mapping function to pass to `mapStructure`.
 *
 * @callback mapFn
 * @param {any} node
 * @param {Array} array of mapped children
 */

/**
 * Convert a tree into a new tree-like structure. The tree is traversed bottom-
 * up, and for each node, its mapped children are passed to the mapping
 * function. This allows the mapping function to integrate the mapped children
 * however it needs to.
 *
 * @param {mapFn} mapFn Mapping function to apply to each node.
 * @param {Function} getChildren A function that returns an iterable object over a node's children.
 * @param {any} root The root node of the tree whose structure is being mapped.
 */
export function mapStructure(mapFn, getChildren, root) {
  let mappedChildren = map(child => {
    return mapStructure(mapFn, getChildren, child)
  }, getChildren(root));
  return mapFn(root, Array.from(mappedChildren));
}

/**
 * For any node in a tree that does not pass `nodePredicate`, replace it with
 * its children. A new tree will be returned.
 *
 * @param {Function} fn Predicate function to determine if a node
 * will be kept.
 * @param {Object} root Root node of a tree.
 * @return {Object}
 */
export let pruneDescendantNodes = (fn, root) => {
  let prunedChildren = pruneNodes(fn, root.children);
  return prunedChildren === root.children
    ? root
    : Object.assign({}, root, {
      children: pruneNodes(fn, root.children)
    })
}

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
export let pruneNodes = (fn, nodes) => {
  let prunedNodes = nodes.reduce((prunedNodes, node) => {
    let prunedNode = pruneDescendantNodes(fn, node);
    return fn(prunedNode)
      ? pushInto(prunedNodes, prunedNode)
      : pushInto(prunedNodes, ...prunedNode.children);
  }, []);
  return shallowEqual(nodes, prunedNodes) ? nodes : prunedNodes;
}

/**
 * If the root node has only one child, replace the root node with it's child.
 *
 * @param {Object} root Root node of a tree
 * @return {Object} Tree
 */
export let compactRootNodes = (root) =>
  root.children.length === 1 ? compactRootNodes(root.children[0])
  : root

export let mapNodes = (nodeTransform, root) => {
  return Object.assign({}, nodeTransform(root), {
    children: root.children.map(child => mapNodes(nodeTransform, child))
  });
}

/**
 * Simple convenience method to identify nodes that are leaves
 * @param {Object} node representing root of subtree (possibly a leaf)
 * @return {Boolean} indicates true if the node is a leaf and false otherwise
 */
export let isLeaf = (node, getNodeChildren) => getNodeChildren(node).length === 0;

/**
 * Using recursion to return all the leaf nodes for the given node.
 * @param {Object} node representing root of subtree
 * @param {Array} initial list of leaf nodes (optional)
 * @return {Array} updated list of leaf nodes
 */
export let getLeaves = (node, getNodeChildren, leaves=[]) => {
 if(!isLeaf(node, getNodeChildren)) {
   getNodeChildren(node).map(function(child) {

     // push only leaf nodes into the array
     if(isLeaf(child, getNodeChildren)) {
       leaves.push(child);
     }
     getLeaves(child, getNodeChildren, leaves);
   });
 }
 return leaves;
};

/**
 * Using recursion to return all the branch nodes for a given node
 * @param {Object} node representing root of subtree
 * @param {Array} initial list of branch nodes (optional)
 * @return {Array} updated list of branch nodes
 */
export let getBranches = (node, getNodeChildren, branches=[]) => {
  if(!isLeaf(node, getNodeChildren)) {
    branches.push(node);
    getNodeChildren(node).map(child => getBranches(child, getNodeChildren, branches));
  }
  return branches;
};

/**
 * Using recursion to descend the tree to find the node associated with the node id given
 * @param {String} nodeId of the node to find
 * @param {Object} root node of subtree to search
 * @param {Function} function that returns array of child nodes of the passed node
 * @param {Function} function that returns the id of the passed node
 * @return {Object} the node corresponding to the node id or undefined if
 * not found.
 */
export let findNodeById = (nodeId, root, getNodeChildren, getNodeId) => {
  if (getNodeId(root) === nodeId) {
    return root;
  }
  let children = getNodeChildren(root);
  for (let i = 0; i < children.length; i++) {
    let node = findNodeById(nodeId, children[i], getNodeChildren, getNodeId);
    if (node !== undefined) return node;
  }
  return undefined;
}
