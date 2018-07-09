import { Observable } from 'rxjs/Observable';

import WdkStore from 'Core/State/Stores/WdkStore';
import { PageTransitioner } from 'Utils/PageTransitioner';
import WdkService from 'Utils/WdkService';

export interface Action {
  type: string | symbol;
  payload?: string | number | object;
  channel?: string;
  isBroadcast?: boolean;
};

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
 * An Action that carries the type of its `type` and `payload` properties
 */
export interface TypedAction<T extends string, S> {
  type: T;
  payload: S;
}

export interface TypedActionCreator<T extends string, S> {

  /** String used to identify action type. */
  type: T;

  /** Predicate function for testing if an action is of this type. */
  test(action: Action): action is TypedAction<T, S>;

  /** Create an action with a specified payload. */
  create(payload: S): TypedAction<T, S>;
}

interface EmptyTypedActionCreator<T extends string> {

  /** String used to identify action type. */
  type: T;

  /** Predicate function for testing if an action is of this type. */
  test(action: Action): action is TypedAction<T, undefined>;

  /** Create an action with a specified payload. */
  create(): TypedAction<T, undefined>;
}

/**
 * Returns a module that can be used to create Actions. This provides many useful
 * properties to reduce boilerplate while retaining maximum type safety.
 */
export function makeActionCreator<T extends string>(type: T): EmptyTypedActionCreator<T>
export function makeActionCreator<S, T extends string>(type: T): TypedActionCreator<T, S>
export function makeActionCreator(type: string) {
  return { type, create, test };

  function create(payload?: any) {
    return payload ? { type, payload } : { type }
  }

  function test(action: Action) {
    return action.type === type;
  }
}

export function isOneOf<T extends string, S>(...actionCreators: TypedActionCreator<T, S>[]) {
  return function isType(action: Action): action is TypedAction<T, S> {
    return actionCreators.some(ac => ac.test(action));
  }
}

export type ActionType<T extends TypedActionCreator<any, any>> =
  T extends TypedActionCreator<infer S, infer U> ? TypedAction<S, U> : never;

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
