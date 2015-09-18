import {
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../../constants/actionTypes';

let initialState = {
  hiddenCategories: [],
  collapsedCategories: []
};

export default function recordView(state = initialState, action) {
  switch (action.type) {
    case RECORD_CATEGORY_VISIBILITY_TOGGLED:
      return toggleCategoryVisibility(state, action);
    case RECORD_CATEGORY_COLLAPSED_TOGGLED:
      return toggleCategoryCollapsed(state, action);
    default:
      return state;
  }
}

// FIXME Key by record class
function toggleCategoryVisibility(state, action) {
  let { name, isVisible } = action;
  let hiddenCategories = isVisible === false ? state.hiddenCategories.concat(name)
                       : state.hiddenCategories.filter(function(n) {
                         return n !== name;
                       });
  return Object.assign({}, state, { hiddenCategories });
}

function toggleCategoryCollapsed(state, action) {
  let { name, isCollapsed } = action;
  let collapsedCategories = isCollapsed === true ? state.collapsedCategories.concat(name)
                          : state.collapsedCategories.filter(function(n) {
                              return n !== name;
                            });
  return Object.assign({}, state, { collapsedCategories });
}
