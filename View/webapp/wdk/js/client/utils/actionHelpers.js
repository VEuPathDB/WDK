/**
 * Bind a collection of action functions to a dispatchAction function.
 *
 * @param {Function} dispatchAction
 * @param {Object<Function>} actions
 */
export function wrapActions(dispatchAction, actions) {
  let wrappedActions = {};
  for (let key in actions) {
    wrappedActions[key] = function wrappedAction(...args) {
      return dispatchAction(actions[key](...args));
    }
  }
  return wrappedActions;
}
