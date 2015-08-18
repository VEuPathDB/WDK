import { makeKey } from '../utils/recordUtils';
import {
  RECORD_DETAILS_ADDED,
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../constants/actionTypes';

export default function record(state = getInitialState(), action) {
  switch (action.type) {
    case RECORD_DETAILS_ADDED:
      return addRecordDetails(state, action);
    case RECORD_CATEGORY_VISIBILITY_TOGGLED:
      return toggleCategoryVisibility(state, action);
    case RECORD_CATEGORY_COLLAPSED_TOGGLED:
      return toggleCategoryCollapsed(state, action);
    default:
      return state;
  }
}

function getInitialState() {
  return { records: {}, hiddenCategories: [], collapsedCategories: [] };
}

function addRecordDetails(state, action) {
  let { meta, record } = action;
  let key = makeKey(meta.class, record.id);
  let recordData = state.records[key] || { meta, record };
  // link attribute value and meta
  let { attributes, tables } = record;
  meta.attributes.forEach(function(attributeMeta) {
    let { name } = attributeMeta;
    if (name in attributes) {
      attributes[name] = createAttribute(attributeMeta, attributes[name]);
    }
  });
  // merge `meta` and `record` with state.records[key]
  Object.assign(recordData.meta, meta);
  Object.assign(recordData.record.attributes, attributes);
  Object.assign(recordData.record.tables, tables);
  state.records[key] = recordData;
  return state;
}

// FIXME Key by record class
function toggleCategoryVisibility(state, action) {
  let { name, isVisible } = action;
  state.hiddenCategories = isVisible === false
    ? state.hiddenCategories.concat(name)
    : state.hiddenCategories.filter(function(n) {
      return n !== name;
    });
  return state;
}

function toggleCategoryCollapsed(state, action) {
  let { name, isCollapsed } = action;
  state.collapsedCategories = isCollapsed === true
    ? state.collapsedCategories.concat(name)
    : state.collapsedCategories.filter(function(n) {
      return n !== name;
    });
  return state;
}

function createAttribute(meta, value) {
  return Object.create(meta, {
    value: { value, enumerable: true }
  });
}
