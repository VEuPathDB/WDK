import React from 'react';
import { wrappable } from '../utils/componentUtils';

let RecordHeading = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  stubHandler(actionName, e) {
    e.preventDefault();
    alert('You clicked ' + actionName);
  },

  render() {
    let { record, recordClass } = this.props;
    let actions = [
      { name: 'Add a comment', icon: 'comment' },
      { name: 'Add to basket', icon: 'list' },
      { name: 'Add to favorites', icon: 'star-o' },
      { name: 'Download' + recordClass.displayName, icon: 'download' }
    ];
    return (
      <div>
        <ul className="wdk-RecordActions">
          {actions.map(action => {
            return (
              <li key={action.name} className="wdk-RecordActionItem">
                <a href="#" onClick={e => this.stubHandler(action.name, e)}>
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

});

export default wrappable(RecordHeading);
