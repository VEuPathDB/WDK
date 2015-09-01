import {
  QUESTIONS_ADDED,
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

import { restAction } from '../filters/restFilter';

/**
 * The following functions return a Promise which resolves with an Action.
 * This allows async Actions to be batch dispatched.
 */

function fetchQuestions() {
  return restAction({
    method: 'GET',
    resource: '/question',
    data: { expandQuestions: true },
    types: [ null, null, QUESTIONS_ADDED ],
    shouldFetch(state) {
      return state.questions.length === 0;
    }
  });
}

function fetchRecordClasses() {
  return restAction({
    method: 'GET',
    resource: '/record',
    data: { expandRecordClasses: true },
    types: [ null, null, RECORD_CLASSES_ADDED ],
    shouldFetch(state) {
      return state.recordClasses.length === 0;
    }
  });
}

export default {
  fetchQuestions,
  fetchRecordClasses
};
