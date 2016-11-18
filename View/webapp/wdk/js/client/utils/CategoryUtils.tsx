import {flowRight as compose, kebabCase, memoize} from 'lodash';
import * as React from 'react';
import {preorderSeq} from './TreeUtils';
import {
  getTree,
  nodeHasChildren,
  getNodeChildren,
  nodeHasProperty,
  getPropertyValue,
  getPropertyValues,
  OntologyNode,
  Ontology
} from './OntologyUtils';
import {areTermsInString} from './SearchUtils';
import {Question, RecordClass} from './WdkModel';

type Dict<T> = {
  [key: string]: T;
};

type TargetType = 'search'|'attribute'|'table';
type Scope = 'record' | 'record-internal' | 'results' | 'results-internal' | 'download' | 'download-internal';

interface CategoryNodeProperties {
  targetType?: [TargetType];
  scope?: [Scope];
  label?: string[];
  name?: string[];
  'EuPathDB alternative term'?: string[];
  hasDefinition?: string[];
  hasNarrowSynonym?: string[];
  hasExactSynonym?: string[];
}

export interface CategoryNode extends OntologyNode {
  type: 'category';
  children: CategoryTreeNode[];
  properties: CategoryNodeProperties & { [key: string]: string[]; };
}

export interface IndividualNode extends OntologyNode {
  type: 'individual';
  properties: CategoryNodeProperties & { [key: string]: string[]; };
  wdkReference: {
    name: string;
    displayName: string;
    help?: string;
    summary?: string;
  };
}

export type CategoryTreeNode = CategoryNode | IndividualNode;

export function getId(node: CategoryTreeNode) {
  return isIndividual(node) ? node.wdkReference.name : kebabCase(getLabel(node));
}

export function getLabel(node: CategoryTreeNode) {
  return getPropertyValue('label', node);
}

export function getTargetType(node: CategoryTreeNode) {
  return getPropertyValue('targetType', node);
}

export function getScope(node: CategoryTreeNode) {
  return getPropertyValue('scope', node);
}

export function getRefName(node: CategoryTreeNode) {
  return getPropertyValue('name', node);
}

export function getRecordClassName(node: CategoryTreeNode) {
  return getPropertyValue('recordClassName', node);
}

export function getDisplayName(node: CategoryTreeNode) {
  return isIndividual(node) ? node.wdkReference.displayName
       : getPropertyValue('EuPathDB alternative term', node);
}

export function getDescription(node: CategoryTreeNode) {
  return isIndividual(node) ? node.wdkReference.help
       : getPropertyValue('hasDefinition', node);
}

export function getTooltipContent(node: CategoryTreeNode) {
  return isIndividual(node) && nodeHasProperty('targetType', 'search', node) ? node.wdkReference.summary
       : getDescription(node);
}

