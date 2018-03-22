import * as React from 'react';

import * as EnumParam from 'Params/EnumParam';
import * as FilterParamNew from 'Params/FilterParamNew';
import * as NumberParam from 'Params/NumberParam';
import * as NumberRangeParam from 'Params/NumberRangeParam';
import { combineEpics, Epic } from 'Utils/ActionCreatorUtils';
import { Parameter } from 'Utils/WdkModel';

import { isPropsType, ParamModule, Props } from './Utils';


// Param modules
// -------------
const paramModules: ParamModule<Parameter, any>[] = [
  EnumParam,
  FilterParamNew,
  NumberParam,
  NumberRangeParam
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
