var Store = require('./Store');
var ActionType = require('../ActionType');

/* TODO Figure out how to integrate Immutable.js */

var answer, isLoading, error;

module.exports = new Store({
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
