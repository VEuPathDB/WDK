import React from 'react';
import { EnumParam } from '../../utils/WdkModel';

type Props = {
  parameter: EnumParam;
  selectedCount: number;
};

export default function SelectionInfo(props: Props) {
  const { minSelectedCount, maxSelectedCount } = props.parameter;
  const hasMin = minSelectedCount > 0;
  const hasMax = maxSelectedCount > 0;
  const message = hasMin && hasMax
    ? `You may only selected between ${minSelectedCount} and ${maxSelectedCount} values for this parameter.`
    : hasMin ? `You must select at least ${minSelectedCount} values for this parameter.`
    : hasMax ? `You may select up to ${maxSelectedCount} values for this parameter.`
    : null;

  return message == null ? null : (
    <div style={{ color: 'blue' }}>
      <div>Note: {message}</div>
      <div>There are currently {props.selectedCount} selected values.</div>
    </div>
  );
}