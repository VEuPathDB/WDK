import {chunk, partial} from 'lodash';
import {getTree, nodeHasProperty, getPropertyValue} from '../utils/OntologyUtils';
import {filterNodes} from '../utils/TreeUtils';
import {seq} from '../utils/PromiseUtils';
import {loadBasketStatus, loadFavoritesStatus} from './UserActionCreators';

// TODO Change SHOW/HIDE_{feature} and EXPAND/COLLAPSE_{feature} to
// {feature}_VISIBILITY_CHANGED
export let actionTypes = {
  ACTIVE_RECORD_RECEIVED: 'record/active-record-received',
  ACTIVE_RECORD_UPDATED: 'record/active-record-updated',
  ACTIVE_RECORD_LOADING: 'record/active-record-loading',
  ERROR_RECEIVED: 'record/error-received',
  SECTION_VISIBILITY_CHANGED: 'record/section-visibility-changed',
  ALL_FIELD_VISIBILITY_CHANGED: 'record/all-field-visibility-changed',
  NAVIGATION_VISIBILITY_CHANGED: 'record/navigation-visibility-changed',
  NAVIGATION_SUBCATEGORY_VISBILITY_CHANGED: 'record/navigation-subcategory-visbility-changed',
  NAVIGATION_QUERY_CHANGED: 'record/navigation-query-changed'
};

let isInternalNode = partial(nodeHasProperty, 'scope', 'record-internal');
let isNotInternalNode = partial(nodeHasProperty, 'scope', 'record');
let isAttributeNode = partial(nodeHasProperty, 'targetType', 'attribute');
let isTableNode = partial(nodeHasProperty, 'targetType', 'table');
let getAttributes = partial(filterNodes, isAttributeNode);
let getTables = partial(filterNodes, isTableNode);
let getNodeName = partial(getPropertyValue, 'name');

export function loadRecordData(recordClass, primaryKeyValues) {
  return function run(dispatch, {wdkService}) {
    dispatch(setActiveRecord(recordClass, primaryKeyValues))
    .then(action => {
      let { record, recordClass } = action.payload;
      if (recordClass.useBasket) {
        dispatch(loadBasketStatus(record));
        dispatch(loadFavoritesStatus(record));
      }
    });
  };
}

/**
 * Fetches the new record from the service and dispatches related
 * actions so that the store can update.
 *
 * @param {string} recordClassName
 * @param {Array<string>} primaryKeyValues
 */
function setActiveRecord(recordClassName, primaryKeyValues) {
  return function run(dispatch, {wdkService}) {
    dispatch({ type: actionTypes.ACTIVE_RECORD_LOADING });
    // Fetch the record base and tables in parallel.
    return Promise.all([
      wdkService.findRecordClass(r => r.urlSegment === recordClassName),
      getPrimaryKey(wdkService, recordClassName, primaryKeyValues),
      getCategoryTree(wdkService, recordClassName)
    ]).then(([recordClass, primaryKey, fullCategoryTree]) => {
      // Set up promises for actions
      let baseAction$ = getRecordBase(wdkService, recordClass, primaryKey, fullCategoryTree);
      // load all subsequent tables in a single request. we were doing it in batches of 4, but that hurts rendering!
      let tableActions = getRecordTables(wdkService, recordClass, primaryKey, fullCategoryTree, 0);
      // Helper to handle errors
      let dispatchError = error => dispatch({
        type: actionTypes.ERROR_RECEIVED,
        payload: { error }
      });
      // Calls dispatch on the array of promises in the provided order
      // even if they resolve out of order.
      seq([baseAction$].concat(tableActions), dispatch).catch(dispatchError);

      return baseAction$;
    });
  }
}

/** Update a section's collapsed status */
export function updateSectionVisibility(sectionName, isVisible) {
  return {
    type: actionTypes.SECTION_VISIBILITY_CHANGED,
    payload: { name: sectionName, isVisible }
  };
}

/** Change the visibility for all record fields (attributes and tables) */
export function updateAllFieldVisibility(isVisible) {
  return {
    type: actionTypes.ALL_FIELD_VISIBILITY_CHANGED,
    payload: { isVisible }
  }
}

/** Update navigation section search term */
export function updateNavigationQuery(query) {
  return {
    type: actionTypes.NAVIGATION_QUERY_CHANGED,
    payload: { query }
  };
}

/** Change the visbility of the navigation panel */
export function updateNavigationVisibility(isVisible) {
  return {
    type: actionTypes.NAVIGATION_VISIBILITY_CHANGED,
    payload: { isVisible }
  }
}

/** Change the visibility of subcategories in the navigation section */
export function updateNavigationSubcategoryVisibility(isVisible) {
  return {
    type: actionTypes.NAVIGATION_SUBCATEGORY_VISBILITY_CHANGED,
    payload: { isVisible }
  }
}


// helpers
// -------

/**
 * Get the base record request payload object
 * @param wdkService
 * @param recordClassUrlSegment
 * @param primaryKeyValues
 * @returns Promise<PrimaryKey>
 */
function getPrimaryKey(wdkService, recordClassUrlSegment, primaryKeyValues) {
  return wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment)
    .then(recordClass => {
      return recordClass.primaryKeyColumnRefs
        .map((ref, index) => ({ name: ref, value: primaryKeyValues[index] }));
    })
}

/** Get the category tree for the given record class */
function getCategoryTree(wdkService, recordClassUrlSegment) {
  return Promise.all([
    wdkService.getOntology(),
    wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment)
  ]).then(([ontology, recordClass]) => {
    return getTree(ontology, isLeafFor(recordClass.name));
  });
}

/** Creates a leaf predicate for the given recordClass */
function isLeafFor(recordClassName) {
  return function isLeaf(node) {
    return (
      (isAttributeNode(node) || isTableNode(node))
      && nodeHasProperty('recordClassName', recordClassName, node)
      && (isInternalNode(node) || isNotInternalNode(node))
    );
  }
}

/** Load all attributes and internal tables */
function getRecordBase(wdkService, recordClass, primaryKey, fullCategoryTree) {
  let options = {
    attributes: getAttributes(fullCategoryTree).map(getNodeName),
    tables: getTables(fullCategoryTree).filter(isInternalNode).map(getNodeName)
  };
  return wdkService.getRecord(recordClass.name, primaryKey, options)
  .then(record => {
    let categoryTree = getTree({tree: fullCategoryTree}, isNotInternalNode);
    return {
      type: actionTypes.ACTIVE_RECORD_RECEIVED,
      payload: { record, recordClass, categoryTree }
    };
  })
}

/** Load non-internal tables, optionally in batches */
function getRecordTables(wdkService, recordClass, primaryKey, fullCategoryTree, batchSize) {
  let tables = getTables(fullCategoryTree).filter(isNotInternalNode).map(getNodeName);
  return chunk(tables, batchSize || tables.length)
  .map(tables => {
    let options = { tables };
    return wdkService.getRecord(recordClass.name, primaryKey, options)
    .then(record => {
      return {
        type: actionTypes.ACTIVE_RECORD_UPDATED,
        payload: { record }
      };
    })
  });
}
