// Collection param reducers and action creators into an object that is consumed by LegacyParamController
import * as React from 'react';

import { DispatchAction, ActionThunk, ActionCreatorRecord, combineEpics, Epic } from '../utils/ActionCreatorUtils';
import { Action } from '../dispatcher/Dispatcher';
import { Parameter, ParameterValues } from '../utils/WdkModel';

import * as FilterParamNew from './FilterParamNew';
import * as EnumParam from './EnumParam';
import { Seq } from '../utils/IterableUtils';
import { Context, isContextType, isPropsType, ParamModule, Props } from './Utils';


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

export const paramEpic = combineEpics(
  ...(paramModules
    .map(m => m.paramEpic)
    .filter(e => e != null) as Epic[]))