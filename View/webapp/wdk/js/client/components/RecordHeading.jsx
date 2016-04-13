import React from 'react';
import {safeHtml, wrappable} from '../utils/componentUtils';
import RecordActionLink from './RecordActionLink';
import RecordOverview from './RecordOverview';

let RecordHeading = props => {
  let { record, recordClass, headerActions } = props;
  return (
    <div>
      <ul className="wdk-RecordActions">
        {headerActions.map((action, index) => {
          return (
            <li key={index} className="wdk-RecordActionItem">
              <RecordActionLink {...props} {...action}/>
            </li>
          );
        })}
      </ul>
      <h1 className="wdk-RecordHeading">{recordClass.displayName}: {safeHtml(record.displayName)}</h1>
      <RecordOverview record={record} recordClass={recordClass}/>
    </div>
  );
}

RecordHeading.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired,
  headerActions: React.PropTypes.arrayOf(React.PropTypes.object)
}

export default wrappable(RecordHeading);
