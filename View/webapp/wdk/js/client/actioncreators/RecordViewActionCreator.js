import ActionCreator from '../utils/ActionCreator';
import {latest} from '../utils/PromiseUtils';

let actionTypes = {
  RECORD_UPDATED: 'record/updated',
  LOADING: 'record/loading',
  ERROR: 'record/error',
  CATEGORY_COLLAPSED_TOGGLED: 'record/category-toggled',
  TABLE_COLLAPSED_TOGGLED: 'record/table-toggled',
  UPDATE_NAVIGATION_QUERY: 'record/update-navigation-query'
};

export default class RecordViewActionCreator extends ActionCreator {

  constructor(...args) {
    super(...args);
    this._getLatestRecord = latest(this._getRecord.bind(this));
  }

  /**
   * @param {string} recordClassName
   * @param {Object} spec
   * @param {Object} spec.primaryKey
   * @param {Array<string>}  spec.attributes
   * @param {Array<string>}  spec.tables
   */
  fetchRecordDetails(recordClassName, primaryKey) {
    this._dispatch({ type: actionTypes.LOADING });

    this._getLatestRecord(recordClassName, primaryKey).then(
      ({ record, recordClass, recordClasses, questions }) => {
        this._dispatch({
          type: actionTypes.RECORD_UPDATED,
          payload: { record, recordClass, recordClasses, questions }
        });
      },
      error => {
        this._dispatch({
          type: actionTypes.ERROR,
          payload: { error }
        });
        console.assert(false, error);
      }
    );
  }

  toggleCategoryCollapsed(recordClassName, categoryName, isCollapsed) {
    this._dispatch({
      type: actionTypes.CATEGORY_COLLAPSED_TOGGLED,
      payload: {
        recordClass: recordClassName,
        name: categoryName,
        isCollapsed
      }
    });
  }

  toggleTableCollapsed(recordClassName, tableName, isCollapsed) {
    this._dispatch({
      type: actionTypes.TABLE_COLLAPSED_TOGGLED,
      payload: {
        recordClass: recordClassName,
        name: tableName,
        isCollapsed
      }
    });
  }

  updateNavigationQuery(query) {
    this._dispatch({
      type: actionTypes.UPDATE_NAVIGATION_QUERY,
      payload: { query }
    });
  }

  _getRecord(recordClassName, primaryKey) {
    let questionsPromise = this._service.getQuestions();
    let recordClassesPromise = this._service.getRecordClasses();

    return Promise.all([ questionsPromise, recordClassesPromise ])
    .then(([ questions, recordClasses ]) => {
      return this._service.getRecordClass(recordClassName)
      .then(recordClass => {
        let attributes = recordClass.attributes.map(a => a.name);
        let tables = recordClass.tables.map(t => t.name);
        let options = { attributes, tables };
        return this._service.getRecord(recordClassName, primaryKey, options).then(
          record => ({ record, recordClass, recordClasses, questions })
        );
      })
    });
  }

}

// Action types
RecordViewActionCreator.actionTypes = actionTypes;
