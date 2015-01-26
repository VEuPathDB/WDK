import * as ActionType from '../ActionType';
import Dispatcher from '../Dispatcher';
import * as ServiceAPI from '../ServiceAPI';


/* helpers */

function dispatchLoadSuccess(questions) {
  Dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOAD_SUCCESS, questions });
}

function dispatchLoadError(error) {
  Dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOAD_ERROR, error });
}

function dispatchLoading() {
  Dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOADING });
}


/* actions */

export function loadQuestions() {
  dispatchLoading();
  ServiceAPI.getResource('/question')
  .then(dispatchLoadSuccess, dispatchLoadError)
  .catch(err => console.assert(false, err));
}
