import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import { safeHtml, wrappable } from '../utils/componentUtils';

let stubHandler = actionName => event => {
  event.preventDefault();
  alert('You clicked ' + actionName);
};

let RecordHeading = React.createClass({

  mixins: [ PureRenderMixin ],

  render() {
    let { record, recordClass } = this.props;
    let actions = [
      { name: 'Add a comment', icon: 'comment' },
      { name: 'Add to basket', icon: 'shopping-cart' },
      { name: 'Add to favorites', icon: 'star-o' },
      { name: 'Download ' + recordClass.displayName, icon: 'download' }
    ];
    return (
      <div className="wdk-RecordOverview">
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
        <div>
          {safeHtml(record.overview)}
        </div>
      </div>
    );
  }

});

export default wrappable(RecordHeading);
