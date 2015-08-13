import RestAPI from '../utils/restAPI';
import {
  APP_LOADING,
  APP_ERROR,
  QUESTIONS_ADDED
} from '../constants/actionTypes';

function loadQuestions() {
  return function (dispatcher, state, config) {
    dispatch({ type: APP_LOADING, isLoading: true });
    RestAPI.getResource(config.endpoint + '/question?expandQuestions=true')
      .then(questions => {
        dispatch({ type: QUESTIONS_ADDED, questions });
        dispatch({ type: APP_LOADING, isLoading: false });
      }, error => {
        dispatch({ type: APP_ERROR, error });
      })
      .catch(err => console.assert(false, err));
  }
}

export default { loadQuestions };
