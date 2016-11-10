import { seq, map } from './IterableUtils';
import { curry } from 'lodash';

export interface Node {
  children: Array<Node>;
}

export interface ChildrenGetter<T> {
  (t: T): T[];
}

// Helper function to push values into an array, and to return that array.
// `push` returns the value added, so this is useful when we want the array
// back. This is more performant than using `concat` which creates a new array.
function pushInto <T>(array: T[], ...values: T[]) {
  return (array.push(...values), array);
}

// Shallow comparison of two arrays
function shallowEqual <T>(array1: T[], array2: T[]) {
  if (array1.length !== array2.length) return false;
  for (let i = 0; i < array1.length; i++) {
    if (array1[i] !== array2[i]) return false;
  }
  return true;
}

/** top-down tree node iterator */
function* preorder<T>(root: T, getChildren: ChildrenGetter<T>): Iterable<T> {
  yield root;
  let children = getChildren(root);
  let length = children.length;
  for (let i = 0; i < length; i++) {
    yield* preorder(children[i], getChildren);
  }
}

/** bottom-up tree node iterator */
function* postorder<T>(root: T, getChildren: ChildrenGetter<T>): Iterable<T> {
  let children = getChildren(root);
  let length = children.length;
  for (let i = 0; i < length; i++) {
    yield* postorder(children[i], getChildren);
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
export function preorderSeq<T extends Node>(root: T) {
  return seq({
    *[Symbol.iterator]() {
      yield* preorder(root, n => n.children as T[]);
    }
  })
}

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
export function postorderSeq<T extends Node>(root: T) {
  return seq({
    *[Symbol.iterator]() {
      yield* postorder(root, n => n.children as T[]);
    }
  })
}

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
export function mapStructure<T, U>(mapFn: (root: T, children: U[]) => U, getChildren: ChildrenGetter<T>, root: T): U {
  let mappedChildren = map(child => {
    return mapStructure(mapFn, getChildren, child)
  }, getChildren(root));
  return mapFn(root, Array.from(mappedChildren));
}

export const foldStructure = curry(<T extends Node, U>(fn: (value: U, node: T) => U, seed: U, root: T): U =>
  fn(root.children.reduce(foldStructure(fn), seed), root))

/**
 * For any node in a tree that does not pass `nodePredicate`, replace it with
 * its children. A new tree will be returned.
 *
 * @param {Function} fn Predicate function to determine if a node
 * will be kept.
 * @param {Object} root Root node of a tree.
 * @return {Object}
 */
export function pruneDescendantNodes<T extends Node>(fn: (node: T) => boolean, root: T) {
  let prunedChildren = pruneNodes(fn, root.children);
  return prunedChildren === root.children
    ? root
    : Object.assign({}, root, {
      children: prunedChildren
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
export function pruneNodes (fn: (node: Node) => boolean, nodes: Node[]): Node[] {
  let prunedNodes = nodes.reduce((prunedNodes, node) => {
    let prunedNode = pruneDescendantNodes(fn, node);
    return fn(prunedNode)
      ? pushInto(prunedNodes, prunedNode)
      : pushInto(prunedNodes, ...prunedNode.children);
  }, [] as Node[]);
  return shallowEqual(nodes, prunedNodes) ? nodes : prunedNodes;
}

/**
 * If the root node has only one child, replace the root node with it's child.
 *
 * @param {Object} root Root node of a tree
 * @return {Object} Tree
 */
export function compactRootNodes (root: Node): Node {
  return root.children.length === 1 ? compactRootNodes(root.children[0])
  : root
}

export function mapNodes (nodeTransform: (root: Node) => Node, root: Node): Node {
  return Object.assign({}, nodeTransform(root), {
    children: root.children.map(child => mapNodes(nodeTransform, child))
  });
}

/**
 * Get an array of nodes that satisfy nodePredicate
 */
export function filterNodes (nodePredicate: (node: Node) => boolean, node: Node) {
  return preorderSeq(node).filter(nodePredicate).toArray();
}

/**
 * Simple convenience method to identify nodes that are leaves
 * @param {Object} node representing root of subtree (possibly a leaf)
 * @return {Boolean} indicates true if the node is a leaf and false otherwise
 */
export function isLeaf <T>(node: T, getNodeChildren: ChildrenGetter<T>) {
   return getNodeChildren(node).length === 0;
}

/**
 * Using recursion to return all the leaf nodes for the given node.
 * @param {Object} node representing root of subtree
 * @param {Array} initial list of leaf nodes (optional)
 * @return {Array} updated list of leaf nodes
 */
export function getLeaves <T>(node: T, getNodeChildren: ChildrenGetter<T>, leaves: T[] = []) {
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
export function getBranches <T>(node: T, getNodeChildren: ChildrenGetter<T>, branches: T[] = []) {
  if(!isLeaf(node, getNodeChildren)) {
    branches.push(node);
    getNodeChildren(node).map(child => getBranches(child, getNodeChildren, branches));
  }
  return branches;
};
