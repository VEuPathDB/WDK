import { ReduceStore } from 'flux/utils';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';

let {
  SET_ACTIVE_RECORD,
  SET_ACTIVE_RECORD_LOADING,
  SET_ERROR,
  SHOW_CATEGORY,
  HIDE_CATEGORY,
  SHOW_TABLE,
  HIDE_TABLE,
  UPDATE_NAVIGATION_QUERY
} = RecordViewActionCreator.actionTypes;

export default class RecordViewStore extends ReduceStore {

  getInitialState() {
    return {
      record: undefined,
      collapsedCategories: undefined,
      collapsedTables: undefined,
      navigationQuery: '',
      visibleNavigationCategories: undefined
    };
  }

  reduce(state, { type, payload }) {
    switch (type) {
      case SET_ERROR:
        return Object.assign({}, this.getInitialState(), {
          isLoading: false,
          error: payload.error
        });

      case SET_ACTIVE_RECORD_LOADING:
        return Object.assign({}, state, {
          isLoading: true,
          error: null
        });

      case SET_ACTIVE_RECORD: {
        let { record, recordClass, questions, recordClasses, categoryTree } = payload;

        let collapsedCategories = state.recordClass === recordClass
          ? state.collapsedCategories : recordClass.collapsedCategories || [];

        let collapsedTables = state.recordClass === recordClass
          ? state.collapsedTables : recordClass.collapsedTables || [];

        return Object.assign({}, state, {
          record: record,
          recordClass: recordClass,
          questions: questions,
          recordClasses: recordClasses,
          collapsedCategories,
          collapsedTables,
          isLoading: false,
          categoryTree
        });
      }

      case SHOW_CATEGORY: {
        let collapsedCategories = updateList(
          payload.name,
          false,
          state.collapsedCategories
        );
        return Object.assign({}, state, { collapsedCategories });
      }

      case HIDE_CATEGORY: {
        let collapsedCategories = updateList(
          payload.name,
          true,
          state.collapsedCategories
        );
        return Object.assign({}, state, { collapsedCategories });
      }

      case SHOW_TABLE: {
        let collapsedTables = updateList(
          payload.name,
          false,
          state.collapsedTables
        );
        return Object.assign({}, state, { collapsedTables });
      }

      case HIDE_TABLE: {
        let collapsedTables = updateList(
          payload.name,
          true,
          state.collapsedTables
        );
        return Object.assign({}, state, { collapsedTables });
      }

      default:
        return state;
    }
  }
}

let updateList = (item, add, list = []) =>
  add ? list.concat(item) : list.filter(x => x !== item)
