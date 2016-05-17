import {difference, union} from 'lodash';
import WdkStore from './WdkStore';
import {filterNodes} from '../utils/TreeUtils';
import {getId, getTargetType} from '../utils/CategoryUtils';
import {actionTypes} from '../actioncreators/RecordViewActionCreator';

/** Store for record page */
export default class RecordViewStore extends WdkStore {

  getInitialState() {
    return {
      record: undefined,
      recordClass: undefined,
      collapsedSections: undefined,
      isLoading: undefined,
      categoryTree: undefined,
      navigationVisible: true,
      navigationQuery: '',
      navigationSubcategoriesExpanded: false
    };
  }

  reduce(state, action) {
    switch (action.type) {
      case actionTypes.ERROR_RECEIVED:
        return Object.assign({}, state, {
          isLoading: false,
          error: action.payload.error
        });

      case actionTypes.ACTIVE_RECORD_LOADING:
        return Object.assign({}, state, {
          isLoading: true,
          error: null
        });

      case actionTypes.ACTIVE_RECORD_RECEIVED: {
        let { record, recordClass, categoryTree } = action.payload;

        return Object.assign({}, state, {
          record,
          recordClass,
          collapsedSections: [],
          isLoading: false,
          categoryTree
        });
      }

      case actionTypes.ACTIVE_RECORD_UPDATED: {
        let { record } = action.payload;
        return Object.assign({}, state, { record });
      }

      case actionTypes.SECTION_VISIBILITY_CHANGED: {
        let collapsedSections = updateList(
          action.payload.name,
          action.payload.isVisible,
          state.collapsedSections
        );
        return Object.assign({}, state, { collapsedSections });
      }

      /**
       * Update visibility of all record fields (tables and attributes).
       * Category section collapsed state will be preserved.
       */
      case actionTypes.ALL_FIELD_VISIBILITY_CHANGED: {
        return Object.assign({}, state, {
          collapsedSections: action.payload.isVisible
          ? difference(state.collapsedSections, getAllFields(state))
          : union(state.collapsedSections, getAllFields(state))
        });
      }

      case actionTypes.NAVIGATION_QUERY_CHANGED: {
        return Object.assign({}, state, {
          navigationQuery: action.payload.query
        })
      }

      case actionTypes.NAVIGATION_VISIBILITY_CHANGED: {
        return Object.assign({}, state, {
          navigationVisible: action.payload.isVisible
        })
      }

      case actionTypes.NAVIGATION_SUBCATEGORY_VISBILITY_CHANGED:
        return Object.assign({}, state, {
          navigationSubcategoriesExpanded: action.payload.isVisible
        })

      default:
        return state;
    }
  }
}

RecordViewStore.actionTypes = actionTypes;

/** Create a new array adding or removing item */
function updateList(item, add, list = []) {
  return add ? list.concat(item) : list.filter(x => x !== item);
}

/** Get all attributes and tables of active record */
function getAllFields(state) {
  return filterNodes(isFieldNode, state.categoryTree)
  .map(getId);
}

function isFieldNode(node) {
  let targetType = getTargetType(node);
  return targetType === 'attribute' || targetType === 'table';
}
