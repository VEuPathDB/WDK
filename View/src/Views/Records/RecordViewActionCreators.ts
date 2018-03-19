import { chunk, partial, uniqueId } from 'lodash';
import { getTree, nodeHasProperty, getPropertyValue } from 'Utils/OntologyUtils';
import { filterNodes } from 'Utils/TreeUtils';
import { seq } from 'Utils/PromiseUtils';
import {
  loadBasketStatus,
  loadFavoritesStatus,
  BasketStatusLoadingAction,
  BasketStatusErrorAction,
  BasketStatusReceivedAction,
  FavoritesStatusErrorAction,
  FavoritesStatusReceivedAction,
  FavoritesStatusLoadingAction
} from 'Core/ActionCreators/UserActionCreators';
import { PrimaryKey, RecordInstance, RecordClass } from 'Utils/WdkModel';
import { ActionThunk, DispatchAction, emptyAction, EmptyAction } from 'Utils/ActionCreatorUtils';
import { Action } from 'Core/State/Dispatcher';
import WdkService from 'Utils/WdkService';
import { CategoryTreeNode } from "Utils/CategoryUtils";
import { ServiceError } from "Utils/WdkService";

type BasketAction = BasketStatusLoadingAction | BasketStatusErrorAction | BasketStatusReceivedAction;
type FavoriteAction = FavoritesStatusLoadingAction | FavoritesStatusReceivedAction | FavoritesStatusErrorAction;

export type RecordReceivedAction = {
  type: 'record-view/active-record-received',
  id: string,
  payload: {
    record: RecordInstance,
    recordClass: RecordClass,
    categoryTree: CategoryTreeNode
  },
}

export type RecordUpdatedAction = {
  type: 'record-view/active-record-updated',
  id: string,
  payload: {
    record: RecordInstance
  },
}

export type RecordLoadingAction = {
  type: 'record-view/active-record-loading',
  id: string,
  payload: {
    recordClassName: string,
    primaryKeyValues: string[]
  },
}

export type RecordErrorAction = {
  type: 'record-view/error-received',
  id: string
  payload: { error: ServiceError },
}

export type SectionVisibilityAction = {
  type: 'record-view/section-visibility-changed',
  payload: {
    name: string,
    isVisible: boolean
  }
}

export type AllFieldVisibilityAction = {
  type: 'record-view/all-field-visibility-changed',
  payload: {
    isVisible: boolean
  }
}

export type NavigationVisibilityAction = {
  type: 'record-view/navigation-visibility-changed',
  payload: {
    isVisible: boolean
  }
}

export type CategoryExpansionAction = {
  type: 'record-view/navigation-category-expansion-changed',
  payload: {
    expandedCategories: string[]
  }
}

export type NavigationQueryAction = {
  type: 'record-view/navigation-query-changed',
  payload: {
    query: string
  }
}

let isInternalNode = partial(nodeHasProperty, 'scope', 'record-internal');
let isNotInternalNode = partial(nodeHasProperty, 'scope', 'record');
let isAttributeNode = partial(nodeHasProperty, 'targetType', 'attribute');
let isTableNode = partial(nodeHasProperty, 'targetType', 'table');
let getAttributes = partial(filterNodes, isAttributeNode);
let getTables = partial(filterNodes, isTableNode);
let getNodeName = partial(getPropertyValue, 'name');

type LoadRecordAction = RecordLoadingAction
  | RecordErrorAction
  | RecordReceivedAction
  | RecordUpdatedAction

type UserAction = BasketAction | FavoriteAction

/** Fetch page data from services */
export function loadRecordData(recordClass: string, primaryKeyValues: string[]): ActionThunk<LoadRecordAction | UserAction | EmptyAction> {
  return function run({ wdkService }) {
    return setActiveRecord(recordClass, primaryKeyValues);
  };
}

const makeWithId = <T extends Action & { id: string }>(
  id = uniqueId('groupid')
) => (action: Action) => {
  return Object.assign(action, { id }) as T;
}

/**
 * Fetches the new record from the service and dispatches related
 * actions so that the store can update.
 *
 * @param {string} recordClassName
 * @param {Array<string>} primaryKeyValues
 */
