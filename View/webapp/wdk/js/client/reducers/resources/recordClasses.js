import {
  RECORD_CLASSES_ADDED
} from '../../constants/actionTypes';

export default function recordClasses(recordClasses = [], action) {
  if (action.type === RECORD_CLASSES_ADDED) {
    recordClasses = action.response;
    // FIXME Remove hardcoded category 'Uncategorized'
    // starthack
    recordClasses.forEach(function(recordClass) {
      recordClass.attributeCategories.push(
        { name: undefined, displayName: 'Uncategorized' }
      );
    });
    // endhack
  }
  return recordClasses;
}
