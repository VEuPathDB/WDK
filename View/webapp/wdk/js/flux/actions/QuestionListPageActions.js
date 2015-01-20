"use strict";

import ActionType from '../ActionType';
import Dispatcher from '../Dispatcher';
import * as ServiceAPI from '../ServiceAPI';


/* helpers */

function dispatchLoadSuccess(questions) {
  Dispatcher.dispatch({ type: ActionType.QuestionList.LOAD_SUCCESS, questions });
}

function dispatchLoadError(error) {
  Dispatcher.dispatch({ type: ActionType.QuestionList.LOAD_ERROR, error });
}

function dispatchLoading() {
  Dispatcher.dispatch({ type: ActionType.QuestionList.LOADING });
}


/* actions */

export function loadQuestions() {
  dispatchLoading();
  ServiceAPI.getResource('/question')
  .then(dispatchLoadSuccess, dispatchLoadError)
  .catch(err => console.assert(false, err));
}
