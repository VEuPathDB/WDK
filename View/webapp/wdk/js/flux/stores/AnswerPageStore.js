/**
 * This store retains the UI state for the AnswerPage, including the current
 * Answer resource being displayed.. UI state includes things like loading
 * state, error state, table sorting options, etc.
 *
 * This store is maintaining its internal state using an immutable data
 * structure (with the aid of the library Immutable.js
 * http://facebook.github.io/immutable-js/). The state will be initialized
 * with default values. It's type is Immutable.Map. See
 * http://facebook.github.io/immutable-js/docs/#/Map for method details. We
 * will mainly be using merge(), which accepts plain JavaScript objects, in
 * addition to any Immutable iterable data type. In our case, we will be
 * merging plain JavaScript objects. This operation will copy the keys of the
 * object to same-named keys of the Map. Any nested objects or arrays will be
 * recursively converteed to a Map or List, resp.
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
   *
   * `action` is the action that is being dispatched. It is a plain JavaScript
   * object.
   *
   * `emitChange` is a function that, when called, will call any registered
   * callback functions via the `subscribe` method.
   */
  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.ANSWER_LOADING:
        var questionName = action.requestData.questionDefinition.questionName;

        /*
         * If the loading answer is for a different question, we want to remove
         * the current question. We will save it in case we have an error so that
         * we can roll back the state of the store.
         *
         * `state.getIn(...)` is another way to write
         * `state.get('questionDefinition').get('questionName')`, but without
         * creating intermediate copies.
         */
        if(state.getIn(['questionDefinition', 'questionName']) !== questionName) {
          /*
           * Cache previous state. It will be used to replace the state
           * if we handle ANSWER_LOAD_ERROR.
           */
          previousState = state;

          /*
           * Clear the current state. This helps keep the UI more consistent
           * by not showing unrelated results when loading.
           */
          state = state.merge({
            answer: {},
            questionDefinition: {},
            displayInfo: {}
          });
        }

        /*
         * Finally, set isLoading to true and error to null.
         */
        state = state.merge({
          isLoading: true,
          error: null
        });

        /*
         * This will cause subscribed functions to be called.
         */
        emitChange();
        break;

      case ActionType.ANSWER_LOAD_SUCCESS:

        /* Answer resource */
        var answer = action.answer;

        /*
         * requestData is an object with the keys `displayInfo` and
         * `questionDefinition`. We will be merging these keys into `state`
         * below.
         */
        var requestData = action.requestData;

        /*
         * Currently, there isn't a "sort" action creator. We just call
         * loadAnswer() with the sorting configuration. That info is
         * captured here and stored in this store's state.
         * If not sorting is provided, we will sort by the first attribute.
         * This should probably come from the server, as configured in the
         * model.
         */
        if (!requestData.displayInfo.sorting) {
          requestData.displayInfo.sorting = [{
            columnName: answer.meta.attributes[0].name,
            direction: 'ASC'
          }];
        }

        /*
         * If state.displayInfo.attributes isn't defined we want to use the
         * defaults. For now, we will just show whatever is in
         * answer.meta.attributes by default. This is probably wrong.
         * We probably also want to persist the user's choice somehow. Using
         * localStorage is one possble solution.
         */
        if (!requestData.displayInfo.attributes) {
          requestData.displayInfo.attributes = answer.meta.attributes;
        }

        /*
         * This will update the keys 'isLoading', 'answer', 'displayInfo',
         * and 'questionDefinition' in `state`. `displayInfo` and
         * `questionDefinition` are defined on `requestData`.
         */
        state = state.merge({ isLoading: false, answer }, requestData);

        emitChange();
        break;

      case ActionType.ANSWER_LOAD_ERROR:

        /* rollback to the previous state, and add the error message */
        state = previousState.merge({
          error: action.error
        });

        emitChange();
        break;

      case ActionType.ANSWER_MOVE_COLUMN:

        /* The name of the attribute being moved. */
        /* FIXME Should be attributeName */
        var columnName = action.columnName;

        /* The new position for the attribute */
        var newPosition = action.newPosition;

        /* used with setIn http://facebook.github.io/immutable-js/docs/#/Map/setIn */
        var keyPath = [ 'displayInfo', 'attributes' ];

        /* list of attributes we will be altering */
        var attributes = state.getIn(keyPath);

        /* The current position of the attribute being moved */
        var currentPosition = attributes.findIndex(function(attribute) {
          return attribute.get('name') === columnName;
        });

        /* The attribute being moved */
        var attribute = attributes.get(currentPosition);

        /* Make a temporary copy of attributes with the one being moved removed */
        var newAttributes = attributes.delete(currentPosition);

        /* Splice the attribute being moved into the new position */
        newAttributes = newAttributes.splice(newPosition, 0, attribute);

        /* Set the attributes to the new list */
        state = state.setIn(keyPath, newAttributes);

        emitChange();
        break;

      case ActionType.ANSWER_CHANGE_ATTRIBUTES:
        /*
         * Create new Immutable list of attribtues. We have to use fromJS so
         * that we create Immutable data structures for nested attribute keys.
         */
        var newList = Immutable.fromJS(action.attributes);

        /* set state.displayInfo.attributes to the new list */
        state = state.setIn(['displayInfo', 'attributes'], newList);

        emitChange();
        break;
    }
  },

  getState() {

    /*
     * Convert `state` to a plain JavaScript object. At some point, we should
     * explore returning `state` as-is. This will require updating any modules
     * that work with the store (AnswerPage, etc).
     */
    return state.toJS();
  }

});
