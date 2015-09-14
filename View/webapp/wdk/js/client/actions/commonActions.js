import {
  QUESTIONS_ADDED,
  RECORD_CLASSES_ADDED
} from '../constants/actionTypes';

import { restAction } from '../filters/restFilter';

/**
 * ActionCreators that fetch resources from the WDK REST Service.
 */

export function fetchQuestions() {
  return restAction({
    method: 'GET',
    resource: '/question',
    data: { expandQuestions: true },
    types: [ null, null, QUESTIONS_ADDED ],
    shouldFetch(state) {
      return state.resources.questions.length === 0;
    }
  });
}

export function fetchRecordClasses() {
  return restAction({
    method: 'GET',
    resource: '/record',
    data: { expandRecordClasses: true },
    types: [ null, null, RECORD_CLASSES_ADDED ],
    shouldFetch(state) {
      return state.resources.recordClasses.length === 0;
    }
  });
}
