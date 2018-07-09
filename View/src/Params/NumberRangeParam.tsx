import React from 'react';

import NumberRangeSelector from 'Components/InputControls/NumberRangeSelector';
import { Props } from 'Params/Utils';
import { NumberRangeParam, Parameter } from 'Utils/WdkModel';

export function isType(param: Parameter): param is NumberRangeParam {
  return param.type === 'NumberRangeParam';
}

export function ParamComponent(props: Props<NumberRangeParam, void>) {
  const { parameter, value, onParamValueChange } = props;
  return (
    <NumberRangeSelector
      start={parameter.min}
      end={parameter.max}
      step={parameter.step}
      value={JSON.parse(value)}
      onChange={value => onParamValueChange(JSON.stringify(value))}
    />
  )
}
