import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/RecordViewActionCreator';

let {
  ACTIVE_RECORD_RECEIVED,
  ACTIVE_RECORD_LOADING,
  ERROR_RECEIVED,
  SHOW_SECTION,
  HIDE_SECTION,
  UPDATE_NAVIGATION_QUERY
} = actionTypes;

export default class RecordViewStore extends WdkStore {

  getInitialState() {
    return {
      record: undefined,
      recordClass: undefined,
      recordClasses: undefined,
      quesitons: undefined,
      collapsedSections: undefined,
      isLoading: undefined,
      isInBasket: undefined,
      isInFavorites: undefined,
      basketStatusLoading: undefined,
      favoriteStatusLoading: undefined,
      categoryTree: undefined
    };
  }

  reduce(state, { type, payload }) {
    switch (type) {
      case ERROR_RECEIVED:
        return Object.assign({}, this.getInitialState(), {
          isLoading: false,
          error: payload.error
        });

      case ACTIVE_RECORD_LOADING:
        return Object.assign({}, state, {
          isLoading: true,
          error: null
        });

      case ACTIVE_RECORD_RECEIVED: {
        let { record, recordClass, questions, recordClasses, categoryTree } = payload;

        return Object.assign({}, state, {
          record,
          recordClass,
          recordClasses,
          questions,
          collapsedSections: [],
          isLoading: false,
          categoryTree
        });
      }

      case SHOW_SECTION: {
        let collapsedSections = updateList(
          payload.name,
          false,
          state.collapsedSections
        );
        return Object.assign({}, state, { collapsedSections });
      }

      case HIDE_SECTION: {
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
