import React from 'react';
import { Parameter, NumberParam } from '../utils/WdkModel';
import NumberSelector from '../components/NumberSelector';
import { Props } from './Utils';

export function isType(param: Parameter): param is NumberParam {
  return param.type === 'NumberParam';
}

export function ParamComponent(props: Props<NumberParam, void>) {
  const { parameter, value, onParamValueChange } = props;
  return (
    <NumberSelector
      start={parameter.min}
      end={parameter.max}
      step={parameter.step}
      value={Number(value)}
      onChange={value => onParamValueChange(String(value))}
    />
  )
}