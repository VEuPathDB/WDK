import React from 'react';

import { Action } from '../dispatcher/Dispatcher';
import { EnumParam, Parameter } from '../utils/WdkModel';
import * as List from './EnumParam/ListEnumParam';
import * as TreeBox from './EnumParam/TreeBoxEnumParam';
import { isPropsType, Props } from './Utils';

type State = TreeBox.State;

export function reduce(state: State, action: Action): State {
  const { parameter } = action.payload;
  if (parameter == null || !isType(parameter)) return state;
  if (TreeBox.isType(parameter)) {
    return TreeBox.reduce(state, action);
  }
  return state;
}

// Use this for both EnumParam and FlatVocabParam.
export function isType(parameter: Parameter): parameter is EnumParam {
  return (
    parameter.type === 'EnumParam' ||
    parameter.type === 'FlatVocabParam'
  );
}

// TODO Handle various displayTypes (see WDK/Model/lib/rng/wdkModel.rng).
export function ParamComponent(props: Props<EnumParam, any>) {
  if (isPropsType(props, TreeBox.isType)) {
    return (
      <TreeBox.TreeBoxEnumParam {...props} />
    );
  }

  else if (isPropsType(props, List.isType)) {
    return (
      <List.ListEnumParam {...props} />
    )
  }

  else {
    return (
      <div>Unknown enum param type: {props.parameter.displayType}</div>
    )
  }
}
