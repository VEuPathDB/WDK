import React from 'react';
import ReactDOM from 'react-dom';
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

// TODO Move this into a Category specific module and refine/normalize
// property names.

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
  get(node, [ 'wdkReference', 'displayName' ]) ||
  getPropertyValue('EuPathDB alternative term', node);

export let getDescription = node =>
  get(node, [ 'wdkReference', 'help' ]) ||
  getPropertyValue('hasDefinition', node);

export let getSynonyms = node =>
  [ ...getPropertyValues('hasNarrowSynonym'), ...getPropertyValues('hasExactSynonym') ]



/**
 * Callback to provide the value/id of the node (i.e., checkbox value).  Using 'name' for
 * leaves and processed 'label' for branches
 * @param node - given id
 * @returns {*} - id/value of node
 */
export let getNodeFormValue = node =>
  getTargetType(node) === 'attribute' ? getRefName(node) : getId(node);


/**
 * Callback to provide a React element holding the display name and description for the node
 * @param node - given node
 * @returns {XML} - React element
 */
export let getBasicNodeReactElement = node =>
  <span title={getDescription(node)}>{getDisplayName(node)}</span>


/**
 * Callback to provide the node children
 * @param node - given node
 * @returns {Array}  child nodes
 */
export let getNodeChildren = node =>
  node.children;