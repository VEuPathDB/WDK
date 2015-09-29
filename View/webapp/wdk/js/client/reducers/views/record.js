import {
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../../constants/actionTypes';

let initialState = {
  hiddenCategories: undefined,
  collapsedCategories: undefined
};

export default function recordView(state = initialState, action) {
  switch (action.type) {
    case RECORD_CATEGORY_VISIBILITY_TOGGLED:
      return Object.assign({}, state, {
        hiddenCategories: toggleCategoryVisibility(state.hiddenCategories, action)
      });

    case RECORD_CATEGORY_COLLAPSED_TOGGLED:
      return Object.assign({}, state, {
        collapsedCategories: toggleCategoryCollapsed(state.collapsedCategories, action)
      });

    default:
      return state;
  }
}

// FIXME Key by record class
function toggleCategoryVisibility(hiddenCategories = [], action) {
  let { name, isVisible } = action;
  return isVisible === false
    ? hiddenCategories.concat(name)
    : hiddenCategories.filter(function(n) {
      return n !== name;
    });
}

function toggleCategoryCollapsed(collapsedCategories = [], action) {
  let { name, isCollapsed } = action;
  return isCollapsed === true
    ? collapsedCategories.concat(name)
    : collapsedCategories.filter(function(n) {
      return n !== name;
    });
}
