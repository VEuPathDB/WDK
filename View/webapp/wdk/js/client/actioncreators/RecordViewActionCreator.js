import {getTree, nodeHasProperty, getPropertyValue} from '../utils/OntologyUtils';
import {preorderSeq} from '../utils/TreeUtils';

export let actionTypes = {
  ACTIVE_RECORD_RECEIVED: 'record/active-record-received',
  ACTIVE_RECORD_LOADING: 'record/active-record-loading',
  ERROR_RECEIVED: 'record/error-received',
  SHOW_SECTION: 'record/show-section',
  HIDE_SECTION: 'record/hide-section',
  UPDATE_NAVIGATION_QUERY: 'record/update-navigation-query'
};

let isLeafFor = recordClassName => node => {
  return (
    (nodeHasProperty('targetType', 'attribute', node) || nodeHasProperty('targetType', 'table', node))
    && nodeHasProperty('recordClassName', recordClassName, node)
    && (nodeHasProperty('scope', 'record', node) || nodeHasProperty('scope', 'record-internal', node))
    )
}

let isNotInternal = node => {
  return nodeHasProperty('scope', 'record', node);
}

let getAttributes = tree =>
  preorderSeq(tree).filter(node => nodeHasProperty('targetType', 'attribute', node)).toArray()

let getTables = tree =>
  preorderSeq(tree).filter(node => nodeHasProperty('targetType', 'table', node)).toArray()

let getNodeName = node => getPropertyValue('name', node);

/**
 * @param {string} recordClassName
 * @param {Array} primaryKeyValues
 */
export function setActiveRecord(recordClassName, primaryKeyValues) {
  return function run(dispatch, { wdkService }) {
    let details$ = fetchRecordDetails(wdkService, recordClassName, primaryKeyValues);
    dispatch({ type: actionTypes.ACTIVE_RECORD_LOADING });
    return details$.then(details => {
      return dispatch({
        type: actionTypes.ACTIVE_RECORD_RECEIVED,
        payload: details
      });
    }, error => {
      dispatch({
        type: actionTypes.ERROR_RECEIVED,
        payload: { error }
      });
      throw error;
    });
  }
}

/** Update a section's collapsed status */
export function updateSectionCollapsed(sectionName, isCollapsed) {
  return {
    type: isCollapsed ? actionTypes.HIDE_SECTION : actionTypes.SHOW_SECTION,
    payload: { name: sectionName }
  };
}

/** Update navigation section search term -- currently unused */
export function updateNavigationQuery(query) {
  return {
    type: actionTypes.UPDATE_NAVIGATION_QUERY,
    payload: { query }
  };
}

/**
 * Helper to fetch record details from Wdk Service.
 * Returns a Promise that resolves to an Object of data needed by View store.
 */
function fetchRecordDetails(wdkService, recordClassUrlSegment, primaryKeyValues) {
  let questions$ = wdkService.getQuestions();
  let recordClasses$ = wdkService.getRecordClasses();
  let recordClass$ = wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment);
  let categoryTree$ = Promise.all([ recordClass$, wdkService.getOntology() ])
    .then(([ recordClass, ontology ]) => getTree(ontology, isLeafFor(recordClass.name)));
  let record$ = Promise.all([ recordClass$, categoryTree$ ]).then(([ recordClass, categoryTree ]) => {
    let attributes = getAttributes(categoryTree).map(getNodeName);
    let tables = getTables(categoryTree).map(getNodeName);
    let primaryKey = recordClass.primaryKeyColumnRefs
      .map((ref, index) => ({ name: ref, value: primaryKeyValues[index] }));
    let options = { attributes, tables };
    return wdkService.getRecord(recordClass.name, primaryKey, options)
  });

  return Promise.all([ record$, categoryTree$, recordClass$, recordClasses$, questions$ ])
  .then(([ record, categoryTree, recordClass, recordClasses, questions ]) => {
     let newTree = getTree({tree: categoryTree}, isNotInternal);
     return {
        record, categoryTree: newTree, recordClass, recordClasses, questions
      }
    }
  );
}
