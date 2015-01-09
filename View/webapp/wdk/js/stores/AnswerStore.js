import Store from './Store';
import ActionType from '../ActionType';


/* TODO Figure out how to integrate Immutable.js */

var answer, isLoading, error;

export default new Store({
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.Answer.LOADING:
        isLoading = true;
        error = null;
        emitChange();
        break;

      case ActionType.Answer.LOAD_SUCCESS:
        isLoading = false;
        answer = action.answer;
        emitChange();
        break;

      case ActionType.Answer.LOAD_ERROR:
        isLoading = false;
        error = action.error;
        break;

    }
  },
  getState() {
    return { answer: _.clone(answer), isLoading, error };
  }
});
