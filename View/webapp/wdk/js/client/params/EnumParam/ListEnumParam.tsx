import React from 'react';

import { Parameter, ListEnumParam } from "../../utils/WdkModel";
import { Props } from "../Utils";
import { isType as isEnumParam } from '../EnumParam';

export function isType(parameter: Parameter): parameter is ListEnumParam {
  return (
    isEnumParam(parameter) && (
      parameter.displayType === 'select' ||
      parameter.displayType === 'checkbox' ||
      parameter.displayType === 'typeAhead'
    )
  );
}

export function ListEnumParam(props: Props<ListEnumParam, void>) {
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
