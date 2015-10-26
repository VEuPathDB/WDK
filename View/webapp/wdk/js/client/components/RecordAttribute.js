import React from 'react';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let RecordAttribute = React.createClass({

  propTypes: {
    value: React.PropTypes.oneOfType([
      React.PropTypes.string,
      React.PropTypes.object
    ]).isRequired
  },

  render() {
    return renderAttributeValue(this.props.value, null, 'div');
  }

});

export default wrappable(RecordAttribute);
