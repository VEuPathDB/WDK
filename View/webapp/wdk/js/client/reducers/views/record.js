import {
  RECORD_CATEGORY_COLLAPSED_TOGGLED,
  RECORD_TABLE_COLLAPSED_TOGGLED
} from '../../constants/actionTypes';

let initialState = {
  hiddenCategories: undefined,
  collapsedCategories: undefined,
  collapsedTables: undefined
};

export default function recordView(state = initialState, action) {
  switch (action.type) {
    case RECORD_CATEGORY_COLLAPSED_TOGGLED:
      let collapsedCategories = updateList(
        state.collapsedCategories,
        action.name,
        action.isCollapsed
      );
      return Object.assign({}, state, { collapsedCategories });

    case RECORD_TABLE_COLLAPSED_TOGGLED:
      let collapsedTables = updateList(
        state.collapsedTables,
        action.name,
        action.isCollapsed
      );
      return Object.assign({}, state, { collapsedTables });

    default:
      return state;
  }
}

function updateList(list = [], item, add) {
  return add ? list.concat(item) : list.filter(x => x !== item);
}
