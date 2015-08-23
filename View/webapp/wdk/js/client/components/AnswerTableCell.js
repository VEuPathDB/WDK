import React from 'react';
import ReactRouter from 'react-router';
import wrappable from '../utils/wrappable';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let { Link, Navigation } = ReactRouter;
let primaryKeyName = 'primary_key';

let AnswerTableCell = React.createClass({

  propTypes: {
    // TODO Put reusable propTypes in a module
    value: React.PropTypes.string.isRequired,
    descriptor: React.PropTypes.object.isRequired,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.string.isRequired,
    width: React.PropTypes.number.isRequired
  },

  mixins: [ Navigation ],

  render() {
    if (this.props.value == null) {
      return null;
    }

    let { value, descriptor, record, recordClass, width } = this.props;

    if (descriptor.name === primaryKeyName) {
      let href = this.makeHref('record', { class: recordClass }, record.id);
      return (
        <Link
          {...this.props}
          className="wdk-AnswerTable-recordLink"
          to={href}
          dangerouslySetInnerHTML={{__html: formatAttributeValue(value, descriptor.type) }}
        />
      );
    }
    else {
      return (
        <span
          {...this.props}
          dangerouslySetInnerHTML={{__html: formatAttributeValue(value, descriptor.type) }}
        />
      );
    }
  }

});

export default wrappable(AnswerTableCell);
