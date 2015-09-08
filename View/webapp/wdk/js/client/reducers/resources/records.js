import { makeKey } from '../../utils/recordUtils';
import {
  ANSWER_ADDED,
  RECORD_DETAILS_ADDED
} from '../../constants/actionTypes';

export default function records(state = {}, action) {
  switch (action.type) {
    case ANSWER_ADDED:
      return addAnswerRecords(state, action);
    case RECORD_DETAILS_ADDED:
      return addRecordDetails(state, action);
    default:
      return state;
  }
}

function addAnswerRecords(records, action) {
  let { meta } = action.response;
  let recordClass = meta.class;
  // make a copy of records
  records = Object.assign({}, records);

  // add answer records to records object
  action.response.records.forEach(function(record) {
    let key = makeKey(recordClass, record.id);
    records[key] = record;
  });

  // return new records object
  return records;
}

/**
 * Add attributes and tables to a record.
 * If record isn't already stored in state,
 * just add it.
 */
function addRecordDetails(records, action) {
  let { meta, record } = action.response;
  let key = makeKey(meta.class, record.id);
  // make a copy of the records object
  records = Object.assign({}, records);
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

  // return new record object with the new record details added
  return records;
}
