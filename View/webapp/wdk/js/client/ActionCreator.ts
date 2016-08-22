/**
 * Temporary type declarations that should eventually be moved to code base
 */
import {Action} from "./dispatcher/Dispatcher";
import WdkService from "./utils/WdkService";
/**
 * Created by dfalke on 8/17/16.
 */

export interface ActionCreatorServices {
  wdkService: WdkService;
}

type ActionCreatorResult = Action | Promise<Action | ActionThunk> | ActionThunk;

export interface ActionThunk {
  (dispatch: DispatchAction, services: ActionCreatorServices): void;
}

/**
 * The ActionCreator type describes the type of function that
 * DispatchAction accepts.
 */
export type ActionCreator = (...args: any[]) => ActionCreatorResult;

/**
 * The DispatchAction type describes the type of function that is used to
 * dispatch actions.
 */
export type DispatchAction = (action: ActionCreatorResult) => ActionCreatorResult;
