import React from 'react';

import { Parameter, EnumParam } from "../utils/WdkModel";
import { Props } from "./index";

// Use this for both EnumParam and FlatVocabParam.
export function isType(parameter: Parameter): parameter is EnumParam {
  return (
    parameter.type === 'EnumParam' ||
    parameter.type === 'FlatVocabParam'
  );
}

// TODO Handle various displayTypes (see WDK/Model/lib/rng/wdkModel.rng).
export function ParamComponent(props: Props<EnumParam, void>) {
  return (
    <select
      multiple={props.parameter.multiPick}
      value={props.value}
      onChange={e => props.onParamValueChange(e.target.value)}
    >
      {props.parameter.vocabulary.map(entry => (
        <option key={entry[0]} value={entry[0]}>{entry[1]}</option>
      ))}
    </select>
  );
}
