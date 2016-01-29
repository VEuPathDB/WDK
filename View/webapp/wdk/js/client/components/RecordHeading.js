import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordActionLink from './RecordActionLink';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

let RecordHeading = props => {
  let { record, recordClass } = props;
  let actions = [
    { label: 'Add to basket', iconClassName: 'fa fa-shopping-basket' },
    { label: 'Add to favorites', iconClassName: 'fa fa-lg fa-star' },
    { label: 'Download ' + recordClass.displayName, iconClassName: 'fa fa-lg fa-download' }
  ];
  return (
    <div>
      <ul className="wdk-RecordActions">
        {actions.map((action, index) => {
          return (
            <li key={index} className="wdk-RecordActionItem">
              <RecordActionLink {...props} {...action} onClick={stubHandler(action.label)}/>
            </li>
          );
        })}
      </ul>
      <h1 className="wdk-RecordHeading">{recordClass.displayName} {record.displayName}</h1>
    </div>
  );
}

RecordHeading.propTypes = {
  record: React.PropTypes.object.isRequired,
  recordClass: React.PropTypes.object.isRequired
}

export default wrappable(RecordHeading);