export function getSynonyms(node: CategoryTreeNode) {
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
export function createNode(id: string, displayName: string, description: string, children: CategoryTreeNode[] = []): CategoryTreeNode {
  return children.length > 0 ? {
    type: 'category',
    properties: {
      label: [id],
      hasDefinition: [description],
      'EuPathDB alternative term': [displayName]
    },
    children
  } : {
    type: 'individual',
    properties : {
      targetType : ['attribute'],
      name : [id]
    },
    wdkReference : {
      name: id,
      displayName : displayName,
      help : description
    },
    children
  }
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

/**
 * Create a predicate function to filter out of the Categories ontology tree those items appropriate for the given
 * scope that identify attributes for the current record class.  In the case of the Transcript Record Class, a
 * distinction is made depending on whether the summary view applies to transcripts or genes.
 */
export function isQualifying(spec: { targetType?: string; recordClassName?: string; scope?: string; }) {
  return function(node: CategoryNode) {
    // We have to cast spec as StringDict to avoid an implicitAny error
    // See http://stackoverflow.com/questions/32968332/how-do-i-prevent-the-error-index-signature-of-object-type-implicitly-has-an-an
    return Object.keys(spec).every(prop => nodeHasProperty(prop, (spec as Dict<string>)[prop], node));
  };
}

export function isIndividual(node: CategoryTreeNode): node is IndividualNode {
  return node.type === 'individual';
}

/**
 * Callback to provide a React element holding the display name and description for the node
 * @param node - given node
 * @returns {React.Element} - React element
 */
export function BasicNodeComponent(props: {node: CategoryTreeNode}) {
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
  return areTermsInString(searchQueryTerms, getDisplayName(node) + ' ' +
                          getTooltipContent(node));
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
 * Returns an array of all the IDs of the leaf nodes in the passed tree.  If the
 * root has no children, this function assumes a "null" tree, and returns an empty array.
 */
export function getAllLeafIds(ontologyTreeRoot: CategoryNode): string[] {
  return (!nodeHasChildren(ontologyTreeRoot) ? [] : getAllLeafIdsNoCheck(ontologyTreeRoot));
}

/**
 * Returns an array of all the IDs of the leaf nodes in the passed tree.
 */
function getAllLeafIdsNoCheck(ontologyTreeRoot: CategoryNode): string[] {
  let collectIds = (leafIds: string[], node: CategoryNode): string[] =>
    (!nodeHasChildren(node) ? leafIds.concat(getNodeId(node)) :
      getNodeChildren(node).reduce(collectIds, leafIds));
  return collectIds([], ontologyTreeRoot);
}

export const normalizeOntology = memoize(compose(sortOntology, pruneUnresolvedReferences, resolveWdkReferences));

/**
 * Adds the related WDK reference to each node. This function mutates the
 * ontology tree, which is ok since we are doing this before we cache the
 * result. It might be useful for this to return a new copy of the ontology
 * in the future, but for now this saves some performance.
 */
function resolveWdkReferences(recordClasses: Dict<RecordClass>, questions: Dict<Question>, ontology: Ontology<CategoryTreeNode>) {
  for (let node of preorderSeq(ontology.tree)) {
    switch (getTargetType(node)) {
      case 'attribute': {
        let attributeName = getRefName(node);
        let recordClass = recordClasses[getPropertyValue('recordClassName', node)];
        if (recordClass == null) continue;
        let wdkReference = recordClass.attributesMap[attributeName];
        Object.assign(node, { wdkReference });
        Object.assign(node, { type: 'individual'});
        break;
      }

      case 'table': {
        let tableName = getRefName(node);
        let recordClass = recordClasses[getPropertyValue('recordClassName', node)];
        if (recordClass == null) continue;
        let wdkReference = recordClass.tablesMap[tableName];
        Object.assign(node, { wdkReference });
        Object.assign(node, { type: 'individual'});
        break;
      }

      case 'search': {
        let questionName = getRefName(node);
        let wdkReference = questions[questionName];
        Object.assign(node, { wdkReference });
        Object.assign(node, { type: 'individual'});
        break;
      }

      case 'default':
        Object.assign(node, { type: 'category'});
        break;
    }
  }
  return ontology;
}

function isResolved(node: CategoryTreeNode) {
  return isIndividual(node) ? node.wdkReference != null : true;
}

function pruneUnresolvedReferences(ontology: Ontology<CategoryTreeNode>) {
  //ontology.unprunedTree = ontology.tree;
  ontology.tree = getTree(ontology, isResolved);
  return ontology;
}

/**
 * Compare nodes based on the "sort order" property. If it is undefined,
 * compare based on displayName.
 */
function compareOntologyNodes(nodeA: CategoryNode, nodeB: CategoryNode) {
  if (nodeA.children.length === 0 && nodeB.children.length !== 0)
    return -1;

  if (nodeB.children.length === 0 && nodeA.children.length !== 0)
    return 1;

  let orderBySortNum = compareOnotologyNodesBySortNumber(nodeA, nodeB);
  return orderBySortNum === 0 ? compareOntologyNodesByDisplayName(nodeA, nodeB) : orderBySortNum;
}

/**
 * Sort ontology node siblings. This function mutates the tree, so should
 * only be used before caching the ontology.
 */
function sortOntology(ontology: Ontology<CategoryNode>) {
  for (let node of preorderSeq(ontology.tree)) {
    node.children.sort(compareOntologyNodes);
  }
  return ontology;
}

function compareOnotologyNodesBySortNumber(nodeA: CategoryNode, nodeB: CategoryNode) {
  let sortOrderA = getPropertyValue('display order', nodeA);
  let sortOrderB = getPropertyValue('display order', nodeB);
  return sortOrderA && sortOrderB ? Number(sortOrderA) - Number(sortOrderB)
       : sortOrderA ? -1
       : sortOrderB ? 1
       : 0;
}

function compareOntologyNodesByDisplayName(nodeA: CategoryNode, nodeB: CategoryNode) {
  // attempt to sort by displayName
  let nameA = getDisplayName(nodeA) || '';
  let nameB = getDisplayName(nodeB) || '';
  return nameA < nameB ? -1 : 1;
}
