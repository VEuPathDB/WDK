import RestAPI from '../utils/restAPI';
import {
  QUESTIONS_ADDED,
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

/**
 * The following functions return a Promise which resolves with an Action.
 * This allows async Actions to be batch dispatched.
 */

function fetchQuestions(endpoint) {
  return RestAPI.getResource(endpoint + '/question?expandQuestions=true')
    .then(function(questions) {
      return { type: QUESTIONS_ADDED, questions };
    });
}

function fetchRecordClasses(endpoint) {
  return RestAPI.getResource(endpoint + '/record?expandRecordClasses=true')
    .then(function(recordClasses) {
      // FIXME Remove hardcoded category 'Uncategorized'
      // starthack
      recordClasses.forEach(function(recordClass) {
        recordClass.attributeCategories.push(
          { name: undefined, displayName: 'Uncategorized' }
        );
      });
      // endhack
      return { type: RECORD_CLASSES_ADDED, recordClasses };
    });
}

function fetchCommonData() {
  return function(dispatch, state, config) {
    Promise.all([
      fetchQuestions(config.endpoint),
      fetchRecordClasses(config.endpoint)
    ]).then(dispatch);
  };
}

export default {
  fetchQuestions,
  fetchRecordClasses,
  fetchCommonData
};
