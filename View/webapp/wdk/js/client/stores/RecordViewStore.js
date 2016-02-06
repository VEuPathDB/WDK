import { ReduceStore } from 'flux/utils';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';
import * as i from '../utils/IterableUtils';
import { postorder as postorderCategories } from '../utils/CategoryTreeIterators';

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
        let { record, recordClass, questions, recordClasses, user, inBasket } = payload;

        let collapsedCategories = state.recordClass === recordClass
          ? state.collapsedCategories : recordClass.collapsedCategories || [];

        let collapsedTables = state.recordClass === recordClass
          ? state.collapsedTables : recordClass.collapsedTables || [];

        let categoryWordsMap = makeCategoryWordsMap(recordClass);;

        return Object.assign({}, state, {
          record: record,
          recordClass: recordClass,
          questions: questions,
          recordClasses: recordClasses,
          collapsedCategories,
          collapsedTables,
          isLoading: false,
          categoryWordsMap,
          user,
          inBasket
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

function updateList(item, add, list = []) {
  return add ? list.concat(item) : list.filter(x => x !== item);
}

function makeCategoryWordsMap(recordClass) {
  return i.reduce((map, category) => {
    let words = [];

    for (let attribute of recordClass.attributes) {
      if (attribute.category == category.name) {
        words.push(attribute.displayName, attribute.description);
      }
    }

    for (let table of recordClass.tables) {
      if (table.category == category.name) {
        words.push(table.displayName, table.description);
      }
    }

    if (category.categories != null) {
      for (let cat of map.keys()) {
        if (category.categories.indexOf(cat) > -1) {
          words.push(map.get(cat));
        }
      }
    }

    words.push(category.displayName, category.description);

    return map.set(category, words.join('\0').toLowerCase());
  }, new Map(), postorderCategories(recordClass.categories));
}
