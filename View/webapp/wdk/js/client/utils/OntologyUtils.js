import kebabCase from 'lodash/string/kebabCase';
import get from 'lodash/object/get';
import {
  compactRootNodes,
  pruneDescendantNodes
} from './TreeUtils';

let hasChildren = node => node.children.length > 0;

/**
 * Get a sub-tree from an Ontology. The `leafPredicate` function
 * is used to find the leaves of the tree to return.
 *
 * @param {Ontology} ontology
 * @param {Function} leafPredicate
 */
export let getTree = (ontology, leafPredicate) =>
  compactRootNodes(
    pruneDescendantNodes(node => hasChildren(node) || leafPredicate(node), ontology.tree));


let includes = (array, value) => array != null && array.indexOf(value) > -1;

export let nodeHasProperty = (name, value, node) => includes(node.properties[name], value);

export let getPropertyValues = (name, node) => get(node, [ 'properties', name ]) || [];

export let getPropertyValue = (name, node) => get(node, [ 'properties', name, 0 ]);

export let getId = node => kebabCase(getPropertyValue('label', node));
