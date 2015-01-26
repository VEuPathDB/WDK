/**
 * This store retains the UI state for the AnswerPage, as well as the current
 * Answer resource. UI state includes things like loading state, error state,
 * table sorting options, etc.
 *
 * TODO Fill in the rest of the story, re flux.
 */

import Immutable from 'immutable';
import Store from './Store';
import * as ActionType from '../ActionType';


/**
 * The state of the store. We're using Immutable.js here, but not to its
 * fullest extent. This is sort of a trial run. So far, the interface is very
 * intuitive. -dmf
 */
var state = Immutable.fromJS({
  isLoading: false,
  error: null,
  answer: {},
  displayInfo: {},
  questionDefinition: {}
});

/** Used to roll back on loading errors */
var previousState;

export default new Store({

  /**
   * Handle dispatched actions. Hopefully most of this is self explanatory.
   */
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.ANSWER_LOADING:
        var { requestData: { questionDefinition: { questionName } } } = action;

        if(state.getIn(['questionDefinition', 'questionName']) !== questionName) {
          previousState = state;
          state = state.merge({
            answer: {},
            questionDefinition: {},
            displayInfo: {}
          });
        }

        state = state.merge({
          isLoading: true,
          error: null
        });
        emitChange();
        break;

      case ActionType.ANSWER_LOAD_SUCCESS:
        state = state.withMutations(state => {
          var { answer, requestData } = action;

          // Currently, there isn't a "sort" action creator. We just call
          // loadAnswer() with the sorting configuration. That info is
          // captured here and stored in this store's state.
          if (!requestData.displayInfo.sorting) {
            requestData.displayInfo.sorting = [{
              columnName: answer.meta.attributes[0].name,
              direction: 'ASC'
            }];
          }

          // If state.displayInfo.attributes isn't defined we want to use the
          // defaults. For now, we will just show whatever is in
          // answer.meta.attributes by default. This is probably wrong.
          if (!requestData.displayInfo.attributes) {
            requestData.displayInfo.attributes = answer.meta.attributes;
          }

          state
          .merge({ isLoading: false, answer })
          .merge(requestData);
        });
        emitChange();
        break;

      case ActionType.ANSWER_LOAD_ERROR:
        state = previousState.merge({
          error: action.error
        });
        emitChange();
        break;

      case ActionType.ANSWER_MOVE_COLUMN:
        // Reorder state.displayInfo.visibleAttributes
        state = state.withMutations(state => {
          var { columnName, newPosition } = action;
          var keyPath = [ 'displayInfo', 'attributes' ];
          var as = state.getIn(keyPath);
          var currentPosition = as.findIndex(a => a.get('name') === columnName);
          var thisAttr = as.get(currentPosition);

          state.setIn(keyPath, as.delete(currentPosition).splice(newPosition, 0, thisAttr));
        });
        emitChange();
        break;

      case ActionType.ANSWER_CHANGE_ATTRIBUTES:
        state = state.setIn(['displayInfo', 'attributes'], Immutable.fromJS(action.attributes));
        emitChange();
        break;
    }
  },

  getState() {
    return state.toJS();
  }

});
