/**
 * Returns a function with the signature:
 *    reduceState(State, Action): State
 */
export default function combineReducers(reducers) {
  return function reduceState(state = {}, action) {
    return Object.keys(reducers).reduce(function(nextState, key) {
      return Object.assign(nextState, {
        [key]: reducers[key](state[key], action)
      });
    }, {});
  };
}
