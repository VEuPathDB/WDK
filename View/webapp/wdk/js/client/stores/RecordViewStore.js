import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/RecordViewActionCreator';

/** Store for record page */
export default class RecordViewStore extends WdkStore {

  getInitialState() {
    return {
      record: undefined,
      recordClass: undefined,
      collapsedSections: undefined,
      isLoading: undefined,
      categoryTree: undefined
    };
  }

  reduce(state, { type, payload }) {
    switch (type) {
      case actionTypes.ERROR_RECEIVED:
        return Object.assign({}, this.getInitialState(), {
          isLoading: false,
          error: payload.error
        });

      case actionTypes.ACTIVE_RECORD_LOADING:
        return Object.assign({}, state, {
          isLoading: true,
          error: null
        });

      case actionTypes.ACTIVE_RECORD_RECEIVED: {
        let { record, recordClass, categoryTree } = payload;

        return Object.assign({}, state, {
          record,
          recordClass,
          collapsedSections: [],
          isLoading: false,
          categoryTree
        });
      }

    case actionTypes.ACTIVE_RECORD_UPDATED: {
      let { record } = payload;
      return Object.assign({}, state, { record });
    }

      case actionTypes.SHOW_SECTION: {
        let collapsedSections = updateList(
          payload.name,
          false,
          state.collapsedSections
        );
        return Object.assign({}, state, { collapsedSections });
      }

      case actionTypes.HIDE_SECTION: {
        let collapsedSections = updateList(
          payload.name,
          true,
          state.collapsedSections
        );
        return Object.assign({}, state, { collapsedSections });
      }

      default:
        return state;
    }
  }
}

RecordViewStore.actionTypes = actionTypes;

let updateList = (item, add, list = []) =>
  add ? list.concat(item) : list.filter(x => x !== item)
