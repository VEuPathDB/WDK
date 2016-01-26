import { ReduceStore } from 'flux/utils';
import RecordViewActionCreator from '../actioncreators/RecordViewActionCreator';
import { postorderSeq } from '../utils/TreeUtils';
import { nodeHasProperty, getPropertyValues } from '../utils/OntologyUtils';

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
        let { record, recordClass, questions, recordClasses, categoryTree } = payload;

        let collapsedCategories = state.recordClass === recordClass
          ? state.collapsedCategories : recordClass.collapsedCategories || [];

        let collapsedTables = state.recordClass === recordClass
          ? state.collapsedTables : recordClass.collapsedTables || [];

        let categoryWordsMap = makeCategoryWordsMap(recordClass, categoryTree);;

        return Object.assign({}, state, {
          record: record,
          recordClass: recordClass,
          questions: questions,
          recordClasses: recordClasses,
          collapsedCategories,
          collapsedTables,
          isLoading: false,
          categoryWordsMap,
          categoryTree
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

let updateList = (item, add, list = []) =>
  add ? list.concat(item) : list.filter(x => x !== item)

let makeCategoryWordsMap = (recordClass, root) =>
  postorderSeq(root).reduce((map, node) => {
    let words = [];

    // add current node's displayName and description
    words.push(
      ...getPropertyValues('hasDefinition', node),
      ...getPropertyValues('hasExactSynonym', node),
      ...getPropertyValues('hasNarrowSynonym', node)
    );

    // add displayName and desription of attribute
    if (nodeHasProperty('targetType', 'attribute', node)) {
      let attribute = recordClass.attributes.find(a => a.name === getPropertyValues('name', node)[0]);
      words.push(attribute.displayName, attribute.description);
    }

    // add displayName and desription of table
    if (nodeHasProperty('targetType', 'table', node)) {
      let table = recordClass.tables.find(a => a.name === getPropertyValues('name', node)[0]);
      words.push(table.displayName, table.description);
    }

    // add words from any children
    for (let child of node.children) {
      words.push(map.get(child.properties));
    }

    return map.set(node.properties, words.join('\0').toLowerCase());
  }, new Map)
