import ActionCreator from '../utils/ActionCreator';
import UserActionCreator from './UserActionCreator';
import {latest} from '../utils/PromiseUtils';

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
    let record$ = recordClass$.then(recordClass => {
      let primaryKey = recordClass.primaryKeyColumnRefs
        .map((ref, index) => ({ name: ref, value: primaryKeyValues[index] }));
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let options = { attributes, tables };
      return this._service.getRecord(recordClass.name, primaryKey, options)
    });

    return Promise.all([ questions$, recordClasses$, record$, recordClass$ ])
    .then(([ questions, recordClasses, record, recordClass ]) =>
      ({ questions, recordClasses, recordClass, record }));
  }

}

// Action types
RecordViewActionCreator.actionTypes = actionTypes;
