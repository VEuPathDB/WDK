/**
 * Returns a function with the signature:
 *
 *    <T>(state: T, action: Object): => T
 *
 * If a reducer function causes a part of the state to change, it is assumed
 * the reducer will return a new object. This information is used to determine
 * if `reduceState` should return a new object or the same object. This is
 * effectively immutability.
 */
export default function combineReducers(reducers) {
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
