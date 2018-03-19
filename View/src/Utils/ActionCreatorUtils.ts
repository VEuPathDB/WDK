import { Observable } from 'rxjs/Observable';

import Dispatcher, { Action } from 'Core/State/Dispatcher';
import { PageTransitioner } from 'Utils/PageTransitioner';
import WdkService from 'Utils/WdkService';
import WdkStore from 'Core/State/Stores/WdkStore';
import { isPromise } from 'Utils/PromiseUtils';


export interface ActionCreatorServices {
  wdkService: WdkService;
  transitioner: PageTransitioner;
}

export type ActionCreatorResult<T extends Action> = T
                                           | ActionThunk<T>
                                           | ActionCreatorResultArray<T>
                                           | ActionCreatorResultPromise<T>;

interface ActionCreatorResultArray<T extends Action> extends Array<ActionCreatorResult<T>> {}

interface ActionCreatorResultPromise<T extends Action> extends Promise<ActionCreatorResult<T>> {}

export interface ActionThunk<T extends Action> {
  (services: ActionCreatorServices): ActionCreatorResult<T>;
}

export const emptyType = Symbol('empty');

export type EmptyAction = {
  type: typeof emptyType
}

export const emptyAction: EmptyAction = {
  type: emptyType
}

/**
 * The ActionCreator type describes the type of function that
 * DispatchAction accepts.
 */
export interface ActionCreator<T extends Action> {
  (...args: any[]): ActionCreatorResult<T>;
}

export type ActionCreatorRecord<T extends Action> = Record<string, ActionCreator<T>>

/**
 * The DispatchAction type describes the type of function that is used to
 * dispatch actions.
 */
export type DispatchAction<T extends Action> = (action: ActionCreatorResult<T>) => ActionCreatorResult<T>;

/**
 * Create a function that takes a channel and creates a dispatch function
 * `dispatchAction` that forwards calls to `dispatcher.dispatch` using the
 * channel as a scope for the audience of the action.  In dispatchAction:
 *
 * If `action` is a function, it will be called with `dispatchAction` and
 * `services`. Calling it with `dispatchAction` allows for composability since
 * an action function can in turn call another action function. This is useful
 * for creating higher-order dispatch helpers, such as latest, once, etc.
 *
 * If `action` is an object, `dispatcher.dispatch` will be called with it.
 *
 * An `action` function should ultimately return an object to invoke a dispatch.
 *
 * @param {String} rootUrl
 * @param {Dispatcher} dispatcher
 * @param {Object?} serviceSubset
 */
export function getDispatchActionMaker(dispatcher: Dispatcher, services: ActionCreatorServices) {
  return function makeDispatchAction(channel: string) {
    if (channel === undefined) {
      console.warn("Call to makeDispatchAction() with no channel defined.");
    }

    const dispatchAction = tryCatch(
      function dispatch(action: ActionCreatorResult<Action>) {
        if (typeof action === 'function') {
          // Call the function with dispatchAction and services
          // TODO Change this to `dispatchAction(action(services))`. Doing this alone will make it impossible
          // for an ActionCreator to dispatch multiple actions. We can either handle an array as a case below,
          // and call dispatchAction on each item of the array, or more generally we can support iterables.
          // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols
          return dispatchAction(action(services));
        }
        else if (isPromise(action)) {
          return action.then(result => dispatchAction(result)).then(undefined, logError);
        }
        else if (Array.isArray(action)) {
          return action.map(dispatchAction);
        }
        else if (action == null) {
          throw new Error("Action received is undefined or is null");
        }
        else if (action.type == null) {
          throw new Error("Action received does not have a `type` property.");
        }
        if (action.type === emptyType) {
          // nothing to dispatch, so bail
          return;
        }
        // assign channel if requested
        action.channel = (action.isBroadcast ? undefined : channel);
        return dispatcher.dispatch(action);
      },
      logError
    );

    return dispatchAction as DispatchAction<Action>;
  };

  function logError(error: Error) {
    console.error(error);
    services.wdkService.submitError({
      name: error.name,
      message: error.message,
      stack: error.stack
    }).catch(err => {
      console.error('Could not submit error to log.', err);
    })
  }
}


function tryCatch<T extends Function>(fn: T, handleError: (error: Error) => any) {
  return (function tryCatchWrapper(...args: any[]) {
    try {
      return fn(...args)
    }
    catch(error) {
      return handleError(error);
    }
  });
}

