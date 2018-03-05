import React from 'react';
import { Parameter, NumberParam } from 'Utils/WdkModel';
import NumberSelector from 'Components/InputControls/NumberSelector';
import { Props } from 'Params/Utils';

export function isType(param: Parameter): param is NumberParam {
  return param.type === 'NumberParam';
}

export function ParamComponent(props: Props<NumberParam, void>) {
  const { parameter, value, onParamValueChange } = props;
  console.info('Number param', { NumberSelector, props });
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
