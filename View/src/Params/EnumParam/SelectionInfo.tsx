import React from 'react';
import { EnumParam } from 'Utils/WdkModel';

type Props = {
  parameter: EnumParam;
  selectedCount: number;
  allCount: number;
  alwaysShowCount?: boolean;
};

export default function SelectionInfo(props: Props) {
  const { alwaysShowCount = false } = props;
  const { minSelectedCount, maxSelectedCount } = props.parameter;
  const hasMin = minSelectedCount > 0;
  const hasMax = maxSelectedCount > 0;
  const message = hasMin && hasMax
    ? `You may only selected between ${minSelectedCount} and ${maxSelectedCount} values for this parameter.`
    : hasMin ? `You must select at least ${minSelectedCount} values for this parameter.`
    : hasMax ? `You may select up to ${maxSelectedCount} values for this parameter.`
    : null;
  const countColor = props.selectedCount
    ? 'black'
    : 'red';

  if (message == null && alwaysShowCount == false) return null;

  return (
    <div className="treeCount">
      {message && <div>Note: {message}</div>}
      <div><span className={countColor}>{props.selectedCount} selected</span>, of {props.allCount}</div>
    </div>
  );
}