/**
 * An Action that carries the type of its `type` and `payload` properties
 */
interface TypedAction<T extends string, S> {
  type: T;
  payload: S;
}

/**
 * A type used to carry some generic type. Used by the `payload()` function below.
 */
interface Data<T> { }

/**
 * An object that can be used to create Actions and to test for them.
 */
interface TypedActionCreator<T extends string, S> {
  // For convenience
  type: T;
  // Create a TypedAction<T,S>
  create(payload: S): TypedAction<T, S>;
  // Verify if an action is a TypedAction<T,S>
  isType(action: Action): action is TypedAction<T, S>;
}

/**
 * An Action with no payload
 */
interface EmptyActionCreator<T extends string> extends TypedActionCreator<T, undefined> {
  create(): TypedAction<T, undefined>
}


// A basic object used to satisfy typescript's compiler (see `payload()` below).
const empty = Object.create(null);

/**
 * This is a trick to make `makeActionCreator()` infer the type of the Action's
 * `payload` property. By doing this, we can declare the types of an Action's
 * `type` and `payload` property non-redundantly. Without this trick, we would
 * have to declare the generic types as well as pass the string value of `type`:
 * `const ActionCreator = makeActionCreator<'my-type', { name: string }>('my-type');`
 *
 * @example
 * ```
 * // Note that we have to call `payload` (see the parentheses ----------------vv)
 * const ActionCreator = makeActionCreator('my-type', payload<{ name: string }>());
 * ```
 */
export function payload<T = undefined>(): Data<T> {
  return empty as Data<T>;
}


/**
 * Returns a class that can be used to create Actions. This provides many useful
 * properties to reduce boilerplate while retaining maximum type safety.
 */
export function makeActionCreator<T extends string>(type: T): EmptyActionCreator<T>;
export function makeActionCreator<T extends string, S>(type: T, _: Data<S>): TypedActionCreator<T, S>;
export function makeActionCreator<T extends string, S>(type: T, _?: Data<S>): TypedActionCreator<T, S> {
  class Base implements TypedAction<T, S> {
    readonly type = type
    constructor(readonly payload: S) {}
    static type = type;
    static isType(action: TypedAction<string, any>): action is TypedAction<T, S> {
      return action.type === type;
    }
  }

  return _ == null
    ? class ActionCreator extends Base {
      static create() {
        return new ActionCreator(undefined as any);
      }
    }
    : class ActionCreator extends Base {
      static create(payload: S) {
        return new ActionCreator(payload);
      }
    }
}

export function isOneOf<T extends string, S>(...actionCreators: TypedActionCreator<T, S>[]) {
  return function isType(action: Action): action is TypedAction<T, S> {
    return actionCreators.some(ac => ac.isType(action));
  }
}

export interface EpicServices<T extends WdkStore = WdkStore> extends ActionCreatorServices {
  store: T;
}

/**
 * An Epic can be thought of as a listener that reacts to specific Actions that
 * get dispatched, by creating more actions. This is useful for creating actions
 * that require asynchronous work (such as loading data from a server).
 *
 * The basic shape of an Epic is that it is a function that consumes a stream of
 * Actions, and it returns a stream of new Actions. The Actions it consumes are
 * those that have already been handled by the store. The Actions it produces
 * are handled by the Store as they are emitted (more on that later). Note that
 * it can return an empty stream, which might happen if an Action it expects is
 * never emitted.
 *
 * For convenience, Epics in WDK will also have configured services passed to
 * them.
 *
 * Epics are also scoped to a store. This means that an Epic will only receive
 * Actions for which WdkStore#storeShouldReceiveAction(channel) returns true,
 * and all Actions produced by the Epic will contain the Store's channel
 * (unless the `broadcast()` action decorator is used).
 */
export interface Epic<T extends WdkStore = WdkStore>{
  (action$: Observable<Action>, services: EpicServices<T>): Observable<Action>;
}

/**
 * Creates an Epic that emits the Actions of all input Epics. Actions are
 * emitted in the order that the input Epics emit actions (e.g., they are
 * interleaved).
 */
export function combineEpics<T extends WdkStore>(...epics: Epic<T>[]): Epic<T> {
  return <Epic<T>>((action$, services) =>
    Observable.merge(...epics.map(epic => epic(action$, services))))
}
