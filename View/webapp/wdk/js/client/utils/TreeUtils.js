import { seq } from './IterableUtils';

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


//Utility functions for CheckboxTree React component

  /**
   * Simple convenience method to identify nodes that are leaves
   * @param {Object} node representing root of subtree (possibly a leaf)
   * @return {Boolean} indicates true if the node is a leaf and false otherwise
   */
  export let isLeafNode = node => node.children.length === 0;

  /**
   * Using recursion to return all the leaf nodes for the given node.
   * @param {Object} node representing root of subtree
   * @param {Array} initial list of leaf nodes (optional)
   * @return {Array} updated list of leaf nodes
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
  
  /**
   * Using recursion to return all the branch nodes for a given node
   * @param {Object} node representing root of subtree
   * @param {Array} initial list of branch nodes (optional)
   * @return {Array} updated list of branch nodes
   */
  export let getBranches = (node, branches=[]) => {
	  if(!isLeafNode(node)) {
	    branches.push(node);
	    node.children.map(child => getBranches(child, branches));
	  }
	  return branches;
	}


	/**
	 * Using recursion to descend the tree to find the node associate with the node id given
	 * @param {String} nodeId of the node to find
	 * @param {Array} list of the tree's top level nodes
	 * @return {Object} the node corresponding to the node id or undefined if
	 * not found.
	 */ 
	export let getNodeById = (nodeId, nodes) => {
	  for(let i = 0; i < nodes.length; i++) {
	    let node = undefined;
	    if(nodes[i].id === nodeId) {
	      return nodes[i];
	    }
	    if(nodes[i].children.length > 0) {
	      node = getNodeById(nodeId, nodes[i].children);
	      if(node !== undefined) {
	        return node;
	      }
	    }
	  }
	  return undefined;
	}
