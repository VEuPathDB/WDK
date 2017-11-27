// Collection param reducers and action creators into an object that is consumed by LegacyParamController
import * as React from 'react';

import { DispatchAction, ActionThunk, ActionCreatorRecord } from '../ActionCreator';
import { Action } from '../dispatcher/Dispatcher';
import { Parameter, ParameterValues } from '../utils/WdkModel';

import * as FilterParamNew from './FilterParamNew';
import * as EnumParam from './EnumParam';


// Types
// -----
export type Context<T extends Parameter> = {
  questionName: string;
  parameter: T;
  paramValues: ParameterValues;
}

export type Props<T extends Parameter, S> = {
  ctx: Context<T>;
  parameter: T;
  value: string;
  uiState: S;
  dispatch: DispatchAction<Action>;
  onParamValueChange: (value: string) => void;

}

type ParamModule<T extends Parameter, S> = {
  isType: (p: Parameter) => p is T;
  ParamComponent: React.ComponentType<Props<T, S>>;
  ActionCreators?: ActionCreatorRecord<any>;
  reduce?: (state: S, action: any) => S;
}


// Param modules
// -------------
const paramModules: ParamModule<Parameter, any>[] = [
  EnumParam,
  FilterParamNew
];


// API used by Question{ActionCreators,Controller,Store}
// -----------------------------------------------------

/**
 * Parameter renderer.
 */
export function ParamComponent<T extends Parameter>(props: Props<T, any>) {
  for (let paramModule of paramModules) {
    if (isPropsType(props, paramModule.isType)) {
      return <paramModule.ParamComponent {...props} />
    }
  }
  return (
    <div>
      <em style={{color: 'red'}}>Unknown parameter type {props.parameter.type} </em>
      <input type="text" value={props.value} readOnly />
    </div>
  );
}

/**
 * Parameter state-action reducer.
 */
export function reduce<T extends Parameter>(parameter: T, state: any, action: any): any {
  for (let paramModule of paramModules) {
    if (paramModule.isType(parameter) && paramModule.reduce) {
      return paramModule.reduce(state, action);
    }
  }
  return state;
}

/**
 * Parameter init ActionCreator.
 */
export function init<T extends Parameter>(ctx: Context<T>): ActionThunk<any> {
  return dispatch => {
    for (let paramModule of paramModules) {
      if (
        isContextType(ctx, paramModule.isType) &&
        paramModule.ActionCreators &&
        paramModule.ActionCreators.init
      ) {
        return dispatch(paramModule.ActionCreators.init(ctx));
      }
    }
  }
}


// Type guards (see https://www.typescriptlang.org/docs/handbook/advanced-types.html#user-defined-type-guards)
// -----------------------------------------------------------------------------------------------------------

function isPropsType<T extends Parameter>(
  props: Props<Parameter, any>,
  predicate: (parameter: Parameter) => parameter is T
): props is Props<T, any> {
  return predicate(props.parameter);
}

function isContextType<T extends Parameter>(
  context: Context<Parameter>,
  predicate: (parameter: Parameter) => parameter is T
): context is Context<T> {
  return predicate(context.parameter);
}
