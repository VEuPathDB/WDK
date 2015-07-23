import React from 'react';
import wrappable from '../utils/wrappable';

let RecordHeading = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  stubHandler(actionName, e) {
    e.preventDefault();
    alert('You clicked ' + actionName);
  },

  render() {
    let { record, recordClass } = this.props;
    let actions = [
      'Add a comment',
      'Add to basket',
      'Add to favorites',
      'Download' + recordClass.displayName
    ];
    return (
      <div>
        <h1 className="wdk-RecordHeading">{recordClass.displayName} {record.attributes.primary_key}</h1>
        <ul className="wdk-RecordActions">
          {actions.map(action => {
            return (
              <li className="wdk-RecordActionItem">
                <a href="#" onClick={e => this.stubHandler(action, e)}>{action}</a>
              </li>
            );
          })}
        </ul>
      </div>
    );
  }

});

export default wrappable(RecordHeading);
