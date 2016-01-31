import ActionCreator from '../utils/ActionCreator';
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
  RECORD_UPDATED: 'record/updated',
  LOADING: 'record/loading',
  ERROR: 'record/error',
  CATEGORY_COLLAPSED_TOGGLED: 'record/category-toggled',
  TABLE_COLLAPSED_TOGGLED: 'record/table-toggled',
  UPDATE_NAVIGATION_QUERY: 'record/update-navigation-query'
};

let isLeafFor = recordClassName => node => {
  return (
    nodeHasProperty('targetType', 'attribute', node) || nodeHasProperty('targetType', 'table', node)
  ) && nodeHasProperty('recordClassName', recordClassName, node) && nodeHasProperty('scope', 'record', node);

}

let getAttributes = tree =>
  preorderSeq(tree).filter(node => nodeHasProperty('targetType', 'attribute', node)).toArray()

let getTables = tree =>
  preorderSeq(tree).filter(node => nodeHasProperty('targetType', 'table', node)).toArray()

let getNodeName = node => getPropertyValue('name', node);

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
  fetchRecordDetails(recordClassName, primaryKeyValues) {
    this._dispatch({ type: actionTypes.LOADING });

    this._getLatestRecord(recordClassName, primaryKeyValues).then(
      ({ record, recordClass, recordClasses, questions, categoryTree }) => {
        this._dispatch({
          type: actionTypes.RECORD_UPDATED,
          payload: { record, recordClass, recordClasses, questions, categoryTree }
        });
      },
      error => {
        this._dispatch({
          type: actionTypes.ERROR,
          payload: { error }
        });
        throw error;
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

  _getRecord(recordClassUrlSegment, primaryKeyValues) {
    return Promise.all([
      this._service.getQuestions(),
      this._service.getRecordClasses(),
      this._service.getOntology('Categories')
    ]).then(([
      questions,
      recordClasses,
      categoriesOntology
    ]) => {
      let recordClass = recordClasses.find(r => r.urlSegment == recordClassUrlSegment);
      let categoryTree = getTree(categoriesOntology, isLeafFor(recordClass.name));
      let attributes = getAttributes(categoryTree).map(getNodeName);
      let tables = getTables(categoryTree).map(getNodeName);
      let options = { attributes, tables };
      return this._service.getRecord(recordClass.name, primaryKeyValues, options).then(
        record => ({ record, recordClass, recordClasses, questions, categoryTree })
      );
    });
  }

}

// Action types
RecordViewActionCreator.actionTypes = actionTypes;
