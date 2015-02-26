import Immutable from 'immutable';
import Store from './Store';
import * as ActionType from '../ActionType';


export default new Store ({

  state: Immutable.fromJS({
    questions: [],
    isLoading: false,
    error: null
  }),

  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.QUESTION_LIST_LOADING:
        this.state = this.state.merge({
          isLoading: true,
          error: null
        });
        emitChange();
        break;

      case ActionType.QUESTION_LIST_LOAD_SUCCESS:
        this.state = this.state.merge({
          isLoading: false,
          questions: action.questions
        });
        emitChange();
        break;

      case ActionType.QUESTION_LIST_LOAD_ERROR:
        this.state = this.state.merge({
          isLoading: false,
          error: action.error
        });
        emitChange();
        break;

    }
  },

  getState() {
    return this.state.toJS();
  }
});
