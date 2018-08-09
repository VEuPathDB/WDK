import * as React from 'react';

import * as EnumParamModule from 'Params/EnumParam';
import * as FilterParamNewModule from 'Params/FilterParamNew';
import * as NumberParamModule from 'Params/NumberParam';
import * as NumberRangeParamModule from 'Params/NumberRangeParam';
import { combineObserve, ActionObserver } from 'Utils/ActionCreatorUtils';
import { Parameter } from 'Utils/WdkModel';

import { isPropsType, ParamModule, Props } from './Utils';
import { QuestionStore } from 'Core/State/Stores';

// Param modules
// -------------
const paramModules = [
  EnumParamModule,
  FilterParamNewModule,
  NumberParamModule,
  NumberRangeParamModule
] as ParamModule<Parameter, any>[];


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

export const observeParam = combineObserve(
  ...(paramModules
    .map(m => m.observeParam)
    .filter(e => e != null) as ActionObserver[]))
