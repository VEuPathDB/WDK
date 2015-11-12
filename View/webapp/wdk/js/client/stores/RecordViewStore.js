import { ReduceStore } from 'flux/utils';
import memoize from 'lodash/function/memoize';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';
import { reduce } from '../utils/Categories';

let {
  LOADING,
  RECORD_UPDATED,
  CATEGORY_COLLAPSED_TOGGLED,
  TABLE_COLLAPSED_TOGGLED
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
      case LOADING:
        return Object.assign({}, state, {
          isLoading: true
        });

      case RECORD_UPDATED: {
        let { record, recordClass, questions, recordClasses } = payload;

        let collapsedCategories = state.recordClass === recordClass
          ? state.collapsedCategories : recordClass.collapsedCategories || [];

        let collapsedTables = state.recordClass === recordClass
          ? state.collapsedTables : recordClass.collapsedTables || [];

        let categoryWordsMap = reduce(recordClass.attributeCategories, function(map, category) {
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

          words.push(category.displayName, category.description);

          return map.set(category, words.join('\0').toLowerCase());
        }, new Map());

        return Object.assign({}, state, {
          record: record,
          recordClass: recordClass,
          questions: questions,
          recordClasses: recordClasses,
          collapsedCategories,
          collapsedTables,
          isLoading: false,
          categoryWordsMap
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

      default:
        return state;
    }
  }
}

function updateList(item, add, list = []) {
  return add ? list.concat(item) : list.filter(x => x !== item);
}
