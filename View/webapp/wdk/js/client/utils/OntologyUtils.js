import kebabCase from 'lodash/string/kebabCase';
import get from 'lodash/object/get';
import {
  compactRootNodes,
  pruneDescendantNodes
} from './TreeUtils';

let hasChildren = node =>
  node.children.length > 0;

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


let includes = (array, value) =>
  array != null && array.indexOf(value) > -1;

export let nodeHasProperty = (name, value, node) =>
  includes(node.properties[name], value);

export let getPropertyValues = (name, node) =>
  get(node, [ 'properties', name ]) || [];

export let getPropertyValue = (name, node) =>
  get(node, [ 'properties', name, 0 ]);

export let getId = node =>
  // replace whitespace with hyphens
  kebabCase(getPropertyValue('label', node));

export let getLabel = node =>
  getPropertyValue('label', node);

export let getTargetType = node =>
  getPropertyValue('targetType', node);

export let getRefName = node =>
  getPropertyValue('name', node);

export let getDisplayName = node =>
  getPropertyValue('EuPathDB alternative term', node);

export let getDescription = node =>
  getPropertyValue('hasDefinition', node);

export let getSynonyms = node =>
  [ ...getPropertyValues('hasNarrowSynonym'), ...getPropertyValues('hasExactSynonym') ]
