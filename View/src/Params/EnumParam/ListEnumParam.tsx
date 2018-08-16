import React from 'react';

import { Parameter, ListEnumParam } from 'Utils/WdkModel';
import { Props } from 'Params/Utils';
import enumParamModule from 'Params/EnumParam';

export function isType(parameter: Parameter): parameter is ListEnumParam {
  return (
    enumParamModule.isType(parameter) && (
      parameter.displayType === 'select' ||
      parameter.displayType === 'checkBox' ||
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
