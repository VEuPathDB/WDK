/**
 * Temporary type declarations that should eventually be moved to code base
 */
import WdkService from "./utils/WdkService";
import {Action} from "./dispatcher/Dispatcher";
/**
 * Created by dfalke on 8/17/16.
 */

export interface ActionCreatorServices {
  wdkService: WdkService;
}

type ActionCreatorResult<T extends Action> = T
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

/**
 * The DispatchAction type describes the type of function that is used to
 * dispatch actions.
 */
export type DispatchAction<T extends Action> = (action: ActionCreatorResult<T>) => ActionCreatorResult<T>;
