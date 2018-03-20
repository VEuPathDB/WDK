import React from 'react';

import { Action } from 'Core/State/Dispatcher';
import { EnumParam, Parameter } from 'Utils/WdkModel';
import * as List from 'Params/EnumParam/ListEnumParam';
import * as TreeBox from 'Params/EnumParam/TreeBoxEnumParam';
import { isPropsType, Props } from 'Params/Utils';
import { ParamInitAction } from 'Core/ActionCreators/QuestionActionCreators';

type State = TreeBox.State;

export function reduce(state: State, action: Action): State {
  if (!(
    ParamInitAction.isType(action) ||
    TreeBox.ExpandedListSet.isType(action) ||
    TreeBox.SearchTermSet.isType(action)
  )) return state;
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
