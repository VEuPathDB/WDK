import React from 'react';
import { wrappable } from '../utils/componentUtils';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

let RecordHeading = props => {
  let { record, recordClass } = props;
  let actions = [
    { name: 'Add to basket', icon: 'shopping-cart' },
    { name: 'Add to favorites', icon: 'star-o' },
    { name: 'Download ' + recordClass.displayName, icon: 'download' }
  ];
  return (
    <div>
      <ul className="wdk-RecordActions">
        {actions.map(action => {
          return (
            <li key={action.name} className="wdk-RecordActionItem">
              <a href="#" onClick={stubHandler(action.name)}>
                {action.name} <i style={{ marginLeft: '.4em'}} className={'fa fa-lg fa-' + action.icon}/>
              </a>
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
