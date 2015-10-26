import React from 'react';
import ReactRouter from 'react-router';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

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
        <Link to={href} className="wdk-AnswerTable-recordLink">
          {renderAttributeValue(value, this.props)}
        </Link>
      );
    }
    else {
      return renderAttributeValue(value, this.props);
    }
  }

});

export default wrappable(AnswerTableCell);
