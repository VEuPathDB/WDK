import { Action } from "./ActionCreatorUtils";

export interface Reducer<T, S extends Action> {
  (state: T, action: S): T;
}

export interface ActionPredicate<T extends Action> {
  (action: Action): action is T;
}

/**
 * Create a reducer which will find the first pair whose action predicate
 * returns `true` and return the result of the associated reducer. Otherwise,
 * `state` will be returns as-is.
 *
 * @param pairs An array of tuples of action predicate and reducer
 */
export const matchAction = <T, S extends Action>(pairs: [ActionPredicate<S>, Reducer<T, S> ][]): Reducer<T, Action> => (state: T, action: Action) => {
  for (const [ pred, reducer ] of pairs) {
    if (pred(action)) return reducer(state, action);
  }
  return state;
}

/**
 * Creates a composite reducer. The provided reducers are called right-to-left,
 * where the previous reducer's return value is passed as `state` to the next
 * reducer.
 *
 * @param reducers
 */
export const composeReducers = <T, S extends Action>(...reducers: Reducer<T, S>[]): Reducer<T, S> => (state: T, action: S) =>
  reducers.reduceRight((state, reducer) => reducer(state, action), state);