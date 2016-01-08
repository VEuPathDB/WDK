import React from 'react';
import { safeHtml, wrappable } from '../utils/componentUtils';

export default wrappable(function RecordOverview(props) {
  return (
    <div className="wdk-RecordOverview">
      {safeHtml(props.record.overview)}
    </div>
  );
});
