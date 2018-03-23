import React from 'react';

export default function UnknownCount(props) {
  const { activeFieldSummary, dataCount, displayName } = props;
  const unknownCount = dataCount - activeFieldSummary.internalsCount;
  const unknownPercent = Math.round(unknownCount / activeFieldSummary.internalsCount * 100);
  return unknownCount > 0
    ? (
      <div className="unknown-count">
        <b>{unknownCount.toLocaleString()} ({unknownPercent}%)</b> {displayName} have no data provided for this filter
      </div>
    )
    : null
}
