import { makeKey } from '../utils/recordUtils';
import {
  ANSWER_ADDED,
  RECORD_DETAILS_ADDED,
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../constants/actionTypes';

let initialState = {
  records: {},
  hiddenCategories: [],
  collapsedCategories: []
};

export default function record(state = initialState, action) {
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

function addAnswerRecords(state, action) {
  let { meta } = action.response;
  let recordClass = meta.class;
  // make a copy of records
  let records = Object.assign({}, state.records);

  // add answer records to records object
  action.response.records.forEach(function(record) {
    let key = makeKey(recordClass, record.id);
    records[key] = record;
  });

  // return a new copy of state with new records object
  return Object.assign({}, state, { records });
}

/**
 * Add attributes and tables to a record.
 * If record isn't already stored in state,
 * just add it.
 */
function addRecordDetails(state, action) {
  let { meta, record } = action.response;
  let key = makeKey(meta.class, record.id);
  // make a copy of the records object
  let records = Object.assign({}, state.records);
  // make a copy of the existing record object, or just use the new record
  let stateRecord = records[key] === undefined ? record
                  : Object.assign({}, records[key]);

  // if we already had the record, merge the new attributes and tables
  if (stateRecord !== record) {
    Object.assign(stateRecord.attributes, record.attributes);
    Object.assign(stateRecord.tables, record.tables);
  }

  // add the merged record to the new records object
  records[key] = stateRecord;

  // return a new state object with the new record details added
  return Object.assign({}, state, { records });
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
