import {
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

function update(recordClasses = [], action) {
  if (action.type === RECORD_CLASSES_ADDED) {
    recordClasses = action.recordClasses;
  }
  return recordClasses;
}

export default {
  update
};
