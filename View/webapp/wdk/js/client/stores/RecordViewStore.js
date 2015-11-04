import {ReduceStore} from 'flux/utils';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';

let {
  LOADING,
  RECORD_UPDATED,
  CATEGORY_COLLAPSED_TOGGLED,
  TABLE_COLLAPSED_TOGGLED,
  UPDATE_NAVIGATION_QUERY
} = RecordViewActionCreator.actionTypes;

export default class RecordViewStore extends ReduceStore {

  getInitialState() {
    return {
      record: undefined,
      collapsedCategories: undefined,
      collapsedTables: undefined,
      navigationQuery: ''
    };
  }

  reduce(state, { type, payload }) {
    switch (type) {
      case LOADING:
        return Object.assign({}, state, {
          isLoading: true
        });

      case RECORD_UPDATED: {
        let collapsedCategories = state.recordClass === payload.recordClass
          ? state.collapsedCategories : payload.recordClass.collapsedCategories || [];

        let collapsedTables = state.recordClass === payload.recordClass
          ? state.collapsedTables : payload.recordClass.collapsedTables || [];

        return Object.assign({}, state, {
          record: payload.record,
          recordClass: payload.recordClass,
          questions: payload.questions,
          recordClasses: payload.recordClasses,
          collapsedCategories,
          collapsedTables,
          isLoading: false
        });
      }

      case CATEGORY_COLLAPSED_TOGGLED: {
        let collapsedCategories = updateList(
          payload.name,
          payload.isCollapsed,
          state.collapsedCategories
        );
        return Object.assign({}, state, { collapsedCategories });
      }

      case TABLE_COLLAPSED_TOGGLED: {
        let collapsedTables = updateList(
          payload.name,
          payload.isCollapsed,
          state.collapsedTables
        );
        return Object.assign({}, state, { collapsedTables });
      }

      case UPDATE_NAVIGATION_QUERY:
        return Object.assign({}, state, { navigationQuery: payload.query });

      default:
        return state;
    }
  }
}

function updateList(item, add, list = []) {
  return add ? list.concat(item) : list.filter(x => x !== item);
}
