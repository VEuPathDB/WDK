var Store = require('./Store');
var ActionType = require('./ActionType');

/* TODO Figure out how to integrate Immutable.js */

var answer, isLoading;

module.exports = new Store({
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.Answer.LOADING:
        isLoading = true;
        emitChange();
        break;

      case ActionType.Answer.LOADED:
        isLoading = false;
        answer = action.answer;
        emitChange();
        break;

    }
  },
  getState() {
    return { isLoading, answer: _.clone(answer) };
  }
});
