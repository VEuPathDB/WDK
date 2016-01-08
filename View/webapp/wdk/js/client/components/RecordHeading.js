import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordOverview from './RecordOverview';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

export default wrappable(function RecordHeading(props) {
  let { record, recordClass } = props;
  let actions = [
    { name: 'Add a comment', icon: 'comment' },
    { name: 'Add to basket', icon: 'shopping-cart' },
    { name: 'Add to favorites', icon: 'star-o' },
    { name: 'Download ' + recordClass.displayName, icon: 'download' }
  ];
  return (
    <div className="wdk-RecordHeadingSection">
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
      <RecordOverview record={record} recordClass={recordClass}/>
    </div>
  );
});
