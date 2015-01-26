/**
 * This store retains the UI state for the AnswerPage, including the current
 * Answer resource being displayed.. UI state includes things like loading
 * state, error state, table sorting options, etc.
 *
 * This store is maintaining its internal state using an immutable data
 * structure (with the aid of the library Immutable.js
 * http://facebook.github.io/immutable-js/). The state will be initialized
 * with default values. As actions are dispatched, the state will be updated
 * accordingly. Mostly, the merge() method is used, which will accept any
 * iterable and perform a deep transform to Immutable.js iterables, if needed.
 *
 * Currently, getState() will return a plain-old JavaScript object using
 * the toJS() method provided by Immutate.js. This does a deep traversal
 * of state and converts all iterables to the JS alternative (e.g.,
 * Map -> Object, List -> Array, etc).
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

        // If the loading answer is for a different question, we want to remove
        // the current question. We will save it in case we have an error so that
        // we can roll back the state of the store.
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
          // If not sorting is provided, we will sort by the first attribute.
          // This should probably come from the server, as configured in the
          // model.
          if (!requestData.displayInfo.sorting) {
            requestData.displayInfo.sorting = [{
              columnName: answer.meta.attributes[0].name,
              direction: 'ASC'
            }];
          }

          // If state.displayInfo.attributes isn't defined we want to use the
          // defaults. For now, we will just show whatever is in
          // answer.meta.attributes by default. This is probably wrong.
          // We probably also want to persist the user's choice somehow. Using
          // localStorage is one possble solution.
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
        // rollback to the previous state, and add the error message
        state = previousState.merge({
          error: action.error
        });
        emitChange();
        break;

      case ActionType.ANSWER_MOVE_COLUMN:
        // Reorder state.displayInfo.visibleAttributes
        // withMutations() allows us to treat state as mutable, e.g.
        // setIn will update state in place.
        state = state.withMutations(state => {
          var { columnName, newPosition } = action;
          var keyPath = [ 'displayInfo', 'attributes' ]; // used with setIn http://facebook.github.io/immutable-js/docs/#/Map/setIn
          var attributes = state.getIn(keyPath); // list of attributes we will be altering
          var currentPosition = attributes.findIndex(a => a.get('name') === columnName);
          var attribute = attributes.get(currentPosition); // the attribute being moved

          // remove the attribute from current position, the splice it into the new position
          state.setIn(keyPath, attributes.delete(currentPosition).splice(newPosition, 0, attribute));
        });
        emitChange();
        break;

      case ActionType.ANSWER_CHANGE_ATTRIBUTES:
        // set state.displayInfo.attributes to the new list
        state = state.setIn(['displayInfo', 'attributes'],
                            Immutable.fromJS(action.attributes));
        emitChange();
        break;
    }
  },

  getState() {
    return state.toJS();
  }

});
