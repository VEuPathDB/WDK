/**
 * Creates a function that returns a reducer function with the signature:
 *
 *    F<U, V> = (state: U, action: V) => U;
 *
 * When called, the reducer function will return an object whose keys match
 * the keys of the `reducers` object this function is called with.
 *
 * Consider the following reducer function created with this function:
 *
 *    let reducer = combineReducers({
 *      count: function reduceCount(count = 0, action) {
 *        // ...
 *      },
 *      color: function reduceColor(color = 'green', action) {
 *        // ...
 *      }
 *    });
 *
 * Assuming reduceCount always returns a number, and reducerColor always returns
 * a string, `reducer` defined above will alway return an Object of the
 * following type:
 *
 *    type State = { count: number; color: string; };
 *
 *
 * If a reducer function causes a part of the state to change, it is assumed
 * the reducer will return a new object. This information is used to determine
 * if `reduceState` should return a new object or the same object. This is
 * effectively immutability.
 *
 * @param {Object} reducers An Object whose property values are reducer functions.
 * @returns {Function} Returns a reducer function.
 */
export function combineReducers(reducers) {
  return function reduceState(state = {}, action) {
    return Object.keys(reducers).reduce(function(state, key) {
      let reducer = reducers[key];
      let childState = state[key];
      let newChildState = reducer(childState, action);

      return newChildState === childState ? state
           : Object.assign({}, state, {
               [key]: newChildState
             });
    }, state);
  };
}
