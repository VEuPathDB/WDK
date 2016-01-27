import React from 'react';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let RecordAttribute = props => {
  return (
    <div id={props.id} className={props.className}>
      <div className="wdk-RecordAttributeName">
        <strong>{props.displayName}</strong>
      </div>
      <div className="wdk-RecordAttributeValue">
        {renderAttributeValue(props.value, null, 'div')}
      </div>
    </div>
  );
}

RecordAttribute.propTypes = {
  displayName: React.PropTypes.string.isRequired,
  value: React.PropTypes.oneOfType([
    React.PropTypes.string,
    React.PropTypes.object
  ]).isRequired
};

export default wrappable(RecordAttribute);
