import {
  APP_LOADING,
  APP_ERROR,
  QUESTIONS_ADDED
} from '../constants/actionTypes';

function createActions({ dispatcher, service }) {
  return {
    loadQuestions() {
      dispatcher.dispatch({ type: APP_LOADING, isLoading: true });
      service.getResource('/question?expandQuestions=true')
        .then(questions => {
          dispatcher.dispatch({ type: QUESTIONS_ADDED, questions });
          dispatcher.dispatch({ type: APP_LOADING, isLoading: false });
        }, error => {
          dispatcher.dispatch({ type: APP_ERROR, error });
        })
        .catch(err => console.assert(false, err));
    }
  };
}

export default { createActions };
