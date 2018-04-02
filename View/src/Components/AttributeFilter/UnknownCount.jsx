import React from 'react';

export default function UnknownCount(props) {
  const { activeFieldSummary, dataCount, displayName } = props;
  const unknownCount = dataCount - activeFieldSummary.internalsCount;
  return unknownCount > 0
    ? (
      <div className="unknown-count">
        <b>{unknownCount.toLocaleString()} of {dataCount.toLocaleString()}</b> {displayName} have no data provided for this filter
      </div>
    )
    : null
}