function setActiveRecord(recordClassName: string, primaryKeyValues: string[]): ActionThunk<LoadRecordAction|UserAction|EmptyAction> {
  return ({ wdkService }) => {
    const withId = makeWithId<LoadRecordAction>();
    // Helper to handle errors
    const makeErrorAction = (error: Error) => withId({
      type: 'record-view/error-received',
      payload: { error }
    });

    return [
      withId({
        type: 'record-view/active-record-loading',
        payload: { recordClassName, primaryKeyValues }
      }),
      // Fetch the record base and tables in parallel.
      Promise.all([
        wdkService.findRecordClass(r => r.urlSegment === recordClassName),
        getPrimaryKey(wdkService, recordClassName, primaryKeyValues),
        getCategoryTree(wdkService, recordClassName)
      ]).then(
        ([recordClass, primaryKey, fullCategoryTree]) => {
          // Set up promises for actions
          let baseAction$ = getRecordBase(wdkService, recordClass, primaryKey, fullCategoryTree);
          // load all subsequent tables in a single request. we were doing it in batches of 4, but that hurts rendering!
          let tableActions = getRecordTables(wdkService, recordClass, primaryKey, fullCategoryTree, 0);

          return [
            baseAction$.then(withId, makeErrorAction),
            tableActions.map(action$ => action$.then(withId, makeErrorAction)),
            recordClass.useBasket ? baseAction$.then(r => loadBasketStatus(r.payload.record)) : emptyAction,
            baseAction$.then(r => loadFavoritesStatus(r.payload.record))
          ];
        },
        makeErrorAction
      )
    ];
  }
}

/** Update a section's collapsed status */
export function updateSectionVisibility(sectionName: string, isVisible: boolean): SectionVisibilityAction {
  return {
    type: 'record-view/section-visibility-changed',
    payload: { name: sectionName, isVisible }
  };
}

/** Change the visibility for all record fields (attributes and tables) */
export function updateAllFieldVisibility(isVisible: boolean): AllFieldVisibilityAction {
  return {
    type: 'record-view/all-field-visibility-changed',
    payload: { isVisible }
  }
}

/** Update navigation section search term */
export function updateNavigationQuery(query: string): NavigationQueryAction {
  return {
    type: 'record-view/navigation-query-changed',
    payload: { query }
  };
}

/** Change the visibility of the navigation panel */
export function updateNavigationVisibility(isVisible: boolean): NavigationVisibilityAction {
  return {
    type: 'record-view/navigation-visibility-changed',
    payload: { isVisible }
  }
}

/** Change the visibility of subcategories in the navigation section */
export function updateNavigationCategoryExpansion(expandedCategories: string[]): CategoryExpansionAction {
  return {
    type: 'record-view/navigation-category-expansion-changed',
    payload: { expandedCategories }
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
function getPrimaryKey(wdkService: WdkService, recordClassUrlSegment: string, primaryKeyValues: string[]) {
  return wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment)
    .then(recordClass => {
      if (recordClass == null)
        throw new Error("Could not find a record class identified by `" + recordClassUrlSegment + "`.");

      return recordClass.primaryKeyColumnRefs
        .map((ref, index) => ({ name: ref, value: primaryKeyValues[index] }));
    })
}

/** Get the category tree for the given record class */
function getCategoryTree(wdkService: WdkService, recordClassUrlSegment: string) {
  return Promise.all([
    wdkService.getOntology(),
    wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment)
  ]).then(([ontology, recordClass]) => {
    return getTree(ontology, isLeafFor(recordClass.name));
  });
}

/** Creates a leaf predicate for the given recordClass */
function isLeafFor(recordClassName: string) {
  return function isLeaf(node: CategoryTreeNode) {
    return (
      (isAttributeNode(node) || isTableNode(node))
      && nodeHasProperty('recordClassName', recordClassName, node)
      && (isInternalNode(node) || isNotInternalNode(node))
    );
  }
}

/** Load all attributes and internal tables */
function getRecordBase(wdkService: WdkService, recordClass: RecordClass, primaryKey: PrimaryKey, fullCategoryTree: CategoryTreeNode): Promise<RecordReceivedAction> {
  let options = {
    attributes: getAttributes(fullCategoryTree).map(getNodeName),
    tables: getTables(fullCategoryTree).filter(isInternalNode).map(getNodeName)
  };
  return wdkService.getRecord(recordClass.name, primaryKey, options)
    .then(record => {
      let categoryTree = getTree({ name: '__', tree: fullCategoryTree }, isNotInternalNode);
      return {
        type: 'record-view/active-record-received',
        payload: { record, recordClass, categoryTree }
      } as RecordReceivedAction;
    })
}

/** Load non-internal tables, optionally in batches */
function getRecordTables(wdkService: WdkService, recordClass: RecordClass, primaryKey: PrimaryKey, fullCategoryTree: CategoryTreeNode, batchSize?: number): Promise<RecordUpdatedAction>[] {
  let tables: string[] = getTables(fullCategoryTree).filter(isNotInternalNode).map(getNodeName);
  return chunk(tables, batchSize || tables.length)
    .map(tables => {
      let options = { tables };
      return wdkService.getRecord(recordClass.name, primaryKey, options)
        .then(record => {
          return {
            type: 'record-view/active-record-updated',
            payload: { record }
          } as RecordUpdatedAction;
        })
    });
}
