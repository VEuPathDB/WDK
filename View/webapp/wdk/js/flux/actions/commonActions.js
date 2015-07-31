import {
  QUESTIONS_ADDED,
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

function createActions({ dispatcher, service }) {
  return {
    fetchCommonData() {
      // First, create a Promise for the question resource (the ajax request will
      // be made as soon as possible (which will more-or-less be when the current
      // method's execution is complete).
      var questionPromise = service.getResource('/question?expandQuestions=true');

      // Then, create a Promise for the recordClass
      var recordClassPromise = service.getResource('/record?expandRecordClasses=true');

      Promise.all([questionPromise, recordClassPromise])
        .then(function([ questions, recordClasses ]) {
          // FIXME Remove hardcoded category 'Uncategorized'
          // starthack
          recordClasses.forEach(function(recordClass) {
            recordClass.attributeCategories.push(
              { name: 'uncategorized', displayName: 'Uncategorized' }
            );
          });
          // endhack
          dispatcher.dispatch({ type: QUESTIONS_ADDED, questions });
          dispatcher.dispatch({ type: RECORD_CLASSES_ADDED, recordClasses });
        });
    }
  };
}

export default { createActions };
