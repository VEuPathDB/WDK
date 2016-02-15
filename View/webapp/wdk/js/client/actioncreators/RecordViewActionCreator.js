import ActionCreator from '../utils/ActionCreator';
import UserActionCreator from './UserActionCreator';
import {latest} from '../utils/PromiseUtils';
import {
  getTree,
  nodeHasProperty,
  getPropertyValue
} from '../utils/OntologyUtils';
import {
  preorderSeq
} from '../utils/TreeUtils';

let actionTypes = {
  SET_ACTIVE_RECORD: 'record/set-active-record',
  SET_ACTIVE_RECORD_LOADING: 'record/set-active-record-loading',
  SET_ERROR: 'record/set-error',
  SHOW_CATEGORY: 'record/show-category',
  HIDE_CATEGORY: 'record/hide-category',
  SHOW_TABLE: 'record/show-table',
  HIDE_TABLE: 'record/hide-table',
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

export default class RecordViewActionCreator extends ActionCreator {

  constructor(...args) {
    super(...args);
    this._latestFetchRecordDetails = latest(this._fetchRecordDetails.bind(this));
    this._userActionCreator = new UserActionCreator(...args);
  }

  /**
   * @param {string} recordClassName
   * @param {Object} spec
   * @param {Object} spec.primaryKey
   * @param {Array<string>}  spec.attributes
   * @param {Array<string>}  spec.tables
   */
  fetchRecordDetails(recordClassName, primaryKeyValues) {
    this._dispatch({ type: actionTypes.SET_ACTIVE_RECORD_LOADING });

    let details$ = this._latestFetchRecordDetails(recordClassName, primaryKeyValues);
    let basketAction$ = details$.then(details => this._userActionCreator.loadBasketStatus(details.recordClass.name, details.record.id));

    return Promise.all([ details$, basketAction$ ]).then(([ details ]) => {
      return this._dispatch({
        type: actionTypes.SET_ACTIVE_RECORD,
        payload: details
      });
    }, this._errorHandler(actionTypes.SET_ERROR));
  }

  toggleCategoryCollapsed(recordClassName, categoryName, isCollapsed) {
    return this._dispatch({
      type: isCollapsed ? actionTypes.HIDE_CATEGORY : actionTypes.SHOW_CATEGORY,
      payload: {
        recordClass: recordClassName,
        name: categoryName
      }
    });
  }

  toggleTableCollapsed(recordClassName, tableName, isCollapsed) {
    return this._dispatch({
      type: isCollapsed ? actionTypes.HIDE_TABLE : actionTypes.SHOW_TABLE,
      payload: {
        recordClass: recordClassName,
        name: tableName
      }
    });
  }

  updateNavigationQuery(query) {
    return this._dispatch({
      type: actionTypes.UPDATE_NAVIGATION_QUERY,
      payload: { query }
    });
  }

  _fetchRecordDetails(recordClassUrlSegment, primaryKeyValues) {
    let questions$ = this._service.getQuestions();
    let recordClasses$ = this._service.getRecordClasses();
    let recordClass$ = this._service.findRecordClass(r => r.urlSegment === recordClassUrlSegment);
    let categoryTree$ = Promise.all([ recordClass$, this._service.getOntology('Categories') ])
      .then(([ recordClass, ontology ]) => getTree(ontology, isLeafFor(recordClass.name)));
    let record$ = Promise.all([ recordClass$, categoryTree$ ]).then(([ recordClass, categoryTree ]) => {
      let attributes = getAttributes(categoryTree).map(getNodeName);
      let tables = getTables(categoryTree).map(getNodeName);
      let primaryKey = recordClass.primaryKeyColumnRefs
        .map((ref, index) => ({ name: ref, value: primaryKeyValues[index] }));
      let options = { attributes, tables };
      return this._service.getRecord(recordClass.name, primaryKey, options)
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

}

// Action types
RecordViewActionCreator.actionTypes = actionTypes;
