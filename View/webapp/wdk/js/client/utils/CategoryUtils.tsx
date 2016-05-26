import {kebabCase} from 'lodash';
import * as React from 'react';
import {mapStructure} from './TreeUtils';
import {getTree, nodeHasChildren, getNodeChildren, nodeHasProperty,
  getPropertyValue, getPropertyValues, OntologyNode} from './OntologyUtils';
import {areTermsInString} from './SearchUtils';
import {Question} from './WdkModel';

interface CategoryNodeProperties {
  targetType?: string[];
  scope?: string[];
  label?: string[];
  name?: string[];
  'EuPathDB alternative term'?: string[];
  hasDefinition?: string[];
  hasNarrowSynonym?: string[];
  hasExactSynonym?: string[];
}

export interface CategoryNode extends OntologyNode {
  children: CategoryNode[];
  properties: CategoryNodeProperties & { [key: string]: string[]; };
  wdkReference?: {
    name: string;
    displayName: string;
    help?: string;
  };
}

export type TargetType = 'search' | 'attribute' | 'table';

export type Scope = 'record' | 'record-internal' | 'results' | 'results-internal' | 'download' | 'download-internal';

export function getId(node: CategoryNode) {
  // replace whitespace with hyphens
  return kebabCase(getPropertyValue('label', node));
}

export function getLabel(node: CategoryNode) {
  return getPropertyValue('label', node);
}

export function getTargetType(node: CategoryNode) {
  return getPropertyValue('targetType', node);
}

export function getScope(node: CategoryNode) {
  return getPropertyValue('scope', node);
}

export function getRefName(node: CategoryNode) {
  return getPropertyValue('name', node);
}

export function getRecordClassName(node: CategoryNode) {
  return getPropertyValue('recordClassName', node);
}

export function getDisplayName(node: CategoryNode) {
  return (node.wdkReference && node.wdkReference.displayName) ||
  getPropertyValue('EuPathDB alternative term', node);
}

export function getDescription(node: CategoryNode) {
  return (node.wdkReference && node.wdkReference.help) ||
  getPropertyValue('hasDefinition', node);
}

export function getSynonyms(node: CategoryNode) {
  return getPropertyValues('hasNarrowSynonym', node)
  .concat(getPropertyValues('hasExactSynonym', node));
}

// TODO Make this more genericL createCategoryNode and createWdkEntityNode (or, createLeafNode??)
/**
 * Returns a JSON object representing a simplified category tree node that will be properly interpreted
 * by the checkboxTreeController
 * @param id - name or id of the node
 * @param displayName - name to be displayed
 * @param description - tooltip
 * @returns {{properties: {targetType: string[], name: *[]}, wdkReference: {displayName: *, help: *}, children: Array}}
 */
export function createNode(id: string, displayName: string, description: string, children: CategoryNode[]): CategoryNode {
  return {
    children,
    properties : {
      targetType : ["attribute"],
      name : [id]
    },
    wdkReference : {
      name: id,
      displayName : displayName,
      help : description
    }
  } as CategoryNode;
}

/**
 * Creates and adds a subtree to the given category tree containing the
 * question-specific (i.e. dynamic) attributes associated with the given
 * question.  The subtree is added as the first child.
 *
 * @param question question whose dynamic attributes should be added
 * @param categoryTree root node of a categories ontology tree to modify
 */
export function addSearchSpecificSubtree(question: Question, categoryTree: CategoryNode): CategoryNode {
  if (question.dynamicAttributes.length > 0) {
    let questionNodes = question.dynamicAttributes.map(attribute => {
      return createNode(attribute.name, attribute.displayName, attribute.help, []);
    });
    let subtree = createNode(
      "search_specific_subtree",
      "Search Specific",
      "Information about the records returned that is specific to the search you ran, and the parameters you specified",
      questionNodes
    );
    return Object.assign({}, categoryTree, {
      children: [ subtree ].concat(categoryTree.children)
    })
  }
  return categoryTree;
}

/**
 * Callback to provide the value/id of the node (i.e. checkbox value).  Using 'name' for
 * leaves and processed 'label' for branches
 * @param node - given id
 * @returns {*} - id/value of node
 */
export function getNodeId(node: CategoryNode): string {
  // FIXME: document why the special case for attributes and tables
  let targetType = getTargetType(node);
  return (targetType === 'attribute' || targetType === 'table' ? getRefName(node) : getId(node));
}

interface CategoryNodePropertySpec {
  targetType?: string;
  recordClassName?: string;
  scope?: string;
};

interface StringDict {
  [key: string]: string;
}
/**
 * Create a predicate function to filter out of the Categories ontology tree those items appropriate for the given
 * scope that identify attributes for the current record class.  In the case of the Transcript Record Class, a
 * distinction is made depending on whether the summary view applies to transcripts or genes.
 */
export function isQualifying(spec: CategoryNodePropertySpec) {
  return function(node: CategoryNode) {
    // We have to cast spec as StringDict to avoid an implicitAny error
    // See http://stackoverflow.com/questions/32968332/how-do-i-prevent-the-error-index-signature-of-object-type-implicitly-has-an-an
    return Object.keys(spec).every(prop => nodeHasProperty(prop, (spec as StringDict)[prop], node));
  };
};

export interface NodeComponentProps {
  node: CategoryNode;
}
/**
 * Callback to provide a React element holding the display name and description for the node
 * @param node - given node
 * @returns {React.Element} - React element
 */
export function BasicNodeComponent(props: NodeComponentProps) {
  return ( <span title={getDescription(props.node)}>{getDisplayName(props.node)}</span> );
}

/**
 * Returns whether the passed node 'matches' the passed node's display name
 * or description.
 *
 * @param node node to test
 * @param searchText search text to match against
 * @returns true if node 'matches' the passed search text
 */
export function nodeSearchPredicate(node: CategoryNode, searchQueryTerms: string[]): boolean {
  return areTermsInString(searchQueryTerms, getDisplayName(node)) ||
    areTermsInString(searchQueryTerms, getDescription(node));
}

/**
 * Finds the "left-most" leaf in the tree and returns its ID using getNodeId()
 */
export function findFirstLeafId(ontologyTreeRoot: CategoryNode): string {
  if (nodeHasChildren(ontologyTreeRoot)) {
    return findFirstLeafId(getNodeChildren(ontologyTreeRoot)[0] as CategoryNode);
  }
  return getNodeId(ontologyTreeRoot);
}

/**
 * Returns an array of all the IDs of the leaf nodes in the passed tree
 */
export function getAllLeafIds(ontologyTreeRoot: CategoryNode): string[] {
  let collectIds = (leafIds: string[], node: CategoryNode): string[] =>
    (!nodeHasChildren(node) ? leafIds.concat(getNodeId(node)) :
      getNodeChildren(node).reduce(collectIds, leafIds));
  return collectIds([], ontologyTreeRoot);
}
