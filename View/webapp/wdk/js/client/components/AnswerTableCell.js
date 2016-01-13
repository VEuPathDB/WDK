import React from 'react';
import ReactRouter from 'react-router';
import RecordLink from './RecordLink';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let primaryKeyName = 'primary_key';

let AnswerTableCell = React.createClass({

  propTypes: {
    // TODO Put reusable propTypes in a module
    value: React.PropTypes.string,
    descriptor: React.PropTypes.object.isRequired,
    record: React.PropTypes.object.isRequired,
    recordClass: React.PropTypes.object.isRequired,
    width: React.PropTypes.number.isRequired
  },

  render() {
    if (this.props.value == null) {
      return null;
    }

    let { value, descriptor, record, recordClass, width } = this.props;

    if (descriptor.name === primaryKeyName) {
      return (
        <RecordLink
          record={record}
          recordClass={recordClass}
          className="wdk-AnswerTable-recordLink"
        >
          {renderAttributeValue(value, this.props)}
        </RecordLink>
      );
    }
    else {
      return renderAttributeValue(value, this.props);
    }
  }

});

export default wrappable(AnswerTableCell);
