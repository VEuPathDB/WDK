import { Observable } from 'rxjs/Observable';

import { Action } from './../dispatcher/Dispatcher';
import { PageTransitioner } from './PageTransitioner';
import WdkService from './WdkService';

export interface ActionCreatorServices {
  wdkService: WdkService;
  transitioner: PageTransitioner;
}

export type ActionCreatorResult<T extends Action> = T
                                           | Promise<T>
                                           | Promise<ActionThunk<T>>
                                           | ActionThunk<T>;

export interface ActionThunk<T extends Action> {
  (dispatch: DispatchAction<T>, services: ActionCreatorServices): void;
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


interface TypedAction<T extends string, S> {
  type: T;
  payload: S;
}

interface Data<T> { }

interface TypedActionCreator<T extends string, S> {
  type: T;
  create(payload: S): TypedAction<T, S>;
  isType(action: Action): action is TypedAction<T, S>;
}

interface EmptyActionCreator<T extends string> extends TypedActionCreator<T, undefined> {
  create(): TypedAction<T, undefined>
}


const empty = Object.create(null);

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

export interface Epic {
  (action$: Observable<Action>, services: ActionCreatorServices): Observable<Action>;
}

export function combineEpics(...epics: Epic[]): Epic {
  return <Epic>((action$, services) =>
    Observable.merge(...epics.map(epic => epic(action$, services))))
}
