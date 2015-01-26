import _ from 'lodash';
import Store from './Store';
import * as ActionType from '../ActionType';


/* TODO Figure out how to integrate Immutable.js */

var questions, isLoading, error;

export default new Store({
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.QUESTION_LIST_LOADING:
        isLoading = true;
        error = null;
        emitChange();
        break;

      case ActionType.QUESTION_LIST_LOAD_SUCCESS:
        isLoading = false;
        questions = action.questions;
        emitChange();
        break;

      case ActionType.QUESTION_LIST_LOAD_ERROR:
        isLoading = false;
        error = action.error;
        emitChange();
        break;

    }
  },
  getState() {
    return { questions: _.clone(questions), isLoading, error };
  }
});
