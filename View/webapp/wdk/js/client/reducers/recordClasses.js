import {
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

export default function recordClasses(recordClasses = [], action) {
  if (action.type === RECORD_CLASSES_ADDED) {
    recordClasses = action.recordClasses;
  }
  return recordClasses;
}
