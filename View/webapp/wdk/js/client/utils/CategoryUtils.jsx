import get from 'lodash/object/get';
import kebabCase from 'lodash/string/kebabCase';
import { getTree, nodeHasProperty, getPropertyValue } from './OntologyUtils';

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
 * Returns a JSON object representing a simplified category tree node that will be properly interpreted
 * by the checkboxTreeController
 * @param id - name or id of the node
 * @param displayName - name to be displayed
 * @param description - tooltip
 * @returns {{properties: {targetType: string[], name: *[]}, wdkReference: {displayName: *, help: *}, children: Array}}
 */
function createNode(id, displayName, description) {
  return {
    "properties" : {
      "targetType" : ["attribute"],
      "name" : [id]
    },
    "wdkReference" : {
      "displayName" : displayName,
      "help" : description
    },
    "children":[]
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
function addSearchSpecificSubtree(question, categoryTree) {
  if (question.dynamicAttributes.length > 0) {
    let subtree = createNode("search_specific_subtree","Search Specific",
                             "Information about the records returned that is specific to the search you ran, and the parameters you specified");
    question.dynamicAttributes.forEach(attribute => {
      let node = createNode(attribute.name, attribute.displayName, attribute.help);
      subtree.children.push(node);
    });
    categoryTree.children.unshift(subtree);
  }
}

/**
 * Callback to provide the value/id of the node (i.e. checkbox value).  Using 'name' for
 * leaves and processed 'label' for branches
 * @param node - given id
 * @returns {*} - id/value of node
 */
export let getNodeId = node =>
  // FIXME: document why the special case for attributes
  getTargetType(node) === 'attribute' ? getRefName(node) : getId(node);


/**
 * Callback to provide a React element holding the display name and description for the node
 * @param node - given node
 * @returns {XML} - React element
 */
export let BasicNodeComponent = props =>
  ( <span title={getDescription(props.node)}>{getDisplayName(props.node)}</span> );

/**
 * Create a predicate function to filter out of the Categories ontology tree those items appropriate for the given
 * scope that identify attributes for the current record class.  In the case of the Transcript Record Class, a
 * distinction is made depending on whether the summary view applies to transcripts or genes.
 * 
 * @param recordClassName - full name of the current record class
 * @param viewName - either gene or transcript depending on the summary view
 */
export let isQualifying = (recordClassName, viewName, scope) => node => {
  let qualified = nodeHasProperty('targetType', 'attribute', node)
                && nodeHasProperty('recordClassName', recordClassName, node)
                && nodeHasProperty('scope', scope, node);
  if(qualified && recordClassName === 'TranscriptRecordClasses.TranscriptRecordClass' && viewName==="gene") {
    qualified = nodeHasProperty('geneOrTranscript', "gene", node);
  }
  return qualified;
};

/**
 * Create a separate search specific subtree, based upon the question asked and tack it onto the start of top level array
 * of nodes in the ontology tree
 * @param question - question posited
 * @param categoryTree - the munged ontology tree
 * @param recordClassName - full name of the record class
 * @param viewName - the name of the view (not sure how that will fly if everything else is _default
 */
export function addSearchSpecificSubtree(question, categoryTree, recordClassName, viewName) {
  if(question.dynamicAttributes.length > 0 && (recordClassName != 'TranscriptRecordClasses.TranscriptRecordClass' ||
     (!question.properties.questionType || (question.properties.questionType.indexOf('transcript') > -1 && viewName==="transcript")))) {
    let subtree = createNode("search_specific_subtree","Search Specific",
                             "Information about the records returned that is specific to the search you ran, and the parameters you specified");
    question.dynamicAttributes.forEach(attribute => {
      let node = createNode(attribute.name, attribute.displayName, attribute.help);
      subtree.children.push(node);
    });
    categoryTree.children.unshift(subtree);
  }
}

/**
 * Returns a JSON object representing a simplified category tree node that will be properly interpreted
 * by the checkboxTreeController
 * @param id - name or id of the node
 * @param displayName - name to be displayed
 * @param description - tooltip
 * @returns {{properties: {targetType: string[], name: *[]}, wdkReference: {displayName: *, help: *}, children: Array}}
 */
function createNode(id, displayName, description) {
  return {
    "properties" : {
      "targetType" : ["attribute"],
      "name" : [id]
    },
    "wdkReference" : {
      "displayName" : displayName,
      "help" : description
    },
    "children":[]
  }
}

/**
 * Returns whether the passed node 'matches' the passed node's display name
 * or description.
 * 
 * @param node node to test
 * @param searchText search text to match against
 * @returns true if node 'matches' the passed search text
 */
export function nodeSearchPredicate(node, searchText) {
  let nodeContents = getAggregateSearchText([ getDisplayName(node), getDescription(node) ]);
  return (nodeContents.indexOf(searchText.toLowerCase()) !== -1);
}

export function getAggregateSearchText(textStrings) {
  let searchText = '';
  textStrings.forEach(str => {
    if (str != null) searchText += str.toLowerCase() + ' ';
  });
  return searchText;
}
