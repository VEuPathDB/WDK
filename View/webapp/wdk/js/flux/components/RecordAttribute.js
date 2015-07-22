import React from 'react';
import wrappable from '../utils/wrappable';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let RecordAttribute = React.createClass({

  propTypes: {
    attribute: React.PropTypes.object.isRequired
  },

  render() {
    let { attribute } = this.props;
    return (
      <div
        className="wdk-Record-attributeContent"
        dangerouslySetInnerHTML={{__html: formatAttributeValue(attribute.value)}}
      />
    );
  }

});

export default wrappable(RecordAttribute);
