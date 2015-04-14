import createStore from '../utils/createStore';
import {
  QUESTION_LOAD_SUCCESS,
  QUESTION_LIST_LOADING,
  QUESTION_LIST_LOAD_SUCCESS,
  QUESTION_LIST_LOAD_ERROR
} from '../ActionType';


export default createStore ({

  init() {
    this.state = {
      questions: []
    };
  },

  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case QUESTION_LOAD_SUCCESS:
        this.state.questions.push(action.question);
        emitChange();
        break;

      case QUESTION_LIST_LOADING:
        this.state.isLoading = true;
        this.state.error = null;
        emitChange();
        break;

      case QUESTION_LIST_LOAD_SUCCESS:
        this.state.isLoading = false;
        this.state.questions = action.questions;
        emitChange();
        break;

      case QUESTION_LIST_LOAD_ERROR:
        this.state.isLoading = false;
        this.state.error = action.error;
        emitChange();
        break;

    }
  },

  getState() {
    return this.state;
  }
});
