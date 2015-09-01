import { makeKey } from '../utils/recordUtils';
import {
  ANSWER_ADDED,
  RECORD_DETAILS_ADDED,
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../constants/actionTypes';

export default function record(state = getInitialState(), action) {
  switch (action.type) {
    case ANSWER_ADDED:
      return addAnswerRecords(state, action);
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

function addAnswerRecords(state, action) {
  let { meta } = action.response;
  let recordClass = meta.class;
  action.response.records.forEach(function(record) {
    let key = makeKey(recordClass, record.id);
    state.records[key] = record;
    // state.records[key] = mergeRecordWithMeta(meta, record);
  });
  return state;
}

/**
 * Add attributes and tables to a record.
 * If record isn't already stored in state,
 * just add it.
 */
function addRecordDetails(state, action) {
  let { meta, record } = action.response;
  // record = mergeRecordWithMeta(meta, record);
  let key = makeKey(meta.class, record.id);
  let stateRecord = state.records[key] || record;
  if (stateRecord !== record) {
    Object.assign(stateRecord.attributes, record.attributes);
    Object.assign(stateRecord.tables, record.tables);
  }
  state.records[key] = stateRecord;
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

function mergeRecordWithMeta(meta, record) {
  let { attributes, tables } = record;
  meta.attributes.forEach(function(attributeMeta) {
    let { name } = attributeMeta;
    if (name in attributes) {
      attributes[name] = Object.create(attributeMeta, {
        value: {
          value: attributes[name],
          enumerable: true
        }
      });
    }
  });
  return record;
}
