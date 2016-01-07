import { ReduceStore } from 'flux/utils';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';
import * as i from '../utils/Iterable';
import { postorder as postorderCategories } from '../utils/CategoryTreeIterators';

let {
  LOADING,
  ERROR,
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
      case ERROR:
        return Object.assign({}, this.getInitialState(), {
          isLoading: false,
          error: payload.error
        });

      case LOADING:
        return Object.assign({}, state, {
          isLoading: true,
          error: null
        });

      case RECORD_UPDATED: {
        let { record, recordClass, questions, recordClasses } = payload;

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

    if (category.subCategories != null) {
      for (let cat of map.keys()) {
        if (category.subCategories.indexOf(cat) > -1) {
          words.push(map.get(cat));
        }
      }
    }

    words.push(category.displayName, category.description);

    return map.set(category, words.join('\0').toLowerCase());
  }, new Map(), postorderCategories(recordClass.attributeCategories));
}
